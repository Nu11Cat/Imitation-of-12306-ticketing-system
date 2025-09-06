package cn.nu11cat.train.member.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.nu11cat.train.common.exception.BusinessException;
import cn.nu11cat.train.common.exception.BusinessExceptionEnum;
import cn.nu11cat.train.common.util.JwtUtil;
import cn.nu11cat.train.common.util.SnowUtil;
import cn.nu11cat.train.member.domain.Member;
import cn.nu11cat.train.member.domain.MemberExample;
import cn.nu11cat.train.member.mapper.MemberMapper;
import cn.nu11cat.train.member.req.MemberLoginReq;
import cn.nu11cat.train.member.req.MemberRegisterReq;
import cn.nu11cat.train.member.req.MemberSendCodeReq;
import cn.nu11cat.train.member.resp.MemberLoginResp;
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
        Member membersget = getMemberByMobile(mobile);
        if(ObjectUtil.isNotNull(membersget)) {
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

    /**
     * 发送验证码
     * @param req
     */
    public void sendCode(MemberSendCodeReq req) {
        String mobile = req.getMobile();
        //手机号查重
        Member membersget = getMemberByMobile(mobile);
        //手机号不存在，插入一条记录
        if(ObjectUtil.isNull(membersget)) {
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

    /**
     * 登录
     * @param req
     * @return
     */
    public MemberLoginResp login(MemberLoginReq req) {
        String mobile = req.getMobile();
        String code = req.getCode();
        //手机号查重
        Member membersget = getMemberByMobile(mobile);
        //手机号不存在，插入一条记录
        if(ObjectUtil.isNull(membersget)) {
            throw new BusinessException(BusinessExceptionEnum.MEMBER_MOBILE_NOT_EXIST);
        }

        // 校验短信验证码
        if (!"8888".equals(code)) {
            throw new BusinessException(BusinessExceptionEnum.MEMBER_MOBILE_CODE_ERROR);
        }

        MemberLoginResp memberLoginResp = BeanUtil.copyProperties(membersget, MemberLoginResp.class);
        String token = JwtUtil.createToken(memberLoginResp.getId(), memberLoginResp.getMobile());
        memberLoginResp.setToken(token);
        return memberLoginResp;

    }

    /**
     * 查找会员表有无该手机号
     * @param mobile
     * @return
     */
    private Member getMemberByMobile(String mobile) {
        MemberExample memberExample = new MemberExample();
        memberExample.createCriteria().andMobileEqualTo(mobile);
        List<Member> members = memberMapper.selectByExample(memberExample);
        if(CollUtil.isEmpty(members)) {
            return null;
        } else {
            return members.get(0);
        }
    }

}
