package cn.nu11cat.train.business.service;

import cn.nu11cat.train.business.entity.ConfirmOrder;
import cn.nu11cat.train.business.entity.DailyTrainSeat;
import cn.nu11cat.train.business.entity.DailyTrainTicket;
import cn.nu11cat.train.business.req.ConfirmOrderTicketReq;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface AfterConfirmOrderService {

    /**
     * 选中座位后事务处理：
     *  座位表修改售卖情况sell；
     *  余票详情表修改余票；
     *  为会员增加购票记录
     *  更新确认订单为成功
     */
    void afterDoConfirm(DailyTrainTicket dailyTrainTicket,
                        List<DailyTrainSeat> finalSeatList,
                        List<ConfirmOrderTicketReq> tickets,
                        ConfirmOrder confirmOrder) throws Exception;
}
