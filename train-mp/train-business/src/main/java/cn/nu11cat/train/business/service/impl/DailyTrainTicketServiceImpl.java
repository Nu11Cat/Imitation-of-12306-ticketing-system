package cn.nu11cat.train.business.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.EnumUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.nu11cat.train.business.entity.DailyTrain;
import cn.nu11cat.train.business.entity.DailyTrainTicket;
import cn.nu11cat.train.business.entity.TrainStation;
import cn.nu11cat.train.business.enums.SeatTypeEnum;
import cn.nu11cat.train.business.enums.TrainTypeEnum;
import cn.nu11cat.train.business.mapper.DailyTrainTicketMapper;
import cn.nu11cat.train.business.req.DailyTrainTicketQueryReq;
import cn.nu11cat.train.business.req.DailyTrainTicketSaveReq;
import cn.nu11cat.train.business.resp.DailyTrainTicketQueryResp;
import cn.nu11cat.train.business.service.DailyTrainSeatService;
import cn.nu11cat.train.business.service.DailyTrainTicketService;
import cn.nu11cat.train.business.service.TrainStationService;
import cn.nu11cat.train.common.resp.PageResp;
import cn.nu11cat.train.common.util.SnowUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * 余票信息 服务实现类
 * </p>
 *
 * @author nu11cat
 * @since 2025-09-29
 */
@Service
public class DailyTrainTicketServiceImpl extends ServiceImpl<DailyTrainTicketMapper, DailyTrainTicket> implements DailyTrainTicketService {

    private static final Logger LOG = LoggerFactory.getLogger(DailyTrainTicketService.class);

    @Resource
    private DailyTrainTicketMapper dailyTrainTicketMapper;

    @Resource
    private TrainStationService trainStationService;

    @Resource
    private DailyTrainSeatService dailyTrainSeatService;

    public void save(DailyTrainTicketSaveReq req) {
        DateTime now = DateTime.now();
        DailyTrainTicket dailyTrainTicket = BeanUtil.copyProperties(req, DailyTrainTicket.class);
        if (ObjectUtil.isNull(dailyTrainTicket.getId())) {
            dailyTrainTicket.setId(SnowUtil.getSnowflakeNextId());
            dailyTrainTicket.setCreateTime(now);
            dailyTrainTicket.setUpdateTime(now);
            dailyTrainTicketMapper.insert(dailyTrainTicket);
        } else {
            dailyTrainTicket.setUpdateTime(now);
            dailyTrainTicketMapper.updateById(dailyTrainTicket);
        }
    }

    @Cacheable(value = "DailyTrainTicketService.queryList")
    public PageResp<DailyTrainTicketQueryResp> queryList(DailyTrainTicketQueryReq req) {
        LambdaQueryWrapper<DailyTrainTicket> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(DailyTrainTicket::getId);

        if (ObjUtil.isNotNull(req.getDate())) {
            wrapper.eq(DailyTrainTicket::getDate, req.getDate());
        }
        if (ObjUtil.isNotEmpty(req.getTrainCode())) {
            wrapper.eq(DailyTrainTicket::getTrainCode, req.getTrainCode());
        }
        if (ObjUtil.isNotEmpty(req.getStart())) {
            wrapper.eq(DailyTrainTicket::getStart, req.getStart());
        }
        if (ObjUtil.isNotEmpty(req.getEnd())) {
            wrapper.eq(DailyTrainTicket::getEnd, req.getEnd());
        }

        LOG.info("查询页码：{}", req.getPage());
        LOG.info("每页条数：{}", req.getSize());
        Page<DailyTrainTicket> page = new Page<>(req.getPage(), req.getSize());
        Page<DailyTrainTicket> seatPage = dailyTrainTicketMapper.selectPage(page, wrapper);

        List<DailyTrainTicketQueryResp> list = BeanUtil.copyToList(seatPage.getRecords(), DailyTrainTicketQueryResp.class);
        PageResp<DailyTrainTicketQueryResp> pageResp = new PageResp<>();
        pageResp.setTotal(seatPage.getTotal());
        pageResp.setList(list);
        return pageResp;
    }

