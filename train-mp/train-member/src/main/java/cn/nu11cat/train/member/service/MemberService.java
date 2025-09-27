package cn.nu11cat.train.member.service;

import cn.nu11cat.train.member.entity.Member;
import cn.nu11cat.train.member.req.MemberLoginReq;
import cn.nu11cat.train.member.req.MemberRegisterReq;
import cn.nu11cat.train.member.req.MemberSendCodeReq;
import cn.nu11cat.train.member.resp.MemberLoginResp;
import com.baomidou.mybatisplus.extension.service.IService;

public interface MemberService extends IService<Member> {
    long registerMember(MemberRegisterReq req);

    void sendCode(MemberSendCodeReq req);

    MemberLoginResp login(MemberLoginReq req);
}
