package cn.nu11cat.train.business.service;

import cn.nu11cat.train.business.entity.Train;
import cn.nu11cat.train.business.resp.TrainQueryResp;
import com.baomidou.mybatisplus.extension.service.IService;

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
}
