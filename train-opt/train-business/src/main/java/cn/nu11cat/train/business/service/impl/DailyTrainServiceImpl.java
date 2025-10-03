package cn.nu11cat.train.business.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.nu11cat.train.business.entity.DailyTrain;
import cn.nu11cat.train.business.entity.DailyTrainTicket;
import cn.nu11cat.train.business.entity.Train;
import cn.nu11cat.train.business.mapper.DailyTrainMapper;
import cn.nu11cat.train.business.req.DailyTrainQueryReq;
import cn.nu11cat.train.business.req.DailyTrainSaveReq;
import cn.nu11cat.train.business.resp.DailyTrainQueryResp;
import cn.nu11cat.train.business.service.*;
import cn.nu11cat.train.common.resp.PageResp;
import cn.nu11cat.train.common.util.SnowUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 每日车次 服务实现类
 * </p>
 *
 * @author nu11cat
 * @since 2025-09-29
 */
@Service
public class DailyTrainServiceImpl extends ServiceImpl<DailyTrainMapper, DailyTrain> implements DailyTrainService {

    private static final Logger LOG = LoggerFactory.getLogger(DailyTrainService.class);

    @Resource
    private DailyTrainMapper dailyTrainMapper;

    @Resource
    private TrainService trainService;

    @Resource
    private DailyTrainStationService dailyTrainStationService;

    @Resource
    private DailyTrainCarriageService dailyTrainCarriageService;

    @Resource
    private DailyTrainSeatService dailyTrainSeatService;

    @Resource
    private DailyTrainTicketService dailyTrainTicketService;

    @Resource
    private SkTokenService skTokenService;

    @Autowired
    private RedissonClient redissonClient;

    public void save(DailyTrainSaveReq req) {
        DateTime now = DateTime.now();
        DailyTrain dailyTrain = BeanUtil.copyProperties(req, DailyTrain.class);

        if (ObjectUtil.isNull(dailyTrain.getId())) {
            dailyTrain.setId(SnowUtil.getSnowflakeNextId());
            dailyTrain.setCreateTime(now);
            dailyTrain.setUpdateTime(now);
            dailyTrainMapper.insert(dailyTrain);
        } else {
            dailyTrain.setUpdateTime(now);
            dailyTrainMapper.updateById(dailyTrain);
        }
    }

    public PageResp<DailyTrainQueryResp> queryList(DailyTrainQueryReq req) {
        LambdaQueryWrapper<DailyTrain> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(DailyTrain::getDate).orderByAsc(DailyTrain::getCode);

        if (ObjectUtil.isNotNull(req.getDate())) {
            wrapper.eq(DailyTrain::getDate, req.getDate());
        }
        if (ObjectUtil.isNotEmpty(req.getCode())) {
            wrapper.eq(DailyTrain::getCode, req.getCode());
        }

        LOG.info("查询页码：{}", req.getPage());
        LOG.info("每页条数：{}", req.getSize());
        Page<DailyTrain> page = new Page<>(req.getPage(), req.getSize());
        Page<DailyTrain> dailyTrainPage = dailyTrainMapper.selectPage(page, wrapper);

        List<DailyTrainQueryResp> list = BeanUtil.copyToList(dailyTrainPage.getRecords(), DailyTrainQueryResp.class);

        PageResp<DailyTrainQueryResp> pageResp = new PageResp<>();
        pageResp.setTotal(dailyTrainPage.getTotal());
        pageResp.setList(list);
        return pageResp;
    }

    public void delete(Long id) {
        dailyTrainMapper.deleteById(id);
    }

    /**
     * 生成某日所有车次信息，包括车次、车站、车厢、座位
     */
    public void genDaily(Date date) {
        List<Train> trainList = trainService.selectAll();
        if (CollUtil.isEmpty(trainList)) {
            LOG.info("没有车次基础数据，任务结束");
            return;
        }

        for (Train train : trainList) {
            genDailyTrain(date, train);
        }
    }

