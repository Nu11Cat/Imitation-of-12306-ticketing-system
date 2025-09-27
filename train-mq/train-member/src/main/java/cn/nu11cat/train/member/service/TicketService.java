package cn.nu11cat.train.member.service;

import cn.nu11cat.train.common.req.MemberTicketReq;
import cn.nu11cat.train.common.resp.PageResp;
import cn.nu11cat.train.member.entity.Ticket;
import cn.nu11cat.train.member.req.TicketQueryReq;
import cn.nu11cat.train.member.resp.TicketQueryResp;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 车票 服务类
 * </p>
 *
 * @author nu11cat
 * @since 2025-09-27
 */
public interface TicketService extends IService<Ticket> {

    void save(MemberTicketReq req) throws Exception;

    PageResp<TicketQueryResp> queryList(TicketQueryReq req);

    void delete(Long id);
}
