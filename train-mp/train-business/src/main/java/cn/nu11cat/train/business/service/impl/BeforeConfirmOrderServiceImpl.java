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
import com.alibaba.fastjson.JSON;
import jakarta.annotation.Resource;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

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

//    @SentinelResource(value = "beforeDoConfirm", blockHandler = "beforeDoConfirmBlock")
    @Override
    public Long beforeDoConfirm(ConfirmOrderDoReq req) {
        Long id = null;

//        for (int i = 0; i < req.getLineNumber() + 1; i++) {
            req.setMemberId(LoginMemberContext.getId());

            // 校验令牌余量（可根据需求启用）
            // boolean validSkToken = skTokenService.validSkToken(req.getDate(), req.getTrainCode(), LoginMemberContext.getId());
            // if (!validSkToken) {
            //     LOG.info("令牌校验不通过");
            //     throw new BusinessException(BusinessExceptionEnum.CONFIRM_ORDER_SK_TOKEN_FAIL);
            // }

            Date date = req.getDate();
            String trainCode = req.getTrainCode();
            String start = req.getStart();
            String end = req.getEnd();
            List<ConfirmOrderTicketReq> tickets = req.getTickets();

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

            // MyBatis-Plus 插入
            confirmOrderMapper.insert(confirmOrder);

            // 发送MQ排队购票
            ConfirmOrderMQDto confirmOrderMQDto = new ConfirmOrderMQDto();
            confirmOrderMQDto.setDate(date);
            confirmOrderMQDto.setTrainCode(trainCode);
            confirmOrderMQDto.setLogId(MDC.get("LOG_ID"));
            String reqJson = JSON.toJSONString(confirmOrderMQDto);

            LOG.info("排队购票，发送mq开始，消息：{}", reqJson);
            rocketMQTemplate.convertAndSend(RocketMQTopicEnum.CONFIRM_ORDER.getCode(), reqJson);
            LOG.info("排队购票，发送mq结束");

            // 调用确认订单服务
            confirmOrderService.doConfirm(confirmOrderMQDto);

            id = confirmOrder.getId();
//        }

        return id;
    }

//    @Override
//    public void beforeDoConfirmBlock(ConfirmOrderDoReq req, BlockException e) {
//        LOG.info("购票请求被限流：{}", req);
//        throw new BusinessException(BusinessExceptionEnum.CONFIRM_ORDER_FLOW_EXCEPTION);
//    }
}
