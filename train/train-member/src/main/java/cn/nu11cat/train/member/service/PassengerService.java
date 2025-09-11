package cn.nu11cat.train.member.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.ObjectUtil;
import cn.nu11cat.train.common.context.LoginMemberContext;
import cn.nu11cat.train.common.resp.PageResp;
import cn.nu11cat.train.common.util.SnowUtil;
import cn.nu11cat.train.member.domain.Member;
import cn.nu11cat.train.member.domain.MemberExample;
import cn.nu11cat.train.member.domain.Passenger;
import cn.nu11cat.train.member.domain.PassengerExample;
import cn.nu11cat.train.member.enums.PassengerTypeEnum;
import cn.nu11cat.train.member.mapper.MemberMapper;
import cn.nu11cat.train.member.mapper.PassengerMapper;
import cn.nu11cat.train.member.req.PassengerQueryReq;
import cn.nu11cat.train.member.req.PassengerSaveReq;
import cn.nu11cat.train.member.resp.PassengerQueryResp;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class PassengerService {

    private static final Logger LOG = LoggerFactory.getLogger(PassengerService.class);

    @Resource
    private PassengerMapper passengerMapper;

    @Resource
    private MemberMapper memberMapper;

    /**
     * 新增/修改乘车人
     * @param req
     */
    public void savePassenger(PassengerSaveReq req){
        DateTime now = DateTime.now();
        Passenger passenger = BeanUtil.copyProperties(req, Passenger.class);
        if(ObjectUtil.isNull(passenger.getId())){
            passenger.setMemberId(LoginMemberContext.getId());
            passenger.setId(SnowUtil.getSnowflakeNextId());
            passenger.setCreateTime(now);
            passenger.setUpdateTime(now);
            passengerMapper.insert(passenger);
        } else {
            passenger.setUpdateTime(now);
            passengerMapper.updateByPrimaryKey(passenger);
        }

    }

    /**
     * 获取乘车人信息列表
     * @param req
     * @return
     */
    public PageResp<PassengerQueryResp> queryList(PassengerQueryReq req){
        PassengerExample passengerExample = new PassengerExample();
        PassengerExample.Criteria criteria = passengerExample.createCriteria();
        if(ObjectUtil.isNotNull(req.getMemberId())){
            criteria.andMemberIdEqualTo(req.getMemberId());
        }
        PageHelper.startPage(req.getPage(), req.getSize());
        LOG.info("查询页码：{}", req.getPage());
        LOG.info("每页条数：{}", req.getSize());

        List<Passenger> passengerList1 = passengerMapper.selectByExample(passengerExample);

        PageInfo<Passenger> pageInfo = new PageInfo<>(passengerList1);
        LOG.info("总行数：{}", pageInfo.getTotal());
        LOG.info("总页数：{}", pageInfo.getPages());

        List<PassengerQueryResp> list = BeanUtil.copyToList(passengerList1, PassengerQueryResp.class);
        PageResp<PassengerQueryResp> pageResp = new PageResp<>();
        pageResp.setTotal(pageInfo.getTotal());
        pageResp.setList(list);
        return pageResp;
    }

    /**
     * 删除乘车人
     * @param id
     */
    public void delete(Long id) {
        passengerMapper.deleteByPrimaryKey(id);
    }

    /**
     * 查询我的所有乘客
     */
    public List<PassengerQueryResp> queryMine() {
        PassengerExample passengerExample = new PassengerExample();
        passengerExample.setOrderByClause("name asc");
        PassengerExample.Criteria criteria = passengerExample.createCriteria();
        criteria.andMemberIdEqualTo(LoginMemberContext.getId());
        List<Passenger> list = passengerMapper.selectByExample(passengerExample);
        return BeanUtil.copyToList(list, PassengerQueryResp.class);
    }

    /**
     * 初始化乘客，如果没有张三，就增加乘客张三，李四、王五同理，防止线上体验时乘客被删光
     */
    public void init() {
        DateTime now = DateTime.now();
        MemberExample memberExample = new MemberExample();
        memberExample.createCriteria().andMobileEqualTo("13000000000");
        List<Member> memberList = memberMapper.selectByExample(memberExample);
        Member member = memberList.get(0);

        List<Passenger> passengerList = new ArrayList<>();

        List<String> nameList = Arrays.asList("张三", "李四", "王五");
        for (String s : nameList) {
            Passenger passenger = new Passenger();
            passenger.setId(SnowUtil.getSnowflakeNextId());
            passenger.setMemberId(member.getId());
            passenger.setName(s);
            passenger.setIdCard("123456789123456789");
            passenger.setType(PassengerTypeEnum.ADULT.getCode());
            passenger.setCreateTime(now);
            passenger.setUpdateTime(now);
            passengerList.add(passenger);
        }

        for (Passenger passenger : passengerList) {
            PassengerExample passengerExample = new PassengerExample();
            passengerExample.createCriteria().andNameEqualTo(passenger.getName());
            long l = passengerMapper.countByExample(passengerExample);
            if (l == 0) {
                passengerMapper.insert(passenger);
            }
        }
    }

}
