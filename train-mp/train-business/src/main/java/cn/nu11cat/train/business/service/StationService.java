package cn.nu11cat.train.business.service;

import cn.nu11cat.train.business.entity.Station;
import cn.nu11cat.train.business.resp.StationQueryResp;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 车站 服务类
 * </p>
 *
 * @author nu11cat
 * @since 2025-09-29
 */
public interface StationService extends IService<Station> {

    List<StationQueryResp> queryAll();
}
