package cn.nu11cat.train.business.service;

import cn.nu11cat.train.business.entity.TrainCarriage;
import cn.nu11cat.train.business.req.TrainCarriageQueryReq;
import cn.nu11cat.train.business.req.TrainCarriageSaveReq;
import cn.nu11cat.train.business.resp.TrainCarriageQueryResp;
import cn.nu11cat.train.common.resp.PageResp;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.validation.Valid;

import java.util.List;

/**
 * <p>
 * 火车车厢 服务类
 * </p>
 *
 * @author nu11cat
 * @since 2025-09-29
 */
public interface TrainCarriageService extends IService<TrainCarriage> {

    void save(@Valid TrainCarriageSaveReq req);

    PageResp<TrainCarriageQueryResp> queryList(@Valid TrainCarriageQueryReq req);

    void delete(Long id);

    List<TrainCarriage> selectByTrainCode(String trainCode);
}
