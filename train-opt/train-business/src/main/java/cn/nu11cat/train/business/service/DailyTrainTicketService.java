package cn.nu11cat.train.business.service;

import cn.nu11cat.train.business.entity.DailyTrain;
import cn.nu11cat.train.business.entity.DailyTrainTicket;
import cn.nu11cat.train.business.req.DailyTrainTicketQueryReq;
import cn.nu11cat.train.business.req.DailyTrainTicketSaveReq;
import cn.nu11cat.train.business.resp.DailyTrainTicketQueryResp;
import cn.nu11cat.train.common.resp.PageResp;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.validation.Valid;

import java.util.Date;

/**
 * <p>
 * 余票信息 服务类
 * </p>
 *
 * @author nu11cat
 * @since 2025-09-29
 */
public interface DailyTrainTicketService extends IService<DailyTrainTicket> {

    void save(@Valid DailyTrainTicketSaveReq req);

    PageResp<DailyTrainTicketQueryResp> queryList(@Valid DailyTrainTicketQueryReq req);

    void delete(Long id);

    void genDaily(DailyTrain dailyTrain, Date date, String code);

    DailyTrainTicket selectByUnique(Date date, String trainCode, String start, String end);
}
