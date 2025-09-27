package cn.nu11cat.train.member.service;

import cn.nu11cat.train.common.resp.PageResp;
import cn.nu11cat.train.member.entity.Member;
import cn.nu11cat.train.member.entity.Passenger;
import cn.nu11cat.train.member.req.*;
import cn.nu11cat.train.member.resp.MemberLoginResp;
import cn.nu11cat.train.member.resp.PassengerQueryResp;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface PassengerService extends IService<Passenger> {

    void savePassenger(PassengerSaveReq req);

    PageResp<PassengerQueryResp> queryList(PassengerQueryReq req);

    void delete(Long id);

    List<PassengerQueryResp> queryMine();

}
