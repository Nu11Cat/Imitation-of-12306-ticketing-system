package cn.nu11cat.train.business.service;

import cn.nu11cat.train.business.entity.SkToken;
import cn.nu11cat.train.business.req.SkTokenQueryReq;
import cn.nu11cat.train.business.req.SkTokenSaveReq;
import cn.nu11cat.train.business.resp.SkTokenQueryResp;
import cn.nu11cat.train.common.resp.PageResp;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Date;

/**
 * <p>
 * 秒杀令牌 服务类
 * </p>
 *
 * @author nu11cat
 * @since 2025-09-29
 */
public interface SkTokenService extends IService<SkToken> {

    void save(@Valid SkTokenSaveReq req);

    PageResp<SkTokenQueryResp> queryList(@Valid SkTokenQueryReq req);

    void delete(Long id);

    void genDaily(Date date, String code);

    void initDailyToken(Date date, String trainCode, int capacity, int ratePerSecond);

    boolean acquireToken(Date date, String trainCode, int size);

    void releaseToken(Date date, String trainCode, int size);

}
