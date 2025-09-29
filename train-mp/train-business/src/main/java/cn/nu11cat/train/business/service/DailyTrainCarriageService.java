package cn.nu11cat.train.business.service;

import cn.nu11cat.train.business.entity.DailyTrainCarriage;
import cn.nu11cat.train.business.req.DailyTrainCarriageQueryReq;
import cn.nu11cat.train.business.req.DailyTrainCarriageSaveReq;
import cn.nu11cat.train.business.resp.DailyTrainCarriageQueryResp;
import cn.nu11cat.train.common.resp.PageResp;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.validation.Valid;

import java.util.Date;
import java.util.List;

/**
 * <p>
 * 每日车厢 服务类
 * </p>
 *
 * @author nu11cat
 * @since 2025-09-29
 */
public interface DailyTrainCarriageService extends IService<DailyTrainCarriage> {

    void save(@Valid DailyTrainCarriageSaveReq req);

    PageResp<DailyTrainCarriageQueryResp> queryList(@Valid DailyTrainCarriageQueryReq req);

    void delete(Long id);

    void genDaily(Date date, String code);

    List<DailyTrainCarriage> selectBySeatType(Date date, String trainCode, String seatTyp);
}