    @Transactional
    public void genDailyTrain(Date date, Train train) {
        LOG.info("生成日期【{}】车次【{}】的信息开始", DateUtil.formatDate(date), train.getCode());

        // 删除该车次已有的数据
        dailyTrainMapper.delete(
                new LambdaQueryWrapper<DailyTrain>()
                        .eq(DailyTrain::getDate, date)
                        .eq(DailyTrain::getCode, train.getCode())
        );

        // 生成该车次的数据
        DateTime now = DateTime.now();
        DailyTrain dailyTrain = BeanUtil.copyProperties(train, DailyTrain.class);
        dailyTrain.setId(SnowUtil.getSnowflakeNextId());
        dailyTrain.setCreateTime(now);
        dailyTrain.setUpdateTime(now);
        dailyTrain.setDate(date);
        dailyTrainMapper.insert(dailyTrain);

        // 生成车站、车厢、座位、余票和令牌余量数据
        dailyTrainStationService.genDaily(date, train.getCode());
        dailyTrainCarriageService.genDaily(date, train.getCode());
        dailyTrainSeatService.genDaily(date, train.getCode());
        dailyTrainTicketService.genDaily(dailyTrain, date, train.getCode());
        skTokenService.genDaily(date, train.getCode());

        LOG.info("生成日期【{}】车次【{}】的信息结束", DateUtil.formatDate(date), train.getCode());

//        // 查询生成的票列表
//        List<DailyTrainTicket> ticketList = dailyTrainTicketService.getByTrainAndDate(train.getCode(), date);
//
//        ticketList.forEach(this::initRedisStock);
//
//        LOG.info("初始化日期【{}】车次【{}】的redis库存结束");

        // 查询生成的票列表，并初始化 Redis 库存
        List<DailyTrainTicket> ticketList = dailyTrainTicketService.getByTrainAndDate(train.getCode(), date);

        ticketList.forEach(ticket -> {
            initRedisStock(ticket);

            // 打印每种座位类型的库存
            String dateStr = DateUtil.formatDate(ticket.getDate());
            String trainCode = ticket.getTrainCode();
            Map<String, Integer> seatTypeStockMap = Map.of(
                    "1", ticket.getYdz(),
                    "2", ticket.getEdz(),
                    "3", ticket.getRw(),
                    "4", ticket.getYw()
            );

            seatTypeStockMap.forEach((seatType, stock) -> {
                if (stock != null && stock > 0) {
                    String redisKey = "train_stock:" + dateStr + ":" + trainCode + ":" + seatType;
                    LOG.info("Redis库存状态: key={} stock={}", redisKey, redissonClient.getAtomicLong(redisKey).get());
                }
            });
        });

        LOG.info("日期【{}】车次【{}】的 Redis 库存初始化完成", DateUtil.formatDate(date), train.getCode());

    }

    /**
     * 初始化单张票 Redis 库存
     */
    public void initRedisStock(DailyTrainTicket ticket) {
        String dateStr = DateUtil.formatDate(ticket.getDate());
        String trainCode = ticket.getTrainCode();

        // 票种和对应库存映射
        Map<String, Integer> seatTypeStockMap = new HashMap<>();
        seatTypeStockMap.put("1", ticket.getYdz()); // 一等座
        seatTypeStockMap.put("2", ticket.getEdz()); // 二等座
        seatTypeStockMap.put("3", ticket.getRw());  // 软卧
        seatTypeStockMap.put("4", ticket.getYw());  // 硬卧

        for (Map.Entry<String, Integer> entry : seatTypeStockMap.entrySet()) {
            String seatTypeCode = entry.getKey();
            Integer stock = entry.getValue();
            if (stock == null || stock <= 0) continue; // 没有库存就跳过

            String redisKey = "train_stock:" + dateStr + ":" + trainCode + ":" + seatTypeCode;
            RAtomicLong stockCounter = redissonClient.getAtomicLong(redisKey);

//            boolean success = stockCounter.compareAndSet(0, stock);
//            if (success) {
//                LOG.info("初始化 Redis 库存: key={} stock={}", redisKey, stock);
//            } else {
//                LOG.info("Redis 已存在 key={}，当前库存={}", redisKey, stockCounter.get());
//            }
            //测试用
            stockCounter.set(stock);
            LOG.info("覆盖初始化 Redis 库存: key={} stock={}", redisKey, stock);

        }
    }


}
