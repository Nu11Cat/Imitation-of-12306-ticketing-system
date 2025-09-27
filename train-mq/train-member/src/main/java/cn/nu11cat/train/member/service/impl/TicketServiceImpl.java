package cn.nu11cat.train.member.service.impl;

import cn.nu11cat.train.member.entity.Ticket;
import cn.nu11cat.train.member.mapper.TicketMapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 车票 服务实现类
 * </p>
 *
 * @author nu11cat
 * @since 2025-09-27
 */
@Service
public class TicketServiceImpl extends ServiceImpl<TicketMapper, Ticket> implements IService<Ticket> {

}
