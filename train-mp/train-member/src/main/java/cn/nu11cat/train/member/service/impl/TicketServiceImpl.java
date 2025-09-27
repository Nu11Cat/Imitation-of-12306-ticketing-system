package cn.nu11cat.train.member.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateTime;
import cn.nu11cat.train.common.req.MemberTicketReq;
import cn.nu11cat.train.common.resp.PageResp;
import cn.nu11cat.train.common.util.SnowUtil;
import cn.nu11cat.train.member.entity.Ticket;
import cn.nu11cat.train.member.mapper.TicketMapper;
import cn.nu11cat.train.member.req.TicketQueryReq;
import cn.nu11cat.train.member.resp.TicketQueryResp;
import cn.nu11cat.train.member.service.TicketService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 车票 服务实现类
 * </p>
 *
 * @author nu11cat
 * @since 2025-09-27
 */
@Service
public class TicketServiceImpl extends ServiceImpl<TicketMapper, Ticket> implements TicketService {
    private static final Logger LOG = LoggerFactory.getLogger(TicketServiceImpl.class);

    @Resource
    private TicketMapper ticketMapper;

    /**
     * 会员购买车票后新增保存
     */
    @Override
    public void save(MemberTicketReq req) throws Exception {
        //LOG.info("seata全局事务ID save: {}", RootContext.getXID());
        DateTime now = DateTime.now();
        Ticket ticket = BeanUtil.copyProperties(req, Ticket.class);
        ticket.setId(SnowUtil.getSnowflakeNextId());
        ticket.setCreateTime(now);
        ticket.setUpdateTime(now);
        ticketMapper.insert(ticket);
    }

    @Override
    public PageResp<TicketQueryResp> queryList(TicketQueryReq req) {
        LambdaQueryWrapper<Ticket> wrapper = new LambdaQueryWrapper<>();
        if (req.getMemberId() != null) {
            wrapper.eq(Ticket::getMemberId, req.getMemberId());
        }
        wrapper.orderByDesc(Ticket::getId);

        Page<Ticket> page = new Page<>(req.getPage(), req.getSize());
        Page<Ticket> ticketPage = ticketMapper.selectPage(page, wrapper);

        List<TicketQueryResp> list = ticketPage.getRecords().stream()
                .map(ticket -> BeanUtil.copyProperties(ticket, TicketQueryResp.class))
                .toList();

        PageResp<TicketQueryResp> pageResp = new PageResp<>();
        pageResp.setTotal(ticketPage.getTotal());
        pageResp.setList(list);
        return pageResp;
    }

    @Override
    public void delete(Long id) {
        ticketMapper.deleteById(id);
    }
}
