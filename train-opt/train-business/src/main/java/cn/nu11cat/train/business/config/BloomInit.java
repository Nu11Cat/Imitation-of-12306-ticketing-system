package cn.nu11cat.train.business.config;

import cn.nu11cat.train.business.mapper.DailyTrainTicketMapper;
import cn.nu11cat.train.business.service.DailyTrainTicketService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BloomInit {

    private static final Logger LOG = LoggerFactory.getLogger(DailyTrainTicketService.class);

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private DailyTrainTicketMapper dailyTrainTicketMapper;

    @PostConstruct
    public void init() {
        RBloomFilter<String> bloomFilter = redissonClient.getBloomFilter("bloom:trainCode");
        bloomFilter.tryInit(100000, 0.001); // 容量10w，误判率0.1%
        List<String> allTrainCodes = dailyTrainTicketMapper.selectAllTrainCodes();
        LOG.info("布隆过滤器初始化,添加：{}", allTrainCodes);
        allTrainCodes.forEach(bloomFilter::add);
    }
}
