package cn.nu11cat.train.member.service;

import cn.hutool.core.collection.CollUtil;
import cn.nu11cat.train.member.domain.Member;
import cn.nu11cat.train.member.domain.MemberExample;
import cn.nu11cat.train.member.mapper.MemberMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MemberService {

    @Resource
    private MemberMapper memberMapper;

    public long count() {
        return memberMapper.countByExample(null);
    }

    /**
     * 注册新会员
     * @param mobile
     * @return
     */
    public long registerMember(String mobile) {
        //手机号查重
        MemberExample memberExample = new MemberExample();
        memberExample.createCriteria().andMobileEqualTo(mobile);
        List<Member> members = memberMapper.selectByExample(memberExample);
        if(CollUtil.isNotEmpty(members)) {
            //return members.get(0).getId();
            throw new RuntimeException("手机号已注册");
        }
        //新增会员
        Member member1 = new Member();
        member1.setId(System.currentTimeMillis());
        member1.setMobile(mobile);
        memberMapper.insert(member1);
        return member1.getId();
    }

}
