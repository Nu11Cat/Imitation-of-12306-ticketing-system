package cn.nu11cat.train.business.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.ObjectUtil;
import cn.nu11cat.train.business.entity.TrainCarriage;
import cn.nu11cat.train.business.enums.SeatColEnum;
import cn.nu11cat.train.business.mapper.TrainCarriageMapper;
import cn.nu11cat.train.business.req.TrainCarriageQueryReq;
import cn.nu11cat.train.business.req.TrainCarriageSaveReq;
import cn.nu11cat.train.business.resp.TrainCarriageQueryResp;
import cn.nu11cat.train.business.service.TrainCarriageService;
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
 * 火车车厢 服务实现类
 * </p>
 *
 * @author nu11cat
 * @since 2025-09-29
 */
@Service
public class TrainCarriageServiceImpl extends ServiceImpl<TrainCarriageMapper, TrainCarriage> implements TrainCarriageService {

    private static final Logger LOG = LoggerFactory.getLogger(TrainCarriageService.class);

    @Resource
    private TrainCarriageMapper trainCarriageMapper;

    public void save(TrainCarriageSaveReq req) {
        DateTime now = DateTime.now();

        // 自动计算列数和总座位数
        List<SeatColEnum> seatColEnums = SeatColEnum.getColsByType(req.getSeatType());
        req.setColCount(seatColEnums.size());
        req.setSeatCount(req.getColCount() * req.getRowCount());

        TrainCarriage trainCarriage = BeanUtil.copyProperties(req, TrainCarriage.class);
        if (ObjectUtil.isNull(trainCarriage.getId())) {

            // 唯一索引校验
            if (ObjectUtil.isNotEmpty(selectByUnique(req.getTrainCode(), req.getIndex()))) {
                throw new BusinessException(BusinessExceptionEnum.BUSINESS_TRAIN_CARRIAGE_INDEX_UNIQUE_ERROR);
            }

            trainCarriage.setId(SnowUtil.getSnowflakeNextId());
            trainCarriage.setCreateTime(now);
            trainCarriage.setUpdateTime(now);
            trainCarriageMapper.insert(trainCarriage);
        } else {
            trainCarriage.setUpdateTime(now);
            trainCarriageMapper.updateById(trainCarriage);
        }
    }

    private TrainCarriage selectByUnique(String trainCode, Integer index) {
        return trainCarriageMapper.selectOne(
                new LambdaQueryWrapper<TrainCarriage>()
                        .eq(TrainCarriage::getTrainCode, trainCode)
                        .eq(TrainCarriage::getIndex, index)
        );
    }

    public PageResp<TrainCarriageQueryResp> queryList(TrainCarriageQueryReq req) {
        LambdaQueryWrapper<TrainCarriage> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(TrainCarriage::getTrainCode, TrainCarriage::getIndex);

        if (ObjectUtil.isNotEmpty(req.getTrainCode())) {
            wrapper.eq(TrainCarriage::getTrainCode, req.getTrainCode());
        }

        LOG.info("查询页码：{}", req.getPage());
        LOG.info("每页条数：{}", req.getSize());
        Page<TrainCarriage> page = new Page<>(req.getPage(), req.getSize());
        Page<TrainCarriage> trainCarriagePage = trainCarriageMapper.selectPage(page, wrapper);

        List<TrainCarriageQueryResp> list = BeanUtil.copyToList(trainCarriagePage.getRecords(), TrainCarriageQueryResp.class);

        PageResp<TrainCarriageQueryResp> pageResp = new PageResp<>();
        pageResp.setTotal(trainCarriagePage.getTotal());
        pageResp.setList(list);
        return pageResp;
    }

    public void delete(Long id) {
        trainCarriageMapper.deleteById(id);
    }

    public List<TrainCarriage> selectByTrainCode(String trainCode) {
        return trainCarriageMapper.selectList(
                new LambdaQueryWrapper<TrainCarriage>()
                        .eq(TrainCarriage::getTrainCode, trainCode)
                        .orderByAsc(TrainCarriage::getIndex)
        );
    }
}