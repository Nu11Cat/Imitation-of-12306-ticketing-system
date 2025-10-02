package cn.nu11cat.train.business.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.nu11cat.train.business.entity.DailyTrainSeat;
import cn.nu11cat.train.business.entity.TrainSeat;
import cn.nu11cat.train.business.entity.TrainStation;
import cn.nu11cat.train.business.mapper.DailyTrainSeatMapper;
import cn.nu11cat.train.business.req.DailyTrainSeatQueryReq;
import cn.nu11cat.train.business.req.DailyTrainSeatSaveReq;
import cn.nu11cat.train.business.req.SeatSellReq;
import cn.nu11cat.train.business.resp.DailyTrainSeatQueryResp;
import cn.nu11cat.train.business.resp.SeatSellResp;
import cn.nu11cat.train.business.service.DailyTrainSeatService;
import cn.nu11cat.train.business.service.TrainSeatService;
import cn.nu11cat.train.business.service.TrainStationService;
import cn.nu11cat.train.common.resp.PageResp;
import cn.nu11cat.train.common.util.SnowUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * <p>
 * 每日座位 服务实现类
 * </p>
 *
 * @author nu11cat
 * @since 2025-09-29
 */
@Service
public class DailyTrainSeatServiceImpl extends ServiceImpl<DailyTrainSeatMapper, DailyTrainSeat> implements DailyTrainSeatService {

    private static final Logger LOG = LoggerFactory.getLogger(DailyTrainSeatService.class);

    @Resource
    private DailyTrainSeatMapper dailyTrainSeatMapper;

    @Resource
    private TrainSeatService trainSeatService;

    @Resource
    private TrainStationService trainStationService;

    public void save(DailyTrainSeatSaveReq req) {
        DateTime now = DateTime.now();
        DailyTrainSeat dailyTrainSeat = BeanUtil.copyProperties(req, DailyTrainSeat.class);
        if (ObjectUtil.isNull(dailyTrainSeat.getId())) {
            dailyTrainSeat.setId(SnowUtil.getSnowflakeNextId());
            dailyTrainSeat.setCreateTime(now);
            dailyTrainSeat.setUpdateTime(now);
            dailyTrainSeatMapper.insert(dailyTrainSeat);
        } else {
            dailyTrainSeat.setUpdateTime(now);
            dailyTrainSeatMapper.updateById(dailyTrainSeat);
        }
    }

    public PageResp<DailyTrainSeatQueryResp> queryList(DailyTrainSeatQueryReq req) {
        LambdaQueryWrapper<DailyTrainSeat> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(DailyTrainSeat::getDate)
                .orderByAsc(DailyTrainSeat::getTrainCode)
                .orderByAsc(DailyTrainSeat::getCarriageIndex)
                .orderByAsc(DailyTrainSeat::getCarriageSeatIndex);

        if (ObjectUtil.isNotEmpty(req.getTrainCode())) {
            wrapper.eq(DailyTrainSeat::getTrainCode, req.getTrainCode());
        }

        LOG.info("查询页码：{}", req.getPage());
        LOG.info("每页条数：{}", req.getSize());
        Page<DailyTrainSeat> page = new Page<>(req.getPage(), req.getSize());
        Page<DailyTrainSeat> seatPage = dailyTrainSeatMapper.selectPage(page, wrapper);

        List<DailyTrainSeatQueryResp> list = BeanUtil.copyToList(seatPage.getRecords(), DailyTrainSeatQueryResp.class);

        PageResp<DailyTrainSeatQueryResp> pageResp = new PageResp<>();
        pageResp.setTotal(seatPage.getTotal());
        pageResp.setList(list);
        return pageResp;
    }

    public void delete(Long id) {
        dailyTrainSeatMapper.deleteById(id);
    }

    @Transactional
    public void genDaily(Date date, String trainCode) {
        LOG.info("生成日期【{}】车次【{}】的座位信息开始", DateUtil.formatDate(date), trainCode);

        // 删除某日某车次座位信息
        dailyTrainSeatMapper.delete(new LambdaQueryWrapper<DailyTrainSeat>()
                .eq(DailyTrainSeat::getDate, date)
                .eq(DailyTrainSeat::getTrainCode, trainCode)
        );

        List<TrainStation> stationList = trainStationService.selectByTrainCode(trainCode);
        String sell = StrUtil.fillBefore("", '0', stationList.size() - 1);

        List<TrainSeat> seatList = trainSeatService.selectByTrainCode(trainCode);
        if (CollUtil.isEmpty(seatList)) {
            LOG.info("该车次没有座位基础数据，生成该车次的座位信息结束");
            return;
        }

        DateTime now = DateTime.now();
        List<DailyTrainSeat> dailySeats = seatList.stream().map(seat -> {
            DailyTrainSeat dailyTrainSeat = BeanUtil.copyProperties(seat, DailyTrainSeat.class);
            dailyTrainSeat.setId(SnowUtil.getSnowflakeNextId());
            dailyTrainSeat.setCreateTime(now);
            dailyTrainSeat.setUpdateTime(now);
            dailyTrainSeat.setDate(date);
            dailyTrainSeat.setSell(sell);
            return dailyTrainSeat;
        }).toList();

        dailySeats.forEach(dailyTrainSeatMapper::insert);

        LOG.info("生成日期【{}】车次【{}】的座位信息结束", DateUtil.formatDate(date), trainCode);
    }

    public int countSeat(Date date, String trainCode) {
        return countSeat(date, trainCode, null);
    }

    public int countSeat(Date date, String trainCode, String seatType) {
        LambdaQueryWrapper<DailyTrainSeat> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DailyTrainSeat::getDate, date)
                .eq(DailyTrainSeat::getTrainCode, trainCode);
        if (StrUtil.isNotBlank(seatType)) {
            wrapper.eq(DailyTrainSeat::getSeatType, seatType);
        }
        long count = dailyTrainSeatMapper.selectCount(wrapper);
        return count == 0L ? -1 : (int) count;
    }

    public List<DailyTrainSeat> selectByCarriage(Date date, String trainCode, Integer carriageIndex) {
        LambdaQueryWrapper<DailyTrainSeat> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DailyTrainSeat::getDate, date)
                .eq(DailyTrainSeat::getTrainCode, trainCode)
                .eq(DailyTrainSeat::getCarriageIndex, carriageIndex)
                .orderByAsc(DailyTrainSeat::getCarriageSeatIndex);
        return dailyTrainSeatMapper.selectList(wrapper);
    }

    public List<SeatSellResp> querySeatSell(SeatSellReq req) {
        Date date = req.getDate();
        String trainCode = req.getTrainCode();
        LOG.info("查询日期【{}】车次【{}】的座位销售信息", DateUtil.formatDate(date), trainCode);

        LambdaQueryWrapper<DailyTrainSeat> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DailyTrainSeat::getDate, date)
                .eq(DailyTrainSeat::getTrainCode, trainCode)
                .orderByAsc(DailyTrainSeat::getCarriageIndex)
                .orderByAsc(DailyTrainSeat::getCarriageSeatIndex);

        return BeanUtil.copyToList(dailyTrainSeatMapper.selectList(wrapper), SeatSellResp.class);
    }
}