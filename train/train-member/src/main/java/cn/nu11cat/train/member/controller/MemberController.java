package cn.nu11cat.train.member.controller;

import cn.nu11cat.train.member.service.MemberService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/member")
public class MemberController {

    @Resource
    private MemberService memberService;

    @GetMapping("/count")
    public long count(){
        return memberService.count();
    }

    @PostMapping("/register")
    public Long register(String mobile){
        return memberService.registerMember(mobile);
    }

}