    public void delete(Long id) {
        dailyTrainTicketMapper.deleteById(id);
    }

    @Transactional
    public void genDaily(DailyTrain dailyTrain, Date date, String trainCode) {
        LOG.info("生成日期【{}】车次【{}】的余票信息开始", DateUtil.formatDate(date), trainCode);

        // 删除某日某车次的余票信息
        dailyTrainTicketMapper.delete(new LambdaQueryWrapper<DailyTrainTicket>()
                .eq(DailyTrainTicket::getDate, date)
                .eq(DailyTrainTicket::getTrainCode, trainCode)
        );

        List<TrainStation> stationList = trainStationService.selectByTrainCode(trainCode);
        if (CollUtil.isEmpty(stationList)) {
            LOG.info("该车次没有车站基础数据，生成该车次的余票信息结束");
            return;
        }

        DateTime now = DateTime.now();
        for (int i = 0; i < stationList.size(); i++) {
            TrainStation trainStationStart = stationList.get(i);
            BigDecimal sumKM = BigDecimal.ZERO;

            for (int j = i + 1; j < stationList.size(); j++) {
                TrainStation trainStationEnd = stationList.get(j);
                sumKM = sumKM.add(trainStationEnd.getKm());

                DailyTrainTicket ticket = new DailyTrainTicket();
                ticket.setId(SnowUtil.getSnowflakeNextId());
                ticket.setDate(date);
                ticket.setTrainCode(trainCode);
                ticket.setStart(trainStationStart.getName());
                ticket.setStartPinyin(trainStationStart.getNamePinyin());
                ticket.setStartTime(trainStationStart.getOutTime());
                ticket.setStartIndex(trainStationStart.getIndex());
                ticket.setEnd(trainStationEnd.getName());
                ticket.setEndPinyin(trainStationEnd.getNamePinyin());
                ticket.setEndTime(trainStationEnd.getInTime());
                ticket.setEndIndex(trainStationEnd.getIndex());

                int ydz = dailyTrainSeatService.countSeat(date, trainCode, SeatTypeEnum.YDZ.getCode());
                int edz = dailyTrainSeatService.countSeat(date, trainCode, SeatTypeEnum.EDZ.getCode());
                int rw = dailyTrainSeatService.countSeat(date, trainCode, SeatTypeEnum.RW.getCode());
                int yw = dailyTrainSeatService.countSeat(date, trainCode, SeatTypeEnum.YW.getCode());

                BigDecimal priceRate = EnumUtil.getFieldBy(TrainTypeEnum::getPriceRate, TrainTypeEnum::getCode, dailyTrain.getType());

                ticket.setYdz(ydz);
                ticket.setYdzPrice(sumKM.multiply(SeatTypeEnum.YDZ.getPrice()).multiply(priceRate).setScale(2, RoundingMode.HALF_UP));
                ticket.setEdz(edz);
                ticket.setEdzPrice(sumKM.multiply(SeatTypeEnum.EDZ.getPrice()).multiply(priceRate).setScale(2, RoundingMode.HALF_UP));
                ticket.setRw(rw);
                ticket.setRwPrice(sumKM.multiply(SeatTypeEnum.RW.getPrice()).multiply(priceRate).setScale(2, RoundingMode.HALF_UP));
                ticket.setYw(yw);
                ticket.setYwPrice(sumKM.multiply(SeatTypeEnum.YW.getPrice()).multiply(priceRate).setScale(2, RoundingMode.HALF_UP));

                ticket.setCreateTime(now);
                ticket.setUpdateTime(now);
                dailyTrainTicketMapper.insert(ticket);
            }
        }

        LOG.info("生成日期【{}】车次【{}】的余票信息结束", DateUtil.formatDate(date), trainCode);
    }

    public DailyTrainTicket selectByUnique(Date date, String trainCode, String start, String end) {
        LambdaQueryWrapper<DailyTrainTicket> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DailyTrainTicket::getDate, date)
                .eq(DailyTrainTicket::getTrainCode, trainCode)
                .eq(DailyTrainTicket::getStart, start)
                .eq(DailyTrainTicket::getEnd, end);

        return dailyTrainTicketMapper.selectOne(wrapper);
    }
}