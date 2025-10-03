package cn.nu11cat.train.business.service.impl;

import cn.nu11cat.train.business.dto.AddMemberTicketMQDto;
import cn.nu11cat.train.business.entity.ConfirmOrder;
import cn.nu11cat.train.business.entity.DailyTrainSeat;
import cn.nu11cat.train.business.entity.DailyTrainTicket;
import cn.nu11cat.train.business.enums.ConfirmOrderStatusEnum;
import cn.nu11cat.train.business.feign.MemberFeign;
import cn.nu11cat.train.business.mapper.ConfirmOrderMapper;
import cn.nu11cat.train.business.mapper.DailyTrainSeatMapper;
import cn.nu11cat.train.business.mapper.cust.DailyTrainTicketMapperCust;
import cn.nu11cat.train.business.req.ConfirmOrderTicketReq;
import cn.nu11cat.train.business.service.AfterConfirmOrderService;
import cn.nu11cat.train.common.resp.CommonResp;
import com.alibaba.fastjson.JSON;
import io.seata.core.context.RootContext;
import io.seata.spring.annotation.GlobalTransactional;
import jakarta.annotation.Resource;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class AfterConfirmOrderServiceImpl implements AfterConfirmOrderService {

    private static final Logger LOG = LoggerFactory.getLogger(AfterConfirmOrderServiceImpl.class);

    @Resource
    private DailyTrainSeatMapper dailyTrainSeatMapper;

    @Resource
    private DailyTrainTicketMapperCust dailyTrainTicketMapperCust;

    @Resource
    private MemberFeign memberFeign;

    @Resource
    private ConfirmOrderMapper confirmOrderMapper;

    @Resource
    private RocketMQTemplate rocketMQTemplate;

    /**
     * 选中座位后事务处理
     */
    @GlobalTransactional
    @Override
    public void afterDoConfirm(DailyTrainTicket dailyTrainTicket,
                               List<DailyTrainSeat> finalSeatList,
                               List<ConfirmOrderTicketReq> tickets,
                               ConfirmOrder confirmOrder) throws Exception {

        LOG.info("seata全局事务ID: {}", RootContext.getXID());

        for (int j = 0; j < finalSeatList.size(); j++) {
            DailyTrainSeat dailyTrainSeat = finalSeatList.get(j);

            // 更新座位售卖情况
            dailyTrainSeat.setUpdateTime(new Date());
            dailyTrainSeatMapper.updateById(dailyTrainSeat);

            // 计算影响的库存区间
            Integer startIndex = dailyTrainTicket.getStartIndex();
            Integer endIndex = dailyTrainTicket.getEndIndex();
            char[] chars = dailyTrainSeat.getSell().toCharArray();

            Integer maxStartIndex = endIndex - 1;
            Integer minEndIndex = startIndex + 1;
            Integer minStartIndex = 0;
            for (int i = startIndex - 1; i >= 0; i--) {
                if (chars[i] == '1') {
                    minStartIndex = i + 1;
                    break;
                }
            }
            LOG.info("影响出发站区间：" + minStartIndex + "-" + maxStartIndex);

            Integer maxEndIndex = chars.length;
            for (int i = endIndex; i < chars.length; i++) {
                if (chars[i] == '1') {
                    maxEndIndex = i;
                    break;
                }
            }
            LOG.info("影响到达站区间：" + minEndIndex + "-" + maxEndIndex);

            // 查最新余票记录，获取 version
            DailyTrainTicket latestTicket = dailyTrainTicketMapperCust.selectByUnique(
                    dailyTrainTicket.getDate(),
                    dailyTrainTicket.getTrainCode(),
                    dailyTrainTicket.getStart(),
                    dailyTrainTicket.getEnd()
            );
//            LOG.info(" 更新余票详情表：" + dailyTrainSeat);
//            LOG.info(" 获取的 version：" + latestTicket.getVersion());
            // 更新余票详情表
            dailyTrainTicketMapperCust.updateCountBySellOptimistic(
                    dailyTrainSeat.getDate(),
                    dailyTrainSeat.getTrainCode(),
                    dailyTrainSeat.getSeatType(),
                    minStartIndex,
                    maxStartIndex,
                    minEndIndex,
                    maxEndIndex,
                    latestTicket.getVersion()
            );

            // 更新订单状态为成功
            confirmOrder.setStatus(ConfirmOrderStatusEnum.SUCCESS.getCode());
            confirmOrder.setUpdateTime(new Date());
            confirmOrderMapper.updateById(confirmOrder);

//            // 调用会员服务接口
//            MemberTicketReq memberTicketReq = new MemberTicketReq();
//            memberTicketReq.setMemberId(confirmOrder.getMemberId());
//            memberTicketReq.setPassengerId(tickets.get(j).getPassengerId());
//            memberTicketReq.setPassengerName(tickets.get(j).getPassengerName());
//            memberTicketReq.setTrainDate(dailyTrainTicket.getDate());
//            memberTicketReq.setTrainCode(dailyTrainTicket.getTrainCode());
//            memberTicketReq.setCarriageIndex(dailyTrainSeat.getCarriageIndex());
//            memberTicketReq.setSeatRow(dailyTrainSeat.getRowIndex());
//            memberTicketReq.setSeatCol(dailyTrainSeat.getColIndex());
//            memberTicketReq.setStartStation(dailyTrainTicket.getStart());
//            memberTicketReq.setStartTime(dailyTrainTicket.getStartTime());
//            memberTicketReq.setEndStation(dailyTrainTicket.getEnd());
//            memberTicketReq.setEndTime(dailyTrainTicket.getEndTime());
//            memberTicketReq.setSeatType(dailyTrainSeat.getSeatType());
//
//            CommonResp<Object> commonResp = memberFeign.save(memberTicketReq);
//            LOG.info("调用member接口，返回：{}", commonResp);

            AddMemberTicketMQDto dto = new AddMemberTicketMQDto();
            dto.setConfirmOrderId(confirmOrder.getId());
            dto.setMemberId(confirmOrder.getMemberId());
            dto.setTrainDate(dailyTrainTicket.getDate());
            dto.setTrainCode(dailyTrainTicket.getTrainCode());
            dto.setLogId(MDC.get("LOG_ID"));

            List<AddMemberTicketMQDto.PassengerTicketInfo> ticketInfos = new ArrayList<>();
            for (int i = 0; i < tickets.size(); i++) {
                DailyTrainSeat seat = finalSeatList.get(i);
                ConfirmOrderTicketReq ticket = tickets.get(i);

                AddMemberTicketMQDto.PassengerTicketInfo info = new AddMemberTicketMQDto.PassengerTicketInfo();
                info.setPassengerId(ticket.getPassengerId());
                info.setPassengerName(ticket.getPassengerName());
                info.setCarriageIndex(seat.getCarriageIndex());
                info.setRowIndex(seat.getRowIndex());
                info.setColIndex(seat.getColIndex());
                info.setSeatType(seat.getSeatType());
                info.setStartStation(dailyTrainTicket.getStart());
                info.setStartTime(dailyTrainTicket.getStartTime());
                info.setEndStation(dailyTrainTicket.getEnd());
                info.setEndTime(dailyTrainTicket.getEndTime());


                ticketInfos.add(info);
            }
            dto.setTickets(ticketInfos);

            rocketMQTemplate.convertAndSend("ADD_MEMBER_TICKET", JSON.toJSONString(dto));
            LOG.info("发送异步添加车票消息完成，orderId={}", confirmOrder.getId());


            // 模拟调用方出现异常
//             Thread.sleep(10000);
//             if (1 == 1) {
//                 throw new Exception("测试异常");
//             }
        }
    }
}
