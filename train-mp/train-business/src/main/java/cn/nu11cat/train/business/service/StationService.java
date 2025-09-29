package cn.nu11cat.train.business.service;

import cn.nu11cat.train.business.entity.Station;
import cn.nu11cat.train.business.req.StationQueryReq;
import cn.nu11cat.train.business.req.StationSaveReq;
import cn.nu11cat.train.business.resp.StationQueryResp;
import cn.nu11cat.train.common.resp.PageResp;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.validation.Valid;

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

    void save(@Valid StationSaveReq req);

    PageResp<StationQueryResp> queryList(@Valid StationQueryReq req);

    void delete(Long id);
}
