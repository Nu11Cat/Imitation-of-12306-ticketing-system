package cn.nu11cat.train.business.service.impl;

import cn.hutool.core.date.DateTime;
import cn.nu11cat.train.business.dto.ConfirmOrderMQDto;
import cn.nu11cat.train.business.entity.ConfirmOrder;
import cn.nu11cat.train.business.enums.ConfirmOrderStatusEnum;
import cn.nu11cat.train.business.enums.RocketMQTopicEnum;
import cn.nu11cat.train.business.mapper.ConfirmOrderMapper;
import cn.nu11cat.train.business.req.ConfirmOrderDoReq;
import cn.nu11cat.train.business.req.ConfirmOrderTicketReq;
import cn.nu11cat.train.business.service.BeforeConfirmOrderService;
import cn.nu11cat.train.business.service.ConfirmOrderService;
import cn.nu11cat.train.business.service.SkTokenService;
import cn.nu11cat.train.common.context.LoginMemberContext;
import cn.nu11cat.train.common.exception.BusinessException;
import cn.nu11cat.train.common.exception.BusinessExceptionEnum;
import cn.nu11cat.train.common.util.SnowUtil;
import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.fastjson.JSON;
import jakarta.annotation.Resource;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class BeforeConfirmOrderServiceImpl implements BeforeConfirmOrderService {

    private static final Logger LOG = LoggerFactory.getLogger(BeforeConfirmOrderServiceImpl.class);

    @Resource
    private ConfirmOrderMapper confirmOrderMapper;

    @Autowired
    private SkTokenService skTokenService;

    @Resource
    private RocketMQTemplate rocketMQTemplate;

    @Resource
    private ConfirmOrderService confirmOrderService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @SentinelResource(value = "beforeDoConfirm", blockHandler = "beforeDoConfirmBlock")
    @Override
    public Long beforeDoConfirm(ConfirmOrderDoReq req) {
        Long id = null;

        req.setMemberId(LoginMemberContext.getId());
        Date date = req.getDate();
        String trainCode = req.getTrainCode();
        String start = req.getStart();
        String end = req.getEnd();
        List<ConfirmOrderTicketReq> tickets = req.getTickets();

        // === 令牌校验 ===
        boolean validSkToken = skTokenService.acquireToken(date, trainCode, tickets.size());
        if (!validSkToken) {
            LOG.info("用户 {} 抢票失败，令牌耗尽", req.getMemberId());
            throw new BusinessException(BusinessExceptionEnum.CONFIRM_ORDER_SK_TOKEN_FAIL);
        }

        try {
            // 保存确认订单表，状态初始
            DateTime now = DateTime.now();
            ConfirmOrder confirmOrder = new ConfirmOrder();
            confirmOrder.setId(SnowUtil.getSnowflakeNextId());
            confirmOrder.setCreateTime(now);
            confirmOrder.setUpdateTime(now);
            confirmOrder.setMemberId(req.getMemberId());
            confirmOrder.setDate(date);
            confirmOrder.setTrainCode(trainCode);
            confirmOrder.setStart(start);
            confirmOrder.setEnd(end);
            confirmOrder.setDailyTrainTicketId(req.getDailyTrainTicketId());
            confirmOrder.setStatus(ConfirmOrderStatusEnum.INIT.getCode());
            confirmOrder.setTickets(JSON.toJSONString(tickets));

            confirmOrderMapper.insert(confirmOrder);

            // 发送MQ排队购票
            ConfirmOrderMQDto confirmOrderMQDto = new ConfirmOrderMQDto();
            confirmOrderMQDto.setDate(date);
            confirmOrderMQDto.setTrainCode(trainCode);
            confirmOrderMQDto.setLogId(MDC.get("LOG_ID"));
            String reqJson = JSON.toJSONString(confirmOrderMQDto);

            // 通过redis验证是否重复发送MQ消息
//            String idempotentKey = "mqfa:doConfirm:idempotent:" + confirmOrderMQDto.getLogId();
//            Boolean firstConsume = redisTemplate.opsForValue().setIfAbsent(idempotentKey, "1", 10, TimeUnit.MINUTES);

            LOG.info("排队购票，发送mq开始，消息：{}", reqJson);
            rocketMQTemplate.convertAndSend(RocketMQTopicEnum.CONFIRM_ORDER.getCode(), reqJson);
            LOG.info("排队购票，发送mq结束");

//            // 调用确认订单服务
//            confirmOrderService.doConfirm(confirmOrderMQDto);

            id = confirmOrder.getId();
        } catch (Exception e) {
            // 下单失败，释放令牌
            skTokenService.releaseToken(date, trainCode, tickets.size());
            throw e;
        }

        return id;
    }


    /**
     * 降级方法，需包含限流方法的所有参数和BlockException参数
     * @param req
     * @param e
     */
    public void beforeDoConfirmBlock(ConfirmOrderDoReq req, BlockException e) {
        LOG.info("购票请求被限流：{}", req);
        throw new BusinessException(BusinessExceptionEnum.CONFIRM_ORDER_FLOW_EXCEPTION);
    }
}
