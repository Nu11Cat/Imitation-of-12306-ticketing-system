package cn.nu11cat.train.business.mq;

import cn.nu11cat.train.business.dto.AddMemberTicketMQDto;
import cn.nu11cat.train.business.feign.MemberFeign;
import cn.nu11cat.train.common.req.MemberTicketReq;
import cn.nu11cat.train.common.resp.CommonResp;
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
@RocketMQMessageListener(consumerGroup = "add-member-ticket-group", topic = "ADD_MEMBER_TICKET")
public class AddMemberTicketConsumer implements RocketMQListener<MessageExt> {

    @Resource
    private MemberFeign memberFeign;

    private static final Logger LOG = LoggerFactory.getLogger(AddMemberTicketConsumer.class);

    @Override
    public void onMessage(MessageExt messageExt) {
        String msg = new String(messageExt.getBody());
        AddMemberTicketMQDto dto = JSON.parseObject(msg, AddMemberTicketMQDto.class);
        MDC.put("LOG_ID", dto.getLogId());

        LOG.info("收到异步添加车票消息：{}", msg);

        for (AddMemberTicketMQDto.PassengerTicketInfo ticket : dto.getTickets()) {
            MemberTicketReq req = new MemberTicketReq();
            req.setMemberId(dto.getMemberId());
            req.setPassengerId(ticket.getPassengerId());
            req.setPassengerName(ticket.getPassengerName());
            req.setTrainDate(dto.getTrainDate());
            req.setTrainCode(dto.getTrainCode());
            req.setCarriageIndex(ticket.getCarriageIndex());
            req.setSeatRow(ticket.getRowIndex());
            req.setSeatCol(ticket.getColIndex());
            req.setSeatType(ticket.getSeatType());
            req.setStartStation(ticket.getStartStation());
            req.setStartTime(ticket.getStartTime());
            req.setEndStation(ticket.getEndStation());
            req.setEndTime(ticket.getEndTime());


            CommonResp<Object> resp = memberFeign.save(req);
            LOG.info("调用会员接口结果：{}", resp);
        }
    }
}
