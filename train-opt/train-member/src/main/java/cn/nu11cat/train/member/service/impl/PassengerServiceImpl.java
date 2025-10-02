package cn.nu11cat.train.member.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.ObjectUtil;
import cn.nu11cat.train.common.context.LoginMemberContext;
import cn.nu11cat.train.common.resp.PageResp;
import cn.nu11cat.train.common.util.SnowUtil;
import cn.nu11cat.train.member.entity.Passenger;
import cn.nu11cat.train.member.mapper.MemberMapper;
import cn.nu11cat.train.member.mapper.PassengerMapper;
import cn.nu11cat.train.member.req.*;
import cn.nu11cat.train.member.resp.PassengerQueryResp;
import cn.nu11cat.train.member.service.PassengerService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PassengerServiceImpl extends ServiceImpl<PassengerMapper, Passenger> implements PassengerService {

    private static final Logger LOG = LoggerFactory.getLogger(PassengerServiceImpl.class);

    @Resource
    private PassengerMapper passengerMapper;

    @Resource
    private MemberMapper memberMapper;

    @Override
    public void savePassenger(PassengerSaveReq req) {
        DateTime now = DateTime.now();
        Passenger passenger = BeanUtil.copyProperties(req, Passenger.class);
        if (ObjectUtil.isNull(passenger.getId())) {
            passenger.setMemberId(LoginMemberContext.getId());
            passenger.setId(SnowUtil.getSnowflakeNextId());
            passenger.setCreateTime(now);
            passenger.setUpdateTime(now);
            passengerMapper.insert(passenger);
        } else {
            passenger.setUpdateTime(now);
            passengerMapper.updateById(passenger);
        }
    }

    @Override
    public PageResp<PassengerQueryResp> queryList(PassengerQueryReq req) {
        // 1. 构造分页参数
        Page<Passenger> page = new Page<>(req.getPage(), req.getSize());

        // 2. 构造查询条件
        LambdaQueryWrapper<Passenger> wrapper = new LambdaQueryWrapper<>();
        if (req.getMemberId() != null) {
            wrapper.eq(Passenger::getMemberId, req.getMemberId());
        }

        // 3. 执行分页查询
        IPage<Passenger> passengerPage = passengerMapper.selectPage(page, wrapper);

        // 4. 转换成 DTO
        List<PassengerQueryResp> respList = passengerPage.getRecords().stream().map(passenger -> {
            PassengerQueryResp resp = new PassengerQueryResp();
            BeanUtils.copyProperties(passenger, resp);
            return resp;
        }).toList();

        // 5. 封装 PageResp
        PageResp<PassengerQueryResp> pageResp = new PageResp<>();
        pageResp.setTotal(passengerPage.getTotal());
        pageResp.setList(respList);

        return pageResp;
    }

    @Override
    public void delete(Long id) {
        passengerMapper.deleteById(id);
    }

    @Override
    public List<PassengerQueryResp> queryMine() {
        LambdaQueryWrapper<Passenger> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Passenger::getMemberId, LoginMemberContext.getId())
                .orderByAsc(Passenger::getName);
        List<Passenger> list = passengerMapper.selectList(wrapper);
        return BeanUtil.copyToList(list, PassengerQueryResp.class);
    }

}
