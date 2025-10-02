package cn.nu11cat.train.business.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.ObjectUtil;
import cn.nu11cat.train.business.entity.TrainStation;
import cn.nu11cat.train.business.mapper.TrainStationMapper;
import cn.nu11cat.train.business.req.TrainStationQueryReq;
import cn.nu11cat.train.business.req.TrainStationSaveReq;
import cn.nu11cat.train.business.resp.TrainStationQueryResp;
import cn.nu11cat.train.business.service.TrainStationService;
import cn.nu11cat.train.common.exception.BusinessException;
import cn.nu11cat.train.common.exception.BusinessExceptionEnum;
import cn.nu11cat.train.common.resp.PageResp;
import cn.nu11cat.train.common.util.SnowUtil;
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
 * 火车车站 服务实现类
 * </p>
 *
 * @author nu11cat
 * @since 2025-09-29
 */
@Service
public class TrainStationServiceImpl extends ServiceImpl<TrainStationMapper, TrainStation> implements TrainStationService {

    private static final Logger LOG = LoggerFactory.getLogger(TrainStationService.class);

    @Resource
    private TrainStationMapper trainStationMapper;

    public void save(TrainStationSaveReq req) {
        DateTime now = DateTime.now();
        TrainStation trainStation = BeanUtil.copyProperties(req, TrainStation.class);

        if (ObjectUtil.isNull(trainStation.getId())) {

            // 唯一索引校验
            if (ObjectUtil.isNotEmpty(selectByUnique(req.getTrainCode(), req.getIndex()))) {
                throw new BusinessException(BusinessExceptionEnum.BUSINESS_TRAIN_STATION_INDEX_UNIQUE_ERROR);
            }
            if (ObjectUtil.isNotEmpty(selectByUnique(req.getTrainCode(), req.getName()))) {
                throw new BusinessException(BusinessExceptionEnum.BUSINESS_TRAIN_STATION_NAME_UNIQUE_ERROR);
            }

            trainStation.setId(SnowUtil.getSnowflakeNextId());
            trainStation.setCreateTime(now);
            trainStation.setUpdateTime(now);
            trainStationMapper.insert(trainStation);
        } else {
            trainStation.setUpdateTime(now);
            trainStationMapper.updateById(trainStation);
        }
    }

    private TrainStation selectByUnique(String trainCode, Integer index) {
        return trainStationMapper.selectOne(
                new LambdaQueryWrapper<TrainStation>()
                        .eq(TrainStation::getTrainCode, trainCode)
                        .eq(TrainStation::getIndex, index)
        );
    }

    private TrainStation selectByUnique(String trainCode, String name) {
        return trainStationMapper.selectOne(
                new LambdaQueryWrapper<TrainStation>()
                        .eq(TrainStation::getTrainCode, trainCode)
                        .eq(TrainStation::getName, name)
        );
    }

    public PageResp<TrainStationQueryResp> queryList(TrainStationQueryReq req) {
        LambdaQueryWrapper<TrainStation> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(TrainStation::getTrainCode, TrainStation::getIndex);

        if (ObjectUtil.isNotEmpty(req.getTrainCode())) {
            wrapper.eq(TrainStation::getTrainCode, req.getTrainCode());
        }

        LOG.info("查询页码：{}", req.getPage());
        LOG.info("每页条数：{}", req.getSize());
        Page<TrainStation> page = new Page<>(req.getPage(), req.getSize());
        Page<TrainStation> trainStationPage = trainStationMapper.selectPage(page, wrapper);

        List<TrainStationQueryResp> list = BeanUtil.copyToList(trainStationPage.getRecords(), TrainStationQueryResp.class);

        PageResp<TrainStationQueryResp> pageResp = new PageResp<>();
        pageResp.setTotal(trainStationPage.getTotal());
        pageResp.setList(list);
        return pageResp;
    }

    public void delete(Long id) {
        trainStationMapper.deleteById(id);
    }

    public List<TrainStation> selectByTrainCode(String trainCode) {
        return trainStationMapper.selectList(
                new LambdaQueryWrapper<TrainStation>()
                        .eq(TrainStation::getTrainCode, trainCode)
                        .orderByAsc(TrainStation::getIndex)
        );
    }

}