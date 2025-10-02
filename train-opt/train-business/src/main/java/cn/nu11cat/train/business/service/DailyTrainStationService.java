package cn.nu11cat.train.business.service;

import cn.nu11cat.train.business.entity.DailyTrainStation;
import cn.nu11cat.train.business.req.DailyTrainStationQueryReq;
import cn.nu11cat.train.business.req.DailyTrainStationSaveReq;
import cn.nu11cat.train.business.resp.DailyTrainStationQueryResp;
import cn.nu11cat.train.common.resp.PageResp;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.validation.Valid;

import java.util.Date;
import java.util.List;

/**
 * <p>
 * 每日车站 服务类
 * </p>
 *
 * @author nu11cat
 * @since 2025-09-29
 */
public interface DailyTrainStationService extends IService<DailyTrainStation> {

    void save(@Valid DailyTrainStationSaveReq req);

    PageResp<DailyTrainStationQueryResp> queryList(@Valid DailyTrainStationQueryReq req);

    void delete(Long id);

    void genDaily(Date date, String code);

    long countByTrainCode(Date date, String trainCode);

    List<DailyTrainStationQueryResp> queryByTrain(Date date, String trainCode);
}
