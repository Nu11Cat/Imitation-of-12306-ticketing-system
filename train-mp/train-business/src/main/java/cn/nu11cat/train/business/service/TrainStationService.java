package cn.nu11cat.train.business.service;

import cn.nu11cat.train.business.entity.TrainStation;
import cn.nu11cat.train.business.req.TrainStationQueryReq;
import cn.nu11cat.train.business.req.TrainStationSaveReq;
import cn.nu11cat.train.business.resp.TrainStationQueryResp;
import cn.nu11cat.train.common.resp.PageResp;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.validation.Valid;

/**
 * <p>
 * 火车车站 服务类
 * </p>
 *
 * @author nu11cat
 * @since 2025-09-29
 */
public interface TrainStationService extends IService<TrainStation> {

    void save(@Valid TrainStationSaveReq req);

    PageResp<TrainStationQueryResp> queryList(@Valid TrainStationQueryReq req);

    void delete(Long id);
}
