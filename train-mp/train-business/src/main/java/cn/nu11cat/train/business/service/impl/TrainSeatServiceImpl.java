package cn.nu11cat.train.business.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.nu11cat.train.business.entity.TrainCarriage;
import cn.nu11cat.train.business.entity.TrainSeat;
import cn.nu11cat.train.business.enums.SeatColEnum;
import cn.nu11cat.train.business.mapper.TrainSeatMapper;
import cn.nu11cat.train.business.req.TrainSeatQueryReq;
import cn.nu11cat.train.business.req.TrainSeatSaveReq;
import cn.nu11cat.train.business.resp.TrainSeatQueryResp;
import cn.nu11cat.train.business.service.TrainCarriageService;
import cn.nu11cat.train.business.service.TrainSeatService;
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

import java.util.List;

/**
 * <p>
 * 座位 服务实现类
 * </p>
 *
 * @author nu11cat
 * @since 2025-09-29
 */
@Service
public class TrainSeatServiceImpl extends ServiceImpl<TrainSeatMapper, TrainSeat> implements TrainSeatService {

    private static final Logger LOG = LoggerFactory.getLogger(TrainSeatService.class);

    @Resource
    private TrainSeatMapper trainSeatMapper;
    @Resource
    private TrainCarriageService trainCarriageService;

    public void save(TrainSeatSaveReq req) {
        DateTime now = DateTime.now();
        TrainSeat trainSeat = BeanUtil.copyProperties(req, TrainSeat.class);

        if (ObjectUtil.isNull(trainSeat.getId())) {
            trainSeat.setId(SnowUtil.getSnowflakeNextId());
            trainSeat.setCreateTime(now);
            trainSeat.setUpdateTime(now);
            trainSeatMapper.insert(trainSeat);
        } else {
            trainSeat.setUpdateTime(now);
            trainSeatMapper.updateById(trainSeat);
        }
    }

    public PageResp<TrainSeatQueryResp> queryList(TrainSeatQueryReq req) {
        LambdaQueryWrapper<TrainSeat> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(TrainSeat::getTrainCode, TrainSeat::getCarriageIndex, TrainSeat::getCarriageSeatIndex);

        if (ObjectUtil.isNotEmpty(req.getTrainCode())) {
            wrapper.eq(TrainSeat::getTrainCode, req.getTrainCode());
        }

        LOG.info("查询页码：{}", req.getPage());
        LOG.info("每页条数：{}", req.getSize());
        Page<TrainSeat> page = new Page<>(req.getPage(), req.getSize());
        Page<TrainSeat> trainSeatPage = trainSeatMapper.selectPage(page, wrapper);

        List<TrainSeatQueryResp> list = BeanUtil.copyToList(trainSeatPage.getRecords(), TrainSeatQueryResp.class);

        PageResp<TrainSeatQueryResp> pageResp = new PageResp<>();
        pageResp.setTotal(trainSeatPage.getTotal());
        pageResp.setList(list);
        return pageResp;
    }

    public void delete(Long id) {
        trainSeatMapper.deleteById(id);
    }

    @Transactional
    public void genTrainSeat(String trainCode) {
        DateTime now = DateTime.now();
        // 清空当前车次下的所有座位
        trainSeatMapper.delete(
                new LambdaQueryWrapper<TrainSeat>().eq(TrainSeat::getTrainCode, trainCode)
        );

        // 获取车厢列表
        List<TrainCarriage> carriageList = trainCarriageService.selectByTrainCode(trainCode);
        LOG.info("当前车次下的车厢数：{}", carriageList.size());

        for (TrainCarriage trainCarriage : carriageList) {
            Integer rowCount = trainCarriage.getRowCount();
            String seatType = trainCarriage.getSeatType();
            int seatIndex = 1;

            List<SeatColEnum> colEnumList = SeatColEnum.getColsByType(seatType);
            LOG.info("根据车厢类型筛选出的列：{}", colEnumList);

            for (int row = 1; row <= rowCount; row++) {
                for (SeatColEnum seatColEnum : colEnumList) {
                    TrainSeat trainSeat = new TrainSeat();
                    trainSeat.setId(SnowUtil.getSnowflakeNextId());
                    trainSeat.setTrainCode(trainCode);
                    trainSeat.setCarriageIndex(trainCarriage.getIndex());
                    trainSeat.setRowIndex(StrUtil.fillBefore(String.valueOf(row), '0', 2));
                    trainSeat.setColIndex(seatColEnum.getCode());
                    trainSeat.setSeatType(seatType);
                    trainSeat.setCarriageSeatIndex(seatIndex++);
                    trainSeat.setCreateTime(now);
                    trainSeat.setUpdateTime(now);
                    trainSeatMapper.insert(trainSeat);
                }
            }
        }
    }

    public List<TrainSeat> selectByTrainCode(String trainCode) {
        return trainSeatMapper.selectList(
                new LambdaQueryWrapper<TrainSeat>()
                        .eq(TrainSeat::getTrainCode, trainCode)
                        .orderByAsc(TrainSeat::getId)
        );
    }
}