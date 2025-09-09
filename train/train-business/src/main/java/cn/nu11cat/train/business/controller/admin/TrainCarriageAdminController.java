package cn.nu11cat.train.business.controller.admin;

import cn.nu11cat.train.common.context.LoginMemberContext;
import cn.nu11cat.train.common.resp.CommonResp;
import cn.nu11cat.train.common.resp.PageResp;
import cn.nu11cat.train.business.req.TrainCarriageQueryReq;
import cn.nu11cat.train.business.req.TrainCarriageSaveReq;
import cn.nu11cat.train.business.resp.TrainCarriageQueryResp;
import cn.nu11cat.train.business.service.TrainCarriageService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/train-carriage")
public class TrainCarriageAdminController {

    @Resource
    private TrainCarriageService trainCarriageService;

    @PostMapping("/save")
    public CommonResp<Object> save(@Valid @RequestBody TrainCarriageSaveReq req) {
        trainCarriageService.save(req);
        return new CommonResp<>();
    }

    @GetMapping("/query-list")
    public CommonResp<PageResp<TrainCarriageQueryResp>> queryList(@Valid TrainCarriageQueryReq req) {
        PageResp<TrainCarriageQueryResp> list = trainCarriageService.queryList(req);
        return new CommonResp<>(list);
    }

    @DeleteMapping("/delete/{id}")
    public CommonResp<Object> delete(@PathVariable Long id) {
        trainCarriageService.delete(id);
        return new CommonResp<>();
    }

}
