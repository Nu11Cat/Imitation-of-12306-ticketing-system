package cn.nu11cat.train.business.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.nu11cat.train.business.entity.DailyTrainStation;
import cn.nu11cat.train.business.entity.TrainStation;
import cn.nu11cat.train.business.mapper.DailyTrainStationMapper;
import cn.nu11cat.train.business.req.DailyTrainStationQueryReq;
import cn.nu11cat.train.business.req.DailyTrainStationSaveReq;
import cn.nu11cat.train.business.resp.DailyTrainStationQueryResp;
import cn.nu11cat.train.business.service.DailyTrainStationService;
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
 * 每日车站 服务实现类
 * </p>
 *
 * @author nu11cat
 * @since 2025-09-29
 */
@Service
public class DailyTrainStationServiceImpl extends ServiceImpl<DailyTrainStationMapper, DailyTrainStation> implements DailyTrainStationService {

    private static final Logger LOG = LoggerFactory.getLogger(DailyTrainStationService.class);

    @Resource
    private DailyTrainStationMapper dailyTrainStationMapper;

    @Resource
    private TrainStationService trainStationService;

    public void save(DailyTrainStationSaveReq req) {
        DateTime now = DateTime.now();
        DailyTrainStation dailyTrainStation = BeanUtil.copyProperties(req, DailyTrainStation.class);
        if (ObjectUtil.isNull(dailyTrainStation.getId())) {
            dailyTrainStation.setId(SnowUtil.getSnowflakeNextId());
            dailyTrainStation.setCreateTime(now);
            dailyTrainStation.setUpdateTime(now);
            dailyTrainStationMapper.insert(dailyTrainStation);
        } else {
            dailyTrainStation.setUpdateTime(now);
            dailyTrainStationMapper.updateById(dailyTrainStation);
        }
    }

    public PageResp<DailyTrainStationQueryResp> queryList(DailyTrainStationQueryReq req) {
        LambdaQueryWrapper<DailyTrainStation> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(DailyTrainStation::getDate)
                .orderByAsc(DailyTrainStation::getTrainCode)
                .orderByAsc(DailyTrainStation::getIndex);

        if (ObjectUtil.isNotNull(req.getDate())) {
            wrapper.eq(DailyTrainStation::getDate, req.getDate());
        }
        if (ObjectUtil.isNotEmpty(req.getTrainCode())) {
            wrapper.eq(DailyTrainStation::getTrainCode, req.getTrainCode());
        }

        LOG.info("查询页码：{}", req.getPage());
        LOG.info("每页条数：{}", req.getSize());
        Page<DailyTrainStation> page = new Page<>(req.getPage(), req.getSize());
        Page<DailyTrainStation> dailyTrainStationPage = dailyTrainStationMapper.selectPage(page, wrapper);

        List<DailyTrainStationQueryResp> list = BeanUtil.copyToList(dailyTrainStationPage.getRecords(), DailyTrainStationQueryResp.class);

        PageResp<DailyTrainStationQueryResp> pageResp = new PageResp<>();
        pageResp.setTotal(dailyTrainStationPage.getTotal());
        pageResp.setList(list);
        return pageResp;
    }

    public void delete(Long id) {
        dailyTrainStationMapper.deleteById(id);
    }

    @Transactional
    public void genDaily(Date date, String trainCode) {
        LOG.info("生成日期【{}】车次【{}】的车站信息开始", DateUtil.formatDate(date), trainCode);

        // 删除某日某车次的车站信息
        dailyTrainStationMapper.delete(
                new LambdaQueryWrapper<DailyTrainStation>()
                        .eq(DailyTrainStation::getDate, date)
                        .eq(DailyTrainStation::getTrainCode, trainCode)
        );

        // 查出某车次的所有车站信息
        List<TrainStation> stationList = trainStationService.selectByTrainCode(trainCode);
        if (CollUtil.isEmpty(stationList)) {
            LOG.info("该车次没有车站基础数据，生成该车次的车站信息结束");
            return;
        }

        DateTime now = DateTime.now();
        List<DailyTrainStation> dailyTrainStations = stationList.stream().map(trainStation -> {
            DailyTrainStation dailyTrainStation = BeanUtil.copyProperties(trainStation, DailyTrainStation.class);
            dailyTrainStation.setId(SnowUtil.getSnowflakeNextId());
            dailyTrainStation.setCreateTime(now);
            dailyTrainStation.setUpdateTime(now);
            dailyTrainStation.setDate(date);
            return dailyTrainStation;
        }).toList();

        dailyTrainStations.forEach(dailyTrainStationMapper::insert);
        LOG.info("生成日期【{}】车次【{}】的车站信息结束", DateUtil.formatDate(date), trainCode);
    }

    /** 按车次查询车站数量 */
    public long countByTrainCode(Date date, String trainCode) {
        return dailyTrainStationMapper.selectCount(
                new LambdaQueryWrapper<DailyTrainStation>()
                        .eq(DailyTrainStation::getDate, date)
                        .eq(DailyTrainStation::getTrainCode, trainCode)
        );
    }

    /** 按车次日期查询车站列表 */
    public List<DailyTrainStationQueryResp> queryByTrain(Date date, String trainCode) {
        LambdaQueryWrapper<DailyTrainStation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DailyTrainStation::getDate, date)
                .eq(DailyTrainStation::getTrainCode, trainCode)
                .orderByAsc(DailyTrainStation::getIndex);

        List<DailyTrainStation> list = dailyTrainStationMapper.selectList(wrapper);
        return BeanUtil.copyToList(list, DailyTrainStationQueryResp.class);
    }
}
