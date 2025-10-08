package cn.nu11cat.train.business.config;

import jakarta.annotation.Resource;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BloomConfig {

    @Resource
    private RedissonClient redissonClient;

    @Bean
    public RBloomFilter<String> trainCodeBloomFilter(RedissonClient redissonClient) {
        RBloomFilter<String> bloomFilter = redissonClient.getBloomFilter("bloom:trainCode");
        bloomFilter.tryInit(100000, 0.001); // 容量10w，误判率0.1%
        return bloomFilter;
    }
}