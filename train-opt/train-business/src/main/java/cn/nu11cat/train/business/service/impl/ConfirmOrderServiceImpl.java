package cn.nu11cat.train.business.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.EnumUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.nu11cat.train.business.dto.ConfirmOrderMQDto;
import cn.nu11cat.train.business.entity.ConfirmOrder;
import cn.nu11cat.train.business.entity.DailyTrainCarriage;
import cn.nu11cat.train.business.entity.DailyTrainSeat;
import cn.nu11cat.train.business.entity.DailyTrainTicket;
import cn.nu11cat.train.business.enums.ConfirmOrderStatusEnum;
import cn.nu11cat.train.business.enums.RedisKeyPreEnum;
import cn.nu11cat.train.business.enums.SeatColEnum;
import cn.nu11cat.train.business.enums.SeatTypeEnum;
import cn.nu11cat.train.business.mapper.ConfirmOrderMapper;
import cn.nu11cat.train.business.mapper.DailyTrainSeatMapper;
import cn.nu11cat.train.business.req.ConfirmOrderDoReq;
import cn.nu11cat.train.business.req.ConfirmOrderQueryReq;
import cn.nu11cat.train.business.req.ConfirmOrderTicketReq;
import cn.nu11cat.train.business.resp.ConfirmOrderQueryResp;
import cn.nu11cat.train.business.service.*;
import cn.nu11cat.train.common.exception.BusinessException;
import cn.nu11cat.train.common.exception.BusinessExceptionEnum;
import cn.nu11cat.train.common.resp.PageResp;
import cn.nu11cat.train.common.util.SnowUtil;
import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RLock;
import org.redisson.api.RScript;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 确认订单 服务实现类
 * </p>
 *
 * @author nu11cat
 * @since 2025-09-29
 */
@Service
public class ConfirmOrderServiceImpl extends ServiceImpl<ConfirmOrderMapper, ConfirmOrder> implements ConfirmOrderService {

    private static final Logger LOG = LoggerFactory.getLogger(ConfirmOrderService.class);

    @Resource
    private ConfirmOrderMapper confirmOrderMapper;

    @Resource
    private DailyTrainTicketService dailyTrainTicketService;

    @Resource
    private DailyTrainCarriageService dailyTrainCarriageService;

    @Resource
    private DailyTrainSeatService dailyTrainSeatService;

    @Resource
    private AfterConfirmOrderService afterConfirmOrderService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private SkTokenService skTokenService;

    public void save(ConfirmOrderDoReq req) {
        DateTime now = DateTime.now();
        ConfirmOrder confirmOrder = BeanUtil.copyProperties(req, ConfirmOrder.class);
        if (ObjectUtil.isNull(confirmOrder.getId())) {
            confirmOrder.setId(SnowUtil.getSnowflakeNextId());
            confirmOrder.setCreateTime(now);
            confirmOrder.setUpdateTime(now);
            confirmOrderMapper.insert(confirmOrder);
        } else {
            confirmOrder.setUpdateTime(now);
            confirmOrderMapper.updateById(confirmOrder);
        }
    }

    public PageResp<ConfirmOrderQueryResp> queryList(ConfirmOrderQueryReq req) {
        LambdaQueryWrapper<ConfirmOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(ConfirmOrder::getId);

        LOG.info("查询页码：{}", req.getPage());
        LOG.info("每页条数：{}", req.getSize());
        Page<ConfirmOrder> page = new Page<>(req.getPage(), req.getSize());
        Page<ConfirmOrder> orderPage = confirmOrderMapper.selectPage(page, wrapper);

        List<ConfirmOrderQueryResp> list = BeanUtil.copyToList(orderPage.getRecords(), ConfirmOrderQueryResp.class);
        PageResp<ConfirmOrderQueryResp> pageResp = new PageResp<>();
        pageResp.setTotal(orderPage.getTotal());
        pageResp.setList(list);
        return pageResp;
    }

    public void delete(Long id) {
        confirmOrderMapper.deleteById(id);
    }

