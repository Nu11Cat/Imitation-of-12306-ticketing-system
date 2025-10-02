package cn.nu11cat.train.business.service;

import cn.nu11cat.train.business.entity.DailyTrain;
import cn.nu11cat.train.business.req.DailyTrainQueryReq;
import cn.nu11cat.train.business.req.DailyTrainSaveReq;
import cn.nu11cat.train.business.resp.DailyTrainQueryResp;
import cn.nu11cat.train.common.resp.PageResp;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.validation.Valid;

import java.util.Date;

/**
 * <p>
 * 每日车次 服务类
 * </p>
 *
 * @author nu11cat
 * @since 2025-09-29
 */
public interface DailyTrainService extends IService<DailyTrain> {

    void save(@Valid DailyTrainSaveReq req);

    PageResp<DailyTrainQueryResp> queryList(@Valid DailyTrainQueryReq req);

    void delete(Long id);

    void genDaily(Date date);
}
