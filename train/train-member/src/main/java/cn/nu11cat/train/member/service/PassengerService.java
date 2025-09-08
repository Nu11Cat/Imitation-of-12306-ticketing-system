package cn.nu11cat.train.member.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.ObjectUtil;
import cn.nu11cat.train.common.context.LoginMemberContext;
import cn.nu11cat.train.common.util.SnowUtil;
import cn.nu11cat.train.member.domain.Passenger;
import cn.nu11cat.train.member.domain.PassengerExample;
import cn.nu11cat.train.member.mapper.PassengerMapper;
import cn.nu11cat.train.member.req.PassengerQueryReq;
import cn.nu11cat.train.member.req.PassengerSaveReq;
import cn.nu11cat.train.member.resp.PassengerQueryResp;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PassengerService {

    @Resource
    private PassengerMapper passengerMapper;

    /**
     * 新增/修改乘车人
     * @param req
     */
    public void savePassenger(PassengerSaveReq req){
        DateTime now = DateTime.now();
        Passenger passenger = BeanUtil.copyProperties(req, Passenger.class);
        passenger.setMemberId(LoginMemberContext.getId());
        passenger.setId(SnowUtil.getSnowflakeNextId());
        passenger.setCreateTime(now);
        passenger.setUpdateTime(now);
        passengerMapper.insert(passenger);
    }

    public List<PassengerQueryResp> queryList(PassengerQueryReq req){
        PassengerExample passengerExample = new PassengerExample();
        PassengerExample.Criteria criteria = passengerExample.createCriteria();
        if(ObjectUtil.isNotNull(req.getMemberId())){
            criteria.andMemberIdEqualTo(req.getMemberId());
        }
        List<Passenger> passengerList1 = passengerMapper.selectByExample(passengerExample);
        return BeanUtil.copyToList(passengerList1, PassengerQueryResp.class);
    }

}
