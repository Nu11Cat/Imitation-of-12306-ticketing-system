package cn.nu11cat.train.business.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.nu11cat.train.business.entity.DailyTrain;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

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
    }

}
