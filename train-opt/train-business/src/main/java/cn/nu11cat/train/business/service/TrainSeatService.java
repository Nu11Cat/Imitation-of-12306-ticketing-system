package cn.nu11cat.train.business.service;

import cn.nu11cat.train.business.entity.TrainSeat;
import cn.nu11cat.train.business.req.TrainSeatQueryReq;
import cn.nu11cat.train.business.req.TrainSeatSaveReq;
import cn.nu11cat.train.business.resp.TrainSeatQueryResp;
import cn.nu11cat.train.common.resp.PageResp;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.validation.Valid;

import java.util.List;

/**
 * <p>
 * 座位 服务类
 * </p>
 *
 * @author nu11cat
 * @since 2025-09-29
 */
public interface TrainSeatService extends IService<TrainSeat> {

    void save(@Valid TrainSeatSaveReq req);

    PageResp<TrainSeatQueryResp> queryList(@Valid TrainSeatQueryReq req);

    void delete(Long id);

    void genTrainSeat(String trainCode);

    List<TrainSeat> selectByTrainCode(String trainCode);
}