    //@SentinelResource("doConfirm")
    @SentinelResource(value = "doConfirm", blockHandler = "doConfirmBlock")
    public void doConfirm(ConfirmOrderMQDto dto) {
        MDC.put("LOG_ID", dto.getLogId());
        LOG.info("异步出票开始：{}", dto);

        // Redisson 分布式锁 key
        String lockKey = RedisKeyPreEnum.CONFIRM_ORDER + "-" + DateUtil.formatDate(dto.getDate()) + "-" + dto.getTrainCode();
        RLock lock = redissonClient.getLock(lockKey);

        boolean locked = false;
        try {
            // 尝试获取锁，最多等待5秒，锁10秒自动释放
            locked = lock.tryLock(5, 10, TimeUnit.SECONDS);
            if (!locked) {
                LOG.info("未获取到锁，可能有其他线程在处理订单，直接返回");
                return;
            }

            LOG.info("抢到锁，开始处理订单，lockKey：{}", lockKey);

            while (true) {
                // 取确认订单表的记录，同日期车次，状态是I，分页处理，每次取N条
                LambdaQueryWrapper<ConfirmOrder> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(ConfirmOrder::getDate, dto.getDate())
                        .eq(ConfirmOrder::getTrainCode, dto.getTrainCode())
                        .eq(ConfirmOrder::getStatus, ConfirmOrderStatusEnum.INIT.getCode())
                        .orderByAsc(ConfirmOrder::getId);
                Page<ConfirmOrder> page = new Page<>(1, 5);
                Page<ConfirmOrder> orderPage = confirmOrderMapper.selectPage(page, wrapper);
                List<ConfirmOrder> list = orderPage.getRecords();

                if (CollUtil.isEmpty(list)) {
                    LOG.info("没有需要处理的订单，结束循环");
                    break;
                } else {
                    LOG.info("本次处理{}条订单", list.size());
                }

                list.forEach(this::processOrder);

            }
        } catch (InterruptedException e) {
            LOG.error("获取分布式锁异常", e);
            Thread.currentThread().interrupt();
        } finally {
            LOG.info("购票流程结束，释放锁！lockKey：{}", lockKey);
            if (locked && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    // 处理单个订单的方法
    private void processOrder(ConfirmOrder confirmOrder) {
        ConfirmOrderDoReq req = convertToReq(confirmOrder);
        String seatTypeCode = req.getTickets().get(0).getSeatTypeCode();
        String dateStr = DateUtil.formatDate(req.getDate()); // 统一日期格式
        String redisKey = "train_stock:" + dateStr + ":" + req.getTrainCode() + ":" + seatTypeCode;

//        RAtomicLong stockCounter = redissonClient.getAtomicLong(redisKey);
//        long stock = stockCounter.get();
//
//        LOG.info("处理订单前 Redis 库存状态: confirmOrderId={}, redisKey={}, seatTypeCode={}, stock={}",
//                confirmOrder.getId(), redisKey, seatTypeCode, stock);
//
//        if (stock <= 0) {
//            LOG.info("Redis库存不足，订单失败，confirmOrderId={}", confirmOrder.getId());
//            confirmOrder.setStatus(ConfirmOrderStatusEnum.EMPTY.getCode());
//            updateStatus(confirmOrder);
//            return;
//        }
//
//        long remainStock = stockCounter.decrementAndGet();
//        if (remainStock < 0) {
//            stockCounter.incrementAndGet(); // 回滚
//            LOG.info("Redis库存不足（并发修正），订单失败，confirmOrderId={}", confirmOrder.getId());
//            confirmOrder.setStatus(ConfirmOrderStatusEnum.EMPTY.getCode());
//            updateStatus(confirmOrder);
//            return;
//        }
//
//        LOG.info("Redis库存扣减成功，订单ID={}, 剩余库存={}", confirmOrder.getId(), remainStock);

        // 使用Lua脚本原子化扣减库存
        String script =
                "local stock = tonumber(redis.call('GET', KEYS[1])) " +
                        "if stock > 0 then " +
                        "    redis.call('DECR', KEYS[1]) " +
                        "    return 1 " +
                        "else " +
                        "    return 0 " +
                        "end";
        Long success = redissonClient.getScript().eval(
                RScript.Mode.READ_WRITE,
                script,
                RScript.ReturnType.INTEGER,
                Collections.singletonList(redisKey)
        );

        if (success == 0) {
            LOG.info("Redis库存不足，订单失败");
            confirmOrder.setStatus(ConfirmOrderStatusEnum.EMPTY.getCode());
            updateStatus(confirmOrder);
            return;
        }

        try {
            // 从数据库查出余票记录
            DailyTrainTicket dailyTrainTicket = dailyTrainTicketService.selectByUnique(
                    req.getDate(), req.getTrainCode(), req.getStart(), req.getEnd());

            // 数据库库存扣减，快速校验总库存
            //只是让流程更“安全”和“快速失败”，属于优化或冗余保护层
            //reduceTickets(req, dailyTrainTicket);

            // 选座
            List<DailyTrainSeat> finalSeatList = selectSeats(req, dailyTrainTicket);

            // 事务处理：修改座位售卖状态、余票表、订单表
            afterConfirmOrderService.afterDoConfirm(dailyTrainTicket, finalSeatList, req.getTickets(), confirmOrder);

        } catch (BusinessException e) {
            //stockCounter.incrementAndGet(); // 回滚 Redis
            redissonClient.getAtomicLong(redisKey).incrementAndGet();
            if (e.getE().equals(BusinessExceptionEnum.CONFIRM_ORDER_TICKET_COUNT_ERROR)) {
                LOG.info("本订单余票不足，继续下一个订单，confirmOrderId={}", confirmOrder.getId());
                confirmOrder.setStatus(ConfirmOrderStatusEnum.EMPTY.getCode());
                updateStatus(confirmOrder);
            } else {
                confirmOrder.setStatus(ConfirmOrderStatusEnum.INIT.getCode());
                updateStatus(confirmOrder);
                throw e;
            }
        } catch (Exception e) {
            //stockCounter.incrementAndGet(); // 回滚 Redis
            redissonClient.getAtomicLong(redisKey).incrementAndGet();
            confirmOrder.setStatus(ConfirmOrderStatusEnum.INIT.getCode());
            updateStatus(confirmOrder);
            LOG.error("订单处理异常，已回滚Redis库存，confirmOrderId={}", confirmOrder.getId(), e);
            throw new BusinessException(BusinessExceptionEnum.CONFIRM_ORDER_EXCEPTION);
        }
    }


    private List<DailyTrainSeat> selectSeats(ConfirmOrderDoReq req, DailyTrainTicket dailyTrainTicket) {
        List<DailyTrainSeat> finalSeatList = new ArrayList<>();
        List<ConfirmOrderTicketReq> tickets = req.getTickets();

        if (StrUtil.isNotBlank(tickets.get(0).getSeat())) {
            // 有选座
            List<SeatColEnum> colEnumList = SeatColEnum.getColsByType(tickets.get(0).getSeatTypeCode());
            List<String> referSeatList = new ArrayList<>();
            for (int i = 1; i <= 2; i++) {
                for (SeatColEnum seatColEnum : colEnumList) {
                    referSeatList.add(seatColEnum.getCode() + i);
                }
            }

            List<Integer> absoluteOffsetList = new ArrayList<>();
            for (ConfirmOrderTicketReq ticketReq : tickets) {
                int index = referSeatList.indexOf(ticketReq.getSeat());
                absoluteOffsetList.add(index);
            }

            List<Integer> offsetList = new ArrayList<>();
            for (Integer index : absoluteOffsetList) {
                offsetList.add(index - absoluteOffsetList.get(0));
            }

            // 调用 getSeat
            getSeat(finalSeatList,
                    req.getDate(),
                    req.getTrainCode(),
                    tickets.get(0).getSeatTypeCode(),
                    tickets.get(0).getSeat().substring(0, 1),
                    offsetList,
                    dailyTrainTicket.getStartIndex(),
                    dailyTrainTicket.getEndIndex()
            );

        } else {
            // 没选座
            for (ConfirmOrderTicketReq ticketReq : tickets) {
                getSeat(finalSeatList,
                        req.getDate(),
                        req.getTrainCode(),
                        ticketReq.getSeatTypeCode(),
                        null,
                        null,
                        dailyTrainTicket.getStartIndex(),
                        dailyTrainTicket.getEndIndex()
                );
            }
        }

        return finalSeatList;
    }

    private ConfirmOrderDoReq convertToReq(ConfirmOrder confirmOrder) {
        ConfirmOrderDoReq req = new ConfirmOrderDoReq();
        req.setMemberId(confirmOrder.getMemberId());
        req.setDate(confirmOrder.getDate());
        req.setTrainCode(confirmOrder.getTrainCode());
        req.setStart(confirmOrder.getStart());
        req.setEnd(confirmOrder.getEnd());
        req.setDailyTrainTicketId(confirmOrder.getDailyTrainTicketId());
        req.setTickets(JSON.parseArray(confirmOrder.getTickets(), ConfirmOrderTicketReq.class));
        req.setImageCode("");
        req.setImageCodeToken("");
        req.setLogId("");
        return req;
    }

    public void updateStatus(ConfirmOrder confirmOrder) {
        ConfirmOrder update = new ConfirmOrder();
        update.setId(confirmOrder.getId());
        update.setUpdateTime(new Date());
        update.setStatus(confirmOrder.getStatus());
        confirmOrderMapper.updateById(update);
    }

    private void getSeat(List<DailyTrainSeat> finalSeatList, Date date, String trainCode, String seatTyp, String column, List<Integer> offsetList, Integer startIndex, Integer endIndex) {
        List<DailyTrainCarriage> carriageList = dailyTrainCarriageService.selectBySeatType(date, trainCode, seatTyp);
        List<DailyTrainSeat> getSeatList;
        LOG.info("共查出 {} 个符合条件的车厢", carriageList.size());

        for (DailyTrainCarriage dailyTrainCarriage : carriageList) {
            LOG.info("开始从车厢{}选座", dailyTrainCarriage.getIndex());
            getSeatList = new ArrayList<>();
            List<DailyTrainSeat> seatList = dailyTrainSeatService.selectByCarriage(date, trainCode, dailyTrainCarriage.getIndex());
            LOG.info("车厢{}的座位数：{}", dailyTrainCarriage.getIndex(), seatList.size());

            for (int i = 0; i < seatList.size(); i++) {
                DailyTrainSeat dailyTrainSeat = seatList.get(i);
                String col = dailyTrainSeat.getColIndex();
                Integer seatIndex = dailyTrainSeat.getCarriageSeatIndex();

                boolean alreadyChooseFlag = finalSeatList.stream().anyMatch(f -> f.getId().equals(dailyTrainSeat.getId()));
                if (alreadyChooseFlag) {
                    LOG.info("座位{}被选中过，不能重复选中，继续判断下一个座位", seatIndex);
                    continue;
                }

                if(StrUtil.isNotBlank(column) && !column.equals(col)) {
                    LOG.info("座位{}列值不对，继续判断下一个座位，当前列值：{}，目标列值：{}", seatIndex, col, column);
                    continue;
                }

                boolean isChoose = calSell(dailyTrainSeat, startIndex, endIndex);
                if (isChoose) {
                    getSeatList.add(dailyTrainSeat);
                } else {
                    continue;
                }

                boolean isGetAllOffsetSeat = true;
                if (CollUtil.isNotEmpty(offsetList)) {
                    LOG.info("有偏移值：{}，校验便宜的座位是否可选", offsetList);
                    for (int j = 1; j < offsetList.size(); j++) {
                        int nextIndex = i + offsetList.get(j);
                        if (nextIndex >= seatList.size()) {
                            LOG.info("座位{}不可选，偏移后的索引超出了这个车箱的座位数", nextIndex);
                            isGetAllOffsetSeat = false;
                            break;
                        }
                        DailyTrainSeat nextSeat = seatList.get(nextIndex);
                        if (calSell(nextSeat, startIndex, endIndex)) {
                            LOG.info("座位{}被选中", nextSeat.getCarriageSeatIndex());
                            getSeatList.add(nextSeat);
                        } else {
                            LOG.info("座位{}不可选", nextSeat.getCarriageSeatIndex());
                            isGetAllOffsetSeat = false;
                            break;
                        }
                    }
                }
                if (!isGetAllOffsetSeat) {
                    getSeatList = new ArrayList<>();
                    continue;
                }

                finalSeatList.addAll(getSeatList);
                return;
            }
        }
    }

    private boolean calSell(DailyTrainSeat dailyTrainSeat, Integer startIndex, Integer endIndex) {
        String sell = dailyTrainSeat.getSell();
        String sellPart = sell.substring(startIndex, endIndex);
        if (Integer.parseInt(sellPart) > 0) {
            LOG.info("座位{}在本次车站区间{}~{}已售过票,不可选中该座位", dailyTrainSeat.getCarriageSeatIndex(), startIndex, endIndex);
            return false;
        } else {
            LOG.info("座位{}在本次车站区间{}~{}未售过票,可选中该座位", dailyTrainSeat.getCarriageSeatIndex(), startIndex, endIndex);
            String curSell = StrUtil.fillBefore(sellPart.replace('0','1'), '0', endIndex);
            curSell = StrUtil.fillAfter(curSell, '0', sell.length());
            int newSellInt = NumberUtil.binaryToInt(curSell) | NumberUtil.binaryToInt(sell);
            String newSell = StrUtil.fillBefore(NumberUtil.getBinaryStr(newSellInt), '0', sell.length());
            LOG.info("座位{}被选中,原售票信息:{},车站区间:{}~{},即:{},最终售票信息:{}",
                    dailyTrainSeat.getCarriageSeatIndex(), sell, startIndex, endIndex, curSell, newSell);
            dailyTrainSeat.setSell(newSell);
            return true;
        }
    }

    private static void reduceTickets(ConfirmOrderDoReq req, DailyTrainTicket dailyTrainTicket) {
        for (ConfirmOrderTicketReq ticketReq : req.getTickets()) {
            SeatTypeEnum seatTypeEnum = EnumUtil.getBy(SeatTypeEnum::getCode, ticketReq.getSeatTypeCode());
            switch (seatTypeEnum) {
                case YDZ -> {
                    int countLeft = dailyTrainTicket.getYdz() - 1;
                    if (countLeft < 0) throw new BusinessException(BusinessExceptionEnum.CONFIRM_ORDER_TICKET_COUNT_ERROR);
                    dailyTrainTicket.setYdz(countLeft);
                }
                case EDZ -> {
                    int countLeft = dailyTrainTicket.getEdz() - 1;
                    if (countLeft < 0) throw new BusinessException(BusinessExceptionEnum.CONFIRM_ORDER_TICKET_COUNT_ERROR);
                    dailyTrainTicket.setEdz(countLeft);
                }
                case RW -> {
                    int countLeft = dailyTrainTicket.getRw() - 1;
                    if (countLeft < 0) throw new BusinessException(BusinessExceptionEnum.CONFIRM_ORDER_TICKET_COUNT_ERROR);
                    dailyTrainTicket.setRw(countLeft);
                }
                case YW -> {
                    int countLeft = dailyTrainTicket.getYw() - 1;
                    if (countLeft < 0) throw new BusinessException(BusinessExceptionEnum.CONFIRM_ORDER_TICKET_COUNT_ERROR);
                    dailyTrainTicket.setYw(countLeft);
                }
            }
        }
    }
    /**
     * 降级方法，需包含限流方法的所有参数和BlockException参数
     * @param req
     * @param e
     */
    public void doConfirmBlock(ConfirmOrderDoReq req, BlockException e) {
        LOG.info("购票请求被限流：{}", req);
        throw new BusinessException(BusinessExceptionEnum.CONFIRM_ORDER_FLOW_EXCEPTION);
    }

    public Integer queryLineCount(Long id) {
        ConfirmOrder confirmOrder = confirmOrderMapper.selectById(id);
        ConfirmOrderStatusEnum statusEnum = EnumUtil.getBy(ConfirmOrderStatusEnum::getCode, confirmOrder.getStatus());
        int result = switch (statusEnum) {
            case PENDING -> 0;
            case SUCCESS -> -1;
            case FAILURE -> -2;
            case EMPTY -> -3;
            case CANCEL -> -4;
            case INIT -> 999;
        };

        if (result == 999) {
            LambdaQueryWrapper<ConfirmOrder> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ConfirmOrder::getDate, confirmOrder.getDate())
                    .eq(ConfirmOrder::getTrainCode, confirmOrder.getTrainCode())
                    .lt(ConfirmOrder::getCreateTime, confirmOrder.getCreateTime())
                    .in(ConfirmOrder::getStatus, List.of(ConfirmOrderStatusEnum.INIT.getCode(), ConfirmOrderStatusEnum.PENDING.getCode()));
            return Math.toIntExact(confirmOrderMapper.selectCount(wrapper));
        } else {
            return result;
        }
    }

    public Integer cancel(Long id) {
        LambdaUpdateWrapper<ConfirmOrder> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(ConfirmOrder::getId, id)
                .eq(ConfirmOrder::getStatus, ConfirmOrderStatusEnum.INIT.getCode());
        ConfirmOrder update = new ConfirmOrder();
        update.setStatus(ConfirmOrderStatusEnum.CANCEL.getCode());
        return confirmOrderMapper.update(update, wrapper);
    }

}