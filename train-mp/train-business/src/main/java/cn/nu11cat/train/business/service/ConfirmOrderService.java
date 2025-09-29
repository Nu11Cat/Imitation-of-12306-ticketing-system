package cn.nu11cat.train.business.service;

import cn.nu11cat.train.business.dto.ConfirmOrderMQDto;
import cn.nu11cat.train.business.entity.ConfirmOrder;
import cn.nu11cat.train.business.req.ConfirmOrderDoReq;
import cn.nu11cat.train.business.req.ConfirmOrderQueryReq;
import cn.nu11cat.train.business.resp.ConfirmOrderQueryResp;
import cn.nu11cat.train.common.resp.PageResp;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.validation.Valid;

/**
 * <p>
 * 确认订单 服务类
 * </p>
 *
 * @author nu11cat
 * @since 2025-09-29
 */
public interface ConfirmOrderService extends IService<ConfirmOrder> {

    void save(@Valid ConfirmOrderDoReq req);

    PageResp<ConfirmOrderQueryResp> queryList(@Valid ConfirmOrderQueryReq req);

    void delete(Long id);

    void doConfirm(ConfirmOrderMQDto confirmOrderMQDto);

    Integer queryLineCount(Long id);
}
