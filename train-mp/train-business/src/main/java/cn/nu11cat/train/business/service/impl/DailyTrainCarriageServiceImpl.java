package cn.nu11cat.train.business.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.nu11cat.train.business.entity.DailyTrainCarriage;
import cn.nu11cat.train.business.entity.TrainCarriage;
import cn.nu11cat.train.business.enums.SeatColEnum;
import cn.nu11cat.train.business.mapper.DailyTrainCarriageMapper;
import cn.nu11cat.train.business.req.DailyTrainCarriageQueryReq;
import cn.nu11cat.train.business.req.DailyTrainCarriageSaveReq;
import cn.nu11cat.train.business.resp.DailyTrainCarriageQueryResp;
import cn.nu11cat.train.business.service.DailyTrainCarriageService;
import cn.nu11cat.train.business.service.TrainCarriageService;
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
 * 每日车厢 服务实现类
 * </p>
 *
 * @author nu11cat
 * @since 2025-09-29
 */
@Service
public class DailyTrainCarriageServiceImpl extends ServiceImpl<DailyTrainCarriageMapper, DailyTrainCarriage> implements DailyTrainCarriageService {

    private static final Logger LOG = LoggerFactory.getLogger(DailyTrainCarriageService.class);

    @Resource
    private DailyTrainCarriageMapper dailyTrainCarriageMapper;

    @Resource
    private TrainCarriageService trainCarriageService;

    public void save(DailyTrainCarriageSaveReq req) {
        DateTime now = DateTime.now();

        // 自动计算列数和总座位数
        List<SeatColEnum> seatColEnums = SeatColEnum.getColsByType(req.getSeatType());
        req.setColCount(seatColEnums.size());
        req.setSeatCount(req.getColCount() * req.getRowCount());

        DailyTrainCarriage dailyTrainCarriage = BeanUtil.copyProperties(req, DailyTrainCarriage.class);
        if (ObjectUtil.isNull(dailyTrainCarriage.getId())) {
            dailyTrainCarriage.setId(SnowUtil.getSnowflakeNextId());
            dailyTrainCarriage.setCreateTime(now);
            dailyTrainCarriage.setUpdateTime(now);
            dailyTrainCarriageMapper.insert(dailyTrainCarriage);
        } else {
            dailyTrainCarriage.setUpdateTime(now);
            dailyTrainCarriageMapper.updateById(dailyTrainCarriage);
        }
    }

    public PageResp<DailyTrainCarriageQueryResp> queryList(DailyTrainCarriageQueryReq req) {
        LambdaQueryWrapper<DailyTrainCarriage> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(DailyTrainCarriage::getDate)
                .orderByAsc(DailyTrainCarriage::getTrainCode)
                .orderByAsc(DailyTrainCarriage::getIndex);

        if (ObjectUtil.isNotNull(req.getDate())) {
            wrapper.eq(DailyTrainCarriage::getDate, req.getDate());
        }
        if (ObjectUtil.isNotEmpty(req.getTrainCode())) {
            wrapper.eq(DailyTrainCarriage::getTrainCode, req.getTrainCode());
        }

        LOG.info("查询页码：{}", req.getPage());
        LOG.info("每页条数：{}", req.getSize());
        Page<DailyTrainCarriage> page = new Page<>(req.getPage(), req.getSize());
        Page<DailyTrainCarriage> dailyTrainCarriagePage = dailyTrainCarriageMapper.selectPage(page, wrapper);

        List<DailyTrainCarriageQueryResp> list = BeanUtil.copyToList(dailyTrainCarriagePage.getRecords(), DailyTrainCarriageQueryResp.class);

        PageResp<DailyTrainCarriageQueryResp> pageResp = new PageResp<>();
        pageResp.setTotal(dailyTrainCarriagePage.getTotal());
        pageResp.setList(list);
        return pageResp;
    }

    public void delete(Long id) {
        dailyTrainCarriageMapper.deleteById(id);
    }

    @Transactional
    public void genDaily(Date date, String trainCode) {
        LOG.info("生成日期【{}】车次【{}】的车厢信息开始", DateUtil.formatDate(date), trainCode);

        // 删除某日某车次的车厢信息
        dailyTrainCarriageMapper.delete(new LambdaQueryWrapper<DailyTrainCarriage>()
                .eq(DailyTrainCarriage::getDate, date)
                .eq(DailyTrainCarriage::getTrainCode, trainCode)
        );

        // 查出某车次的所有车厢信息
        List<TrainCarriage> carriageList = trainCarriageService.selectByTrainCode(trainCode);
        if (CollUtil.isEmpty(carriageList)) {
            LOG.info("该车次没有车厢基础数据，生成该车次的车厢信息结束");
            return;
        }

        DateTime now = DateTime.now();
        List<DailyTrainCarriage> dailyTrainCarriages = carriageList.stream().map(trainCarriage -> {
            DailyTrainCarriage dailyTrainCarriage = BeanUtil.copyProperties(trainCarriage, DailyTrainCarriage.class);
            dailyTrainCarriage.setId(SnowUtil.getSnowflakeNextId());
            dailyTrainCarriage.setCreateTime(now);
            dailyTrainCarriage.setUpdateTime(now);
            dailyTrainCarriage.setDate(date);
            return dailyTrainCarriage;
        }).toList();

        dailyTrainCarriages.forEach(dailyTrainCarriageMapper::insert);

        LOG.info("生成日期【{}】车次【{}】的车厢信息结束", DateUtil.formatDate(date), trainCode);
    }

    public List<DailyTrainCarriage> selectBySeatType(Date date, String trainCode, String seatType) {
        LambdaQueryWrapper<DailyTrainCarriage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DailyTrainCarriage::getDate, date)
                .eq(DailyTrainCarriage::getTrainCode, trainCode)
                .eq(DailyTrainCarriage::getSeatType, seatType);
        return dailyTrainCarriageMapper.selectList(wrapper);
    }
}