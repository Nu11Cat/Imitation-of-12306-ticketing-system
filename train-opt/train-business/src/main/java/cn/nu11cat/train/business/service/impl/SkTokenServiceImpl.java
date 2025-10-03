package cn.nu11cat.train.business.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.nu11cat.train.business.entity.SkToken;
import cn.nu11cat.train.business.enums.RedisKeyPreEnum;
import cn.nu11cat.train.business.mapper.SkTokenMapper;
import cn.nu11cat.train.business.mapper.cust.SkTokenMapperCust;
import cn.nu11cat.train.business.req.SkTokenQueryReq;
import cn.nu11cat.train.business.req.SkTokenSaveReq;
import cn.nu11cat.train.business.resp.SkTokenQueryResp;
import cn.nu11cat.train.business.service.DailyTrainSeatService;
import cn.nu11cat.train.business.service.DailyTrainStationService;
import cn.nu11cat.train.business.service.SkTokenService;
import cn.nu11cat.train.common.resp.PageResp;
import cn.nu11cat.train.common.util.SnowUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 秒杀令牌 服务实现类
 * </p>
 *
 * @author nu11cat
 * @since 2025-09-29
 */
@Service
public class SkTokenServiceImpl extends ServiceImpl<SkTokenMapper, SkToken> implements SkTokenService {

    private static final Logger LOG = LoggerFactory.getLogger(SkTokenService.class);

    @Resource
    private SkTokenMapper skTokenMapper;

    @Resource
    private DailyTrainSeatService dailyTrainSeatService;

    @Resource
    private DailyTrainStationService dailyTrainStationService;

    @Resource
    private SkTokenMapperCust skTokenMapperCust;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String KEY_PREFIX = "SK_TOKEN_COUNT";

    /**
     * 初始化
     */
    public void genDaily(Date date, String trainCode) {
        LOG.info("删除日期【{}】车次【{}】的令牌记录", DateUtil.formatDate(date), trainCode);
        // 使用 MyBatis-Plus LambdaQueryWrapper 删除
        skTokenMapper.delete(new LambdaQueryWrapper<SkToken>()
                .eq(SkToken::getDate, date)
                .eq(SkToken::getTrainCode, trainCode));

        DateTime now = DateTime.now();
        SkToken skToken = new SkToken();
        skToken.setDate(date);
        skToken.setTrainCode(trainCode);
        skToken.setId(SnowUtil.getSnowflakeNextId());
        skToken.setCreateTime(now);
        skToken.setUpdateTime(now);

        int seatCount = dailyTrainSeatService.countSeat(date, trainCode);
        LOG.info("车次【{}】座位数：{}", trainCode, seatCount);

        long stationCount = dailyTrainStationService.countByTrainCode(date, trainCode);
        LOG.info("车次【{}】到站数：{}", trainCode, stationCount);

        int count = (int) (seatCount * stationCount); // 初始生成令牌数
        LOG.info("车次【{}】初始生成令牌数：{}", trainCode, count);
        skToken.setCount(count);

        skTokenMapper.insert(skToken);

        int ratePerSecond = 1500;
        initDailyToken(date, trainCode, count, ratePerSecond);
        LOG.info("令牌桶初始化，初始生成令牌数：{},每秒补充{}个", count, ratePerSecond);

    }

    public void save(SkTokenSaveReq req) {
        DateTime now = DateTime.now();
        SkToken skToken = BeanUtil.copyProperties(req, SkToken.class);
        if (ObjectUtil.isNull(skToken.getId())) {
            skToken.setId(SnowUtil.getSnowflakeNextId());
            skToken.setCreateTime(now);
            skToken.setUpdateTime(now);
            skTokenMapper.insert(skToken);
        } else {
            skToken.setUpdateTime(now);
            skTokenMapper.updateById(skToken); // MyBatis-Plus 替换 updateByPrimaryKey
        }
    }

    public PageResp<SkTokenQueryResp> queryList(SkTokenQueryReq req) {
        LOG.info("查询页码：{}", req.getPage());
        LOG.info("每页条数：{}", req.getSize());
        Page<SkToken> page = new Page<>(req.getPage(), req.getSize());

        LambdaQueryWrapper<SkToken> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(SkToken::getId);

        IPage<SkToken> resultPage = skTokenMapper.selectPage(page, queryWrapper);

        LOG.info("总行数：{}", resultPage.getTotal());
        LOG.info("总页数：{}", resultPage.getPages());

        List<SkTokenQueryResp> list = BeanUtil.copyToList(resultPage.getRecords(), SkTokenQueryResp.class);

        PageResp<SkTokenQueryResp> pageResp = new PageResp<>();
        pageResp.setTotal(resultPage.getTotal());
        pageResp.setList(list);
        return pageResp;
    }

    public void delete(Long id) {
        skTokenMapper.deleteById(id); // MyBatis-Plus 替换 deleteByPrimaryKey
    }

