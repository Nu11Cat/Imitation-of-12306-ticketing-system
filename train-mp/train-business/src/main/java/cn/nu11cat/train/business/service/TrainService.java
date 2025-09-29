package cn.nu11cat.train.business.service;

import cn.nu11cat.train.business.entity.Train;
import cn.nu11cat.train.business.req.TrainQueryReq;
import cn.nu11cat.train.business.req.TrainSaveReq;
import cn.nu11cat.train.business.resp.TrainQueryResp;
import cn.nu11cat.train.common.resp.PageResp;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.validation.Valid;

import java.util.List;

/**
 * <p>
 * 车次 服务类
 * </p>
 *
 * @author nu11cat
 * @since 2025-09-29
 */
public interface TrainService extends IService<Train> {

    List<TrainQueryResp> queryAll();

    PageResp<TrainQueryResp> queryList(@Valid TrainQueryReq req);

    void delete(Long id);

    void save(@Valid TrainSaveReq req);
}
