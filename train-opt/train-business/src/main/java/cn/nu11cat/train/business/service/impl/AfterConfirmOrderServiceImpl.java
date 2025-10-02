package cn.nu11cat.train.business.service.impl;

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
import cn.nu11cat.train.common.req.MemberTicketReq;
import cn.nu11cat.train.common.resp.CommonResp;
import io.seata.core.context.RootContext;
import io.seata.spring.annotation.GlobalTransactional;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

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

            // MyBatis-Plus 更新座位售卖情况
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

            // 更新余票详情表（仍用自定义 Mapper，可用 MP 更新Wrapper替换）
            dailyTrainTicketMapperCust.updateCountBySell(
                    dailyTrainSeat.getDate(),
                    dailyTrainSeat.getTrainCode(),
                    dailyTrainSeat.getSeatType(),
                    minStartIndex,
                    maxStartIndex,
                    minEndIndex,
                    maxEndIndex
            );

            // 调用会员服务接口
            MemberTicketReq memberTicketReq = new MemberTicketReq();
            memberTicketReq.setMemberId(confirmOrder.getMemberId());
            memberTicketReq.setPassengerId(tickets.get(j).getPassengerId());
            memberTicketReq.setPassengerName(tickets.get(j).getPassengerName());
            memberTicketReq.setTrainDate(dailyTrainTicket.getDate());
            memberTicketReq.setTrainCode(dailyTrainTicket.getTrainCode());
            memberTicketReq.setCarriageIndex(dailyTrainSeat.getCarriageIndex());
            memberTicketReq.setSeatRow(dailyTrainSeat.getRowIndex());
            memberTicketReq.setSeatCol(dailyTrainSeat.getColIndex());
            memberTicketReq.setStartStation(dailyTrainTicket.getStart());
            memberTicketReq.setStartTime(dailyTrainTicket.getStartTime());
            memberTicketReq.setEndStation(dailyTrainTicket.getEnd());
            memberTicketReq.setEndTime(dailyTrainTicket.getEndTime());
            memberTicketReq.setSeatType(dailyTrainSeat.getSeatType());

            CommonResp<Object> commonResp = memberFeign.save(memberTicketReq);
            LOG.info("调用member接口，返回：{}", commonResp);

            // 更新订单状态为成功 (MyBatis-Plus updateById)
            confirmOrder.setStatus(ConfirmOrderStatusEnum.SUCCESS.getCode());
            confirmOrder.setUpdateTime(new Date());
            confirmOrderMapper.updateById(confirmOrder);

            // 模拟调用方出现异常
//             Thread.sleep(10000);
//             if (1 == 1) {
//                 throw new Exception("测试异常");
//             }
        }
    }
}
