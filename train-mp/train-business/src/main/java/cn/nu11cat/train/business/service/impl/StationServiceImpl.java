package cn.nu11cat.train.business.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.ObjectUtil;
import cn.nu11cat.train.business.entity.Station;
import cn.nu11cat.train.business.mapper.StationMapper;
import cn.nu11cat.train.business.req.StationQueryReq;
import cn.nu11cat.train.business.req.StationSaveReq;
import cn.nu11cat.train.business.resp.StationQueryResp;
import cn.nu11cat.train.business.service.StationService;
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
 * 车站 服务实现类
 * </p>
 *
 * @author nu11cat
 * @since 2025-09-29
 */
@Service
public class StationServiceImpl extends ServiceImpl<StationMapper, Station> implements StationService {

    private static final Logger LOG = LoggerFactory.getLogger(StationService.class);

    @Resource
    private StationMapper stationMapper;

    public void save(StationSaveReq req) {
        DateTime now = DateTime.now();
        Station station = BeanUtil.copyProperties(req, Station.class);
        if (ObjectUtil.isNull(station.getId())) {

            // 保存之前，先校验唯一键是否存在
            Station stationDB = selectByUnique(req.getName());
            if (ObjectUtil.isNotEmpty(stationDB)) {
                throw new BusinessException(BusinessExceptionEnum.BUSINESS_STATION_NAME_UNIQUE_ERROR);
            }

            station.setId(SnowUtil.getSnowflakeNextId());
            station.setCreateTime(now);
            station.setUpdateTime(now);
            stationMapper.insert(station);
        } else {
            station.setUpdateTime(now);
            stationMapper.updateById(station);
        }
    }

    private Station selectByUnique(String name) {
        return stationMapper.selectOne(
                new LambdaQueryWrapper<Station>().eq(Station::getName, name)
        );
    }

    public PageResp<StationQueryResp> queryList(StationQueryReq req) {
        LambdaQueryWrapper<Station> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Station::getId);

        LOG.info("查询页码：{}", req.getPage());
        LOG.info("每页条数：{}", req.getSize());

        Page<Station> page = new Page<>(req.getPage(), req.getSize());
        Page<Station> stationPage = stationMapper.selectPage(page, wrapper);

        List<StationQueryResp> list = BeanUtil.copyToList(stationPage.getRecords(), StationQueryResp.class);

        PageResp<StationQueryResp> pageResp = new PageResp<>();
        pageResp.setTotal(stationPage.getTotal());
        pageResp.setList(list);
        return pageResp;
    }

    public void delete(Long id) {
        stationMapper.deleteById(id);
    }

    public List<StationQueryResp> queryAll() {
        LambdaQueryWrapper<Station> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(Station::getNamePinyin);

        List<Station> stationList = stationMapper.selectList(wrapper);
        return BeanUtil.copyToList(stationList, StationQueryResp.class);
    }
}
