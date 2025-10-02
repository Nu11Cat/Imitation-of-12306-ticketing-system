package cn.nu11cat.train.business.service;

import cn.nu11cat.train.business.entity.DailyTrainSeat;
import cn.nu11cat.train.business.req.DailyTrainSeatQueryReq;
import cn.nu11cat.train.business.req.DailyTrainSeatSaveReq;
import cn.nu11cat.train.business.req.SeatSellReq;
import cn.nu11cat.train.business.resp.DailyTrainSeatQueryResp;
import cn.nu11cat.train.business.resp.SeatSellResp;
import cn.nu11cat.train.common.resp.PageResp;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.validation.Valid;

import java.util.Date;
import java.util.List;

/**
 * <p>
 * 每日座位 服务类
 * </p>
 *
 * @author nu11cat
 * @since 2025-09-29
 */
public interface DailyTrainSeatService extends IService<DailyTrainSeat> {

    void save(@Valid DailyTrainSeatSaveReq req);

    PageResp<DailyTrainSeatQueryResp> queryList(@Valid DailyTrainSeatQueryReq req);

    void delete(Long id);

    void genDaily(Date date, String code);

    int countSeat(Date date, String trainCode, String code);

    int countSeat(Date date, String trainCode);

    List<DailyTrainSeat> selectByCarriage(Date date, String trainCode, Integer index);

    List<SeatSellResp> querySeatSell(@Valid SeatSellReq req);
}
