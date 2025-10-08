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
    private DailyTrainTicketMapper dailyTrainTicketMapper;

    @Resource
    private RBloomFilter<String> trainCodeBloomFilter;

    @PostConstruct
    public void init() {
        List<String> allTrainCodes = dailyTrainTicketMapper.selectAllTrainCodes();
        allTrainCodes.forEach(trainCodeBloomFilter::add);
        LOG.info("布隆过滤器初始化, 添加: {}", allTrainCodes);
    }
}
