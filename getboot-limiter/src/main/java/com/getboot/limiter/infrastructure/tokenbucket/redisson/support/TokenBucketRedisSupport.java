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
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateLimiterConfig;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;

import java.util.Objects;

/**
 * Redis 令牌桶限流底层支持。
 *
 * <p>基于 Redisson {@code RRateLimiter} 维护令牌桶状态，并在规则变化时刷新远端配置。</p>
 *
 * @author qiheng
 */
public class TokenBucketRedisSupport {

    private static final String TOKEN_BUCKET_KEY_SUFFIX = ":bucket";

    private final RedissonClient redissonClient;
    private final String keyPrefix;

    public TokenBucketRedisSupport(RedissonClient redissonClient, String keyPrefix) {
        this.redissonClient = redissonClient;
        this.keyPrefix = keyPrefix;
    }

    public boolean tryAcquire(String limiterName, long rate, long interval, RateIntervalUnit intervalUnit, long permits) {
        RRateLimiter rateLimiter = getRateLimiter(limiterName);
        configureRate(rateLimiter, rate, interval, intervalUnit);
        return rateLimiter.tryAcquire(permits);
    }

    public boolean delete(String limiterName) {
        return getRateLimiter(limiterName).delete();
    }

    private void configureRate(RRateLimiter rateLimiter, long rate, long interval, RateIntervalUnit intervalUnit) {
        if (rateLimiter.trySetRate(RateType.OVERALL, rate, interval, intervalUnit)) {
            return;
        }
        RateLimiterConfig currentConfig = rateLimiter.getConfig();
        long expectedIntervalMillis = intervalUnit.toMillis(interval);
        if (currentConfig == null
                || currentConfig.getRateType() != RateType.OVERALL
                || !Objects.equals(currentConfig.getRate(), rate)
                || !Objects.equals(currentConfig.getRateInterval(), expectedIntervalMillis)) {
            rateLimiter.setRate(RateType.OVERALL, rate, interval, intervalUnit);
        }
    }

    private RRateLimiter getRateLimiter(String limiterName) {
        return redissonClient.getRateLimiter(buildBucketKey(limiterName));
    }

    private String buildBucketKey(String limiterName) {
        return keyPrefix + ":" + limiterName + TOKEN_BUCKET_KEY_SUFFIX;
    }
}
