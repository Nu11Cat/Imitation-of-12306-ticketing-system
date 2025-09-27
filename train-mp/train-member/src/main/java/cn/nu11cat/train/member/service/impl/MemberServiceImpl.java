package cn.nu11cat.train.member.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.nu11cat.train.common.exception.BusinessException;
import cn.nu11cat.train.common.exception.BusinessExceptionEnum;
import cn.nu11cat.train.common.util.JwtUtil;
import cn.nu11cat.train.common.util.SnowUtil;
import cn.nu11cat.train.member.entity.Member;
import cn.nu11cat.train.member.mapper.MemberMapper;
import cn.nu11cat.train.member.req.MemberLoginReq;
import cn.nu11cat.train.member.req.MemberRegisterReq;
import cn.nu11cat.train.member.req.MemberSendCodeReq;
import cn.nu11cat.train.member.resp.MemberLoginResp;
import cn.nu11cat.train.member.service.MemberService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MemberServiceImpl extends ServiceImpl<MemberMapper, Member> implements MemberService {

    private static final Logger LOG = LoggerFactory.getLogger(MemberServiceImpl.class);

    @Resource
    private MemberMapper memberMapper;

    /**
     * 注册新会员
     * @param req
     * @return
     */
    @Override
    public long registerMember(MemberRegisterReq req) {
        String mobile = req.getMobile();

        // 手机号查重
        Member member = getMemberByMobile(mobile);
        if (ObjectUtil.isNotNull(member)) {
            throw new BusinessException(BusinessExceptionEnum.MEMBER_MOBILE_EXIST);
        }

        // 新增会员
        Member newMember = new Member();
        newMember.setId(SnowUtil.getSnowflakeNextId());
        newMember.setMobile(mobile);
        memberMapper.insert(newMember);

        return newMember.getId();
    }

    /**
     * 发送验证码
     * @param req
     */
    @Override
    public void sendCode(MemberSendCodeReq req) {
        String mobile = req.getMobile();

        Member member = getMemberByMobile(mobile);
        if (ObjectUtil.isNull(member)) {
            LOG.info("手机号不存在，插入一条记录");
            Member newMember = new Member();
            newMember.setId(SnowUtil.getSnowflakeNextId());
            newMember.setMobile(mobile);
            memberMapper.insert(newMember);
        } else {
            LOG.info("手机号存在，不插入记录");
        }

        // 生成验证码
        String code = RandomUtil.randomString(4);
        LOG.info("生成短信验证码：{}", code);

        // TODO: 保存短信记录表、对接短信通道
    }

    /**
     * 登录
     * @param req
     * @return
     */
    @Override
    public MemberLoginResp login(MemberLoginReq req) {
        String mobile = req.getMobile();
        String code = req.getCode();

        Member member = getMemberByMobile(mobile);
        if (ObjectUtil.isNull(member)) {
            throw new BusinessException(BusinessExceptionEnum.MEMBER_MOBILE_NOT_EXIST);
        }

        // 校验短信验证码（这里先写死 8888）
        if (!"8888".equals(code)) {
            throw new BusinessException(BusinessExceptionEnum.MEMBER_MOBILE_CODE_ERROR);
        }

        // 复制属性
        MemberLoginResp resp = BeanUtil.copyProperties(member, MemberLoginResp.class);

        // 生成 token
        String token = JwtUtil.createToken(resp.getId(), resp.getMobile());
        resp.setToken(token);

        return resp;
    }

    /**
     * 查找会员表有无该手机号
     * @param mobile
     * @return
     */
    private Member getMemberByMobile(String mobile) {
        LambdaQueryWrapper<Member> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Member::getMobile, mobile);
        return memberMapper.selectOne(wrapper);
    }
}