    /**
     * 校验令牌
     */
    public boolean validSkToken(Date date, String trainCode, Long memberId) {
        LOG.info("会员【{}】获取日期【{}】车次【{}】的令牌开始", memberId, DateUtil.formatDate(date), trainCode);

        // 令牌锁逻辑
        String lockKey = RedisKeyPreEnum.SK_TOKEN + "-" + DateUtil.formatDate(date) + "-" + trainCode + "-" + memberId;
        Boolean setIfAbsent = redisTemplate.opsForValue().setIfAbsent(lockKey, lockKey, 5, TimeUnit.SECONDS);
        if (Boolean.TRUE.equals(setIfAbsent)) {
            LOG.info("恭喜，抢到令牌锁了！lockKey：{}", lockKey);
        } else {
            LOG.info("很遗憾，没抢到令牌锁！lockKey：{}", lockKey);
            return false;
        }

        String skTokenCountKey = RedisKeyPreEnum.SK_TOKEN_COUNT + "-" + DateUtil.formatDate(date) + "-" + trainCode;
        Object skTokenCount = redisTemplate.opsForValue().get(skTokenCountKey);
        if (skTokenCount != null) {
            LOG.info("缓存中有该车次令牌大闸的key：{}", skTokenCountKey);
            Long count = redisTemplate.opsForValue().decrement(skTokenCountKey, 1);
            if (count < 0L) {
                LOG.error("获取令牌失败：{}", skTokenCountKey);
                return false;
            } else {
                LOG.info("获取令牌后，令牌余数：{}", count);
                redisTemplate.expire(skTokenCountKey, 60, TimeUnit.SECONDS);
                if (count % 5 == 0) {
                    skTokenMapperCust.decrease(date, trainCode, 5);
                }
                return true;
            }
        } else {
            LOG.info("缓存中没有该车次令牌大闸的key：{}", skTokenCountKey);
            // MyBatis-Plus 查询
            SkToken skToken = skTokenMapper.selectOne(new LambdaQueryWrapper<SkToken>()
                    .eq(SkToken::getDate, date)
                    .eq(SkToken::getTrainCode, trainCode));
            if (skToken == null) {
                LOG.info("找不到日期【{}】车次【{}】的令牌记录", DateUtil.formatDate(date), trainCode);
                return false;
            }

            if (skToken.getCount() <= 0) {
                LOG.info("日期【{}】车次【{}】的令牌余量为0", DateUtil.formatDate(date), trainCode);
                return false;
            }

            int count = skToken.getCount() - 1;
            skToken.setCount(count);
            LOG.info("将该车次令牌大闸放入缓存中，key: {}， count: {}", skTokenCountKey, count);
            redisTemplate.opsForValue().set(skTokenCountKey, String.valueOf(count), 60, TimeUnit.SECONDS);
            return true;
        }
    }
    /**
     * 初始化令牌桶（日志添加）
     */
    public void initDailyToken(Date date, String trainCode, int capacity, int ratePerSecond) {
        String key = buildKey(date, trainCode);

        redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            connection.stringCommands().set(key.getBytes(), String.valueOf(capacity).getBytes());
            connection.stringCommands().set((key + ":capacity").getBytes(),
                    String.valueOf(capacity).getBytes());
            connection.stringCommands().set((key + ":rate").getBytes(),
                    String.valueOf(ratePerSecond).getBytes());
            connection.stringCommands().set((key + ":time").getBytes(),
                    String.valueOf(System.currentTimeMillis()).getBytes());
            return null;
        });

        LOG.info("[令牌桶初始化] key={}, 容量={}, 速率={}/秒", key, capacity, ratePerSecond);
    }

    /**
     * 获取令牌（日志增强）
     */
    public boolean acquireToken(Date date, String trainCode, int need) {
        String key = buildKey(date, trainCode);
        String capKey = key + ":capacity";
        String rateKey = key + ":rate";
        long now = System.currentTimeMillis();

        String luaScript =
                "local key = KEYS[1]\n" +
                        "local capKey = KEYS[2]\n" +
                        "local rateKey = KEYS[3]\n" +
                        "local need = tonumber(ARGV[1])\n" +
                        "local now = tonumber(ARGV[2])\n" +

                        // 获取配置
                        "local capacity = tonumber(redis.call('GET', capKey))\n" +
                        "local rate = tonumber(redis.call('GET', rateKey))\n" +
                        "local lastTime = tonumber(redis.call('GET', key..':time') or now)\n" +

                        // 计算补充令牌
                        "local timePassed = math.max(0, now - lastTime)\n" +
                        "local tokensToAdd = math.floor(timePassed * rate / 1000)\n" +

                        // 更新令牌
                        "local current = tonumber(redis.call('GET', key) or 0)\n" +
                        "current = math.min(capacity, current + tokensToAdd)\n" +
                        "redis.call('SET', key..':time', now)\n" +

                        // 扣减逻辑
                        "if current >= need then\n" +
                        "    redis.call('SET', key, current - need)\n" +
                        "    return 1\n" +
                        "else\n" +
                        "    return 0\n" +
                        "end";

        Long result = redisTemplate.execute(
                new DefaultRedisScript<>(luaScript, Long.class),
                Arrays.asList(key, capKey, rateKey),
                String.valueOf(need),
                String.valueOf(now)
        );

        boolean success = result != null && result == 1;
        if (success) {
            LOG.info("[获取令牌成功] key={}, 消耗={}, 剩余={}",
                    key, need, getCurrentTokens(key) - need);
        } else {
            LOG.info("[获取令牌失败] key={}, 需求={}, 当前剩余={}",
                    key, need, getCurrentTokens(key));
        }
        return success;
    }

    /**
     * 查询当前令牌数（私有方法，用于日志）
     */
    private long getCurrentTokens(String key) {
        String val = redisTemplate.opsForValue().get(key);
        return val == null ? 0 : Long.parseLong(val);
    }

    /**
     * 释放令牌（下单失败时归还）
     */
    public void releaseToken(Date date, String trainCode, int count) {
        String key = buildKey(date, trainCode);
        redisTemplate.opsForValue().increment(key, count);
        LOG.info("释放令牌: key={}, count={}", key, count);
    }

    private String buildKey(Date date, String trainCode) {
        return KEY_PREFIX + "-" + DateUtil.formatDate(date) + "-" + trainCode;
    }

}
