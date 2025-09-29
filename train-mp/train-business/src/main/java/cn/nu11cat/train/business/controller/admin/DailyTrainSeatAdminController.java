package cn.nu11cat.train.business.controller.admin;

import cn.nu11cat.train.business.req.DailyTrainSeatQueryReq;
import cn.nu11cat.train.business.req.DailyTrainSeatSaveReq;
import cn.nu11cat.train.business.resp.DailyTrainSeatQueryResp;
import cn.nu11cat.train.business.service.DailyTrainSeatService;
import cn.nu11cat.train.common.resp.CommonResp;
import cn.nu11cat.train.common.resp.PageResp;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 每日座位 前端控制器
 * </p>
 *
 * @author nu11cat
 * @since 2025-09-29
 */
@RestController
@RequestMapping("/admin/daily-train-seat")
public class DailyTrainSeatAdminController {

    @Resource
    private DailyTrainSeatService dailyTrainSeatService;

    @PostMapping("/save")
    public CommonResp<Object> save(@Valid @RequestBody DailyTrainSeatSaveReq req) {
        dailyTrainSeatService.save(req);
        return new CommonResp<>();
    }

    @GetMapping("/query-list")
    public CommonResp<PageResp<DailyTrainSeatQueryResp>> queryList(@Valid DailyTrainSeatQueryReq req) {
        PageResp<DailyTrainSeatQueryResp> list = dailyTrainSeatService.queryList(req);
        return new CommonResp<>(list);
    }

    @DeleteMapping("/delete/{id}")
    public CommonResp<Object> delete(@PathVariable Long id) {
        dailyTrainSeatService.delete(id);
        return new CommonResp<>();
    }

}

