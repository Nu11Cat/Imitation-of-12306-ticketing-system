package cn.nu11cat.train.business.mq;

import cn.nu11cat.train.business.dto.ConfirmOrderMQDto;
import cn.nu11cat.train.business.service.ConfirmOrderService;
import cn.nu11cat.train.common.exception.BusinessException;
import com.alibaba.fastjson.JSON;
import jakarta.annotation.Resource;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

@Service
@RocketMQMessageListener(consumerGroup = "default", topic = "CONFIRM_ORDER")
public class ConfirmOrderConsumer implements RocketMQListener<MessageExt> {

    private static final Logger LOG = LoggerFactory.getLogger(ConfirmOrderConsumer.class);

    @Resource
    private ConfirmOrderService confirmOrderService;

    @Override
    public void onMessage(MessageExt messageExt) {
        try {
            byte[] body = messageExt.getBody();
            ConfirmOrderMQDto dto = JSON.parseObject(new String(body), ConfirmOrderMQDto.class);
            MDC.put("LOG_ID", dto.getLogId());
            LOG.info("ROCKETMQ收到消息：{}", new String(body));
            confirmOrderService.doConfirm(dto);
        } catch (BusinessException e) {
            // 业务逻辑错误（如库存不足），不重试
            LOG.error("业务异常无需重试", e);
        } catch (Exception e) {
            // 系统异常（如数据库连接失败），触发框架重试
            throw e;
        }

    }
}
