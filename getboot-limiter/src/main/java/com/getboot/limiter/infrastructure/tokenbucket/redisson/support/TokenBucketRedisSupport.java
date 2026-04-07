/*
 * Copyright (c) 2026 qiheng. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.getboot.limiter.infrastructure.tokenbucket.redisson.support;

import org.redisson.api.RRateLimiter;
import org.redisson.api.RateLimiterConfig;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;

import java.time.Duration;
import java.util.Objects;

/**
 * Redis 令牌桶限流底层支持。
 *
 * <p>基于 Redisson {@code RRateLimiter} 维护令牌桶状态，并在规则变化时刷新远端配置。</p>
 *
 * @author qiheng
 */
public class TokenBucketRedisSupport {

    /**
     * 令牌桶 key 后缀。
     */
    private static final String TOKEN_BUCKET_KEY_SUFFIX = ":bucket";

    /**
     * Redisson 客户端。
     */
    private final RedissonClient redissonClient;

    /**
     * Redis key 前缀。
     */
    private final String keyPrefix;

    /**
     * 创建令牌桶 Redis 支撑组件。
     *
     * @param redissonClient Redisson 客户端
     * @param keyPrefix Redis key 前缀
     */
    public TokenBucketRedisSupport(RedissonClient redissonClient, String keyPrefix) {
        this.redissonClient = redissonClient;
        this.keyPrefix = keyPrefix;
    }

    /**
     * 尝试获取指定数量令牌。
     *
     * @param limiterName 限流器名称
     * @param rate 速率阈值
     * @param intervalDuration 时间窗口时长
     * @param permits 令牌数量
     * @return 是否获取成功
     */
    public boolean tryAcquire(String limiterName, long rate, Duration intervalDuration, long permits) {
        RRateLimiter rateLimiter = getRateLimiter(limiterName);
        configureRate(rateLimiter, rate, intervalDuration);
        return rateLimiter.tryAcquire(permits);
    }

    /**
     * 删除令牌桶状态。
     *
     * @param limiterName 限流器名称
     * @return 是否删除成功
     */
    public boolean delete(String limiterName) {
        return getRateLimiter(limiterName).delete();
    }

    /**
     * 按需刷新远端令牌桶速率配置。
     *
     * @param rateLimiter Redisson 令牌桶
     * @param rate 速率阈值
     * @param intervalDuration 时间窗口时长
     */
    private void configureRate(RRateLimiter rateLimiter, long rate, Duration intervalDuration) {
        if (rateLimiter.trySetRate(RateType.OVERALL, rate, intervalDuration)) {
            return;
        }
        RateLimiterConfig currentConfig = rateLimiter.getConfig();
        long expectedIntervalMillis = intervalDuration.toMillis();
        if (currentConfig == null
                || currentConfig.getRateType() != RateType.OVERALL
                || !Objects.equals(currentConfig.getRate(), rate)
                || !Objects.equals(currentConfig.getRateInterval(), expectedIntervalMillis)) {
            rateLimiter.setRate(RateType.OVERALL, rate, intervalDuration);
        }
    }

    /**
     * 获取指定限流器对应的 Redisson 令牌桶。
     *
     * @param limiterName 限流器名称
     * @return Redisson 令牌桶
     */
    private RRateLimiter getRateLimiter(String limiterName) {
        return redissonClient.getRateLimiter(buildBucketKey(limiterName));
    }

    /**
     * 构建令牌桶 Redis key。
     *
     * @param limiterName 限流器名称
     * @return Redis key
     */
    private String buildBucketKey(String limiterName) {
        return keyPrefix + ":" + limiterName + TOKEN_BUCKET_KEY_SUFFIX;
    }
}
