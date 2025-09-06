package cn.nu11cat.train.member.controller;

import cn.nu11cat.train.common.resp.CommonResp;
import cn.nu11cat.train.member.req.PassengerSaveReq;
import cn.nu11cat.train.member.service.PassengerService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/passenger")
public class PassengerController {

    @Resource
    private PassengerService passengerService;


    @PostMapping("/save")
    public CommonResp<Object> save(@Valid @RequestBody PassengerSaveReq req){
        passengerService.savePassenger(req);
        return new CommonResp<>();
    }

}
