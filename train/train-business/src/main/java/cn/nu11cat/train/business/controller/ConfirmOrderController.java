package cn.nu11cat.train.business.controller;

import cn.nu11cat.train.business.req.ConfirmOrderDoReq;
import cn.nu11cat.train.business.service.ConfirmOrderService;
import cn.nu11cat.train.common.exception.BusinessExceptionEnum;
import cn.nu11cat.train.common.resp.CommonResp;
import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/confirm-order")
public class ConfirmOrderController {

    private static final Logger LOG = LoggerFactory.getLogger(ConfirmOrderService.class);

    @Resource
    private ConfirmOrderService confirmOrderService;

    // 接口的资源名称不要和接口路径一致，会导致限流后走不到降级方法中
    @SentinelResource(value = "confirmOrderDo", blockHandler = "doConfirmBlock")
    @PostMapping("/do")
    public CommonResp<Object> doConfirm(@Valid @RequestBody ConfirmOrderDoReq req) {
        confirmOrderService.doConfirm(req);
        return new CommonResp<>();
    }

    /** 降级方法，需包含限流方法的所有参数和BlockException参数，且返回值要保持一致
     * @param req
     * @param e
     */
    public CommonResp<Object> doConfirmBlock(ConfirmOrderDoReq req, BlockException e) {
        LOG.info("ConfirmOrderController购票请求被限流：{}", req);
        // throw new BusinessException(BusinessExceptionEnum.CONFIRM_ORDER_FLOW_EXCEPTION);
        CommonResp<Object> commonResp = new CommonResp<>();
        commonResp.setSuccess(false);
        commonResp.setMessage(BusinessExceptionEnum.CONFIRM_ORDER_FLOW_EXCEPTION.getDesc());
        return commonResp;

    }

}
