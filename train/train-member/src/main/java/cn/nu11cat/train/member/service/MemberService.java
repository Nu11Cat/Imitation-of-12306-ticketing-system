package cn.nu11cat.train.member.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.RandomUtil;
import cn.nu11cat.train.common.exception.BusinessException;
import cn.nu11cat.train.common.exception.BusinessExceptionEnum;
import cn.nu11cat.train.common.util.SnowUtil;
import cn.nu11cat.train.member.domain.Member;
import cn.nu11cat.train.member.domain.MemberExample;
import cn.nu11cat.train.member.mapper.MemberMapper;
import cn.nu11cat.train.member.req.MemberRegisterReq;
import cn.nu11cat.train.member.req.MemberSendCodeReq;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MemberService {

    private static final Logger LOG = LoggerFactory.getLogger(MemberService.class);

    @Resource
    private MemberMapper memberMapper;

    public long count() {
        return memberMapper.countByExample(null);
    }

    /**
     * 注册新会员
     * @param req
     * @return
     */
    public long registerMember(MemberRegisterReq req) {
        String mobile = req.getMobile();
        //手机号查重
        MemberExample memberExample = new MemberExample();
        memberExample.createCriteria().andMobileEqualTo(mobile);
        List<Member> members = memberMapper.selectByExample(memberExample);
        if(CollUtil.isNotEmpty(members)) {
            //return members.get(0).getId();
            //throw new RuntimeException("手机号已注册");
            throw new BusinessException(BusinessExceptionEnum.MEMBER_MOBILE_EXIST);
        }
        //新增会员
        Member member1 = new Member();
        //member1.setId(IdUtil.getSnowflake(1,1).nextId());
        member1.setId(SnowUtil.getSnowflakeNextId());
        member1.setMobile(mobile);
        memberMapper.insert(member1);
        return member1.getId();
    }

    public void sendCode(MemberSendCodeReq req) {
        String mobile = req.getMobile();
        //手机号查重
        MemberExample memberExample = new MemberExample();
        memberExample.createCriteria().andMobileEqualTo(mobile);
        List<Member> members = memberMapper.selectByExample(memberExample);
        //手机号不存在，插入一条记录
        if(CollUtil.isEmpty(members)) {
            LOG.info("手机号不存在，插入一条记录");
            Member member1 = new Member();
            //member1.setId(IdUtil.getSnowflake(1,1).nextId());
            member1.setId(SnowUtil.getSnowflakeNextId());
            member1.setMobile(mobile);
            memberMapper.insert(member1);
        } else {
            LOG.info("手机号存在，不插入记录");
        }
        //生成验证码
        String code = RandomUtil.randomString(4);
        LOG.info("生成短信验证码：{}", code);
        // 保存短信记录表：手机号，短信验证码，有效期，是否已使用，业务类型(登录/忘记密码等)，发送时间，使用时间
        //略
        //对接短信通道
        //略
    }

}
