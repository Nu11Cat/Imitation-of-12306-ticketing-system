package cn.nu11cat.train.business.service;

import cn.nu11cat.train.business.req.ConfirmOrderDoReq;

public interface BeforeConfirmOrderService {

    /**
     * 排队购票前置处理：生成确认订单，发送MQ
     * @param req 前端购票请求
     * @return 生成的确认订单ID
     */
    Long beforeDoConfirm(ConfirmOrderDoReq req);

    /**
     * Sentinel 限流降级方法
     */
    //void beforeDoConfirmBlock(ConfirmOrderDoReq req, BlockException e);
}
