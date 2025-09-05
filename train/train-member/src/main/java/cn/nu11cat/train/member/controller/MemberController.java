package cn.nu11cat.train.member.controller;

import cn.nu11cat.train.common.resp.CommonResp;
import cn.nu11cat.train.member.req.MemberLoginReq;
import cn.nu11cat.train.member.req.MemberRegisterReq;
import cn.nu11cat.train.member.req.MemberSendCodeReq;
import cn.nu11cat.train.member.service.MemberService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/member")
public class MemberController {

    @Resource
    private MemberService memberService;

    @GetMapping("/count")
    public CommonResp<Long> count(){
        Long count = memberService.count();
        return new CommonResp<>(count);
    }

    @PostMapping("/register")
    public CommonResp<Long> register(@Valid MemberRegisterReq req){
        long register = memberService.registerMember(req);
        return new CommonResp<>(register);
    }

    @PostMapping("/send-code")
    public CommonResp<Long> sendCode(@Valid @RequestBody MemberSendCodeReq req){
        memberService.sendCode(req);
        return new CommonResp<>();
    }

    @PostMapping("/login")
    public CommonResp<MemberLoginReq> login(@Valid @RequestBody MemberLoginReq req){
        MemberLoginReq resp = memberService.login(req);
        return new CommonResp<>(resp);
    }
}
