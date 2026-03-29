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
package com.getboot.limiter.infrastructure.slidingwindow.redisson.support;

import com.getboot.limiter.api.model.LimiterRule;
import com.getboot.limiter.api.properties.SlidingWindowRateLimiterProperties;
import com.getboot.limiter.api.registry.RateLimiterRegistry;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 滑动窗口限流器注册表。
 *
 * <p>统一管理命名限流器配置，并通过 Redis 有序集合实现滑动窗口判断。</p>
 *
 * @author qiheng
 */
@Slf4j
@RequiredArgsConstructor
public class RedissonSlidingWindowRateLimiterRegistry implements RateLimiterRegistry {

    private static final long RETRY_INTERVAL_MILLIS = 50L;

    private final SlidingWindowRedisSupport slidingWindowRedisSupport;
    private final SlidingWindowRateLimiterProperties properties;

    private final Map<String, LimiterConfig> configCache = new ConcurrentHashMap<>();

    @Override
    public void configureRateLimiter(String limiterName, LimiterRule config) {
        if (limiterName == null || limiterName.trim().isEmpty()) {
            throw new IllegalArgumentException("Limiter name must not be blank.");
        }
        if (config == null) {
            throw new IllegalArgumentException("Limiter configuration must not be null.");
        }
        LimiterConfig limiterConfig = LimiterConfig.fromRule(config);
        validateLimiterConfig(limiterConfig);
        configCache.put(limiterName, limiterConfig);
        log.info("Configured sliding window limiter. limiter={}, config={}", limiterName, limiterConfig);
    }

    @Override
    public boolean tryAcquire(String limiterName) {
        return tryAcquire(limiterName, 1);
    }

    @Override
    public boolean tryAcquire(String limiterName, long permits) {
        return tryAcquire(limiterName, permits, properties.getDefaultTimeout(), TimeUnit.SECONDS);
    }

    @Override
    public boolean tryAcquire(String limiterName, long timeout, TimeUnit timeUnit) {
        return tryAcquire(limiterName, 1, timeout, timeUnit);
    }

    @Override
    public boolean tryAcquire(String limiterName, long permits, long timeout, TimeUnit timeUnit) {
        if (!properties.isEnabled()) {
            return true;
        }
        if (permits <= 0) {
            throw new IllegalArgumentException("Permits must be greater than 0.");
        }
        LimiterConfig config = getLimiterConfig(limiterName);
        long timeoutMillis = Math.max(0L, timeUnit.toMillis(timeout));
        long deadline = System.currentTimeMillis() + timeoutMillis;
        do {
            if (slidingWindowRedisSupport.tryAcquire(
                    limiterName,
                    config.getRate(),
                    config.getInterval(),
                    config.getIntervalUnit(),
                    permits
            )) {
                return true;
            }
            if (timeoutMillis <= 0L) {
                return false;
            }
            long remainingMillis = deadline - System.currentTimeMillis();
            if (remainingMillis <= 0L) {
                return false;
            }
            sleep(Math.min(RETRY_INTERVAL_MILLIS, remainingMillis));
        } while (true);
    }

    @Override
    public void updateRateLimiterConfig(String limiterName, LimiterRule newConfig) {
        configureRateLimiter(limiterName, newConfig);
    }

    @Override
    public boolean deleteRateLimiter(String limiterName) {
        configCache.remove(limiterName);
        return slidingWindowRedisSupport.delete(limiterName);
    }

    public RateLimiterStatus getRateLimiterStatus(String limiterName) {
        LimiterConfig config = getLimiterConfig(limiterName);
        long currentRequests = slidingWindowRedisSupport.currentRequests(
                limiterName,
                config.getInterval(),
                config.getIntervalUnit()
        );
        return RateLimiterStatus.builder()
                .name(limiterName)
                .currentRequests(currentRequests)
                .rate(config.getRate())
                .interval(config.getInterval())
                .intervalUnit(config.getIntervalUnit())
                .build();
    }

    private void validateLimiterConfig(LimiterConfig config) {
        if (config.getRate() <= 0) {
            throw new IllegalArgumentException("Rate must be greater than 0.");
        }
        if (config.getInterval() <= 0) {
            throw new IllegalArgumentException("Interval must be greater than 0.");
        }
        if (config.getIntervalUnit() == null) {
            throw new IllegalArgumentException("Interval unit must not be null.");
        }
    }

    private LimiterConfig getLimiterConfig(String limiterName) {
        return configCache.computeIfAbsent(limiterName, name -> {
            LimiterRule predefinedConfig = properties.getLimiters().get(name);
            if (predefinedConfig != null) {
                return LimiterConfig.fromRule(predefinedConfig);
            }
            return LimiterConfig.defaultConfig();
        });
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting for rate limiter permit.", ex);
        }
    }

    @Data
    @Builder
    public static class LimiterConfig {
        private long rate;
        private long interval;
        private TimeUnit intervalUnit;

        public static LimiterConfig fromRule(LimiterRule propsConfig) {
            return LimiterConfig.builder()
                    .rate(propsConfig.getRate())
                    .interval(propsConfig.getInterval())
                    .intervalUnit(TimeUnit.valueOf(propsConfig.getIntervalUnit()))
                    .build();
        }

        public static LimiterConfig defaultConfig() {
            return LimiterConfig.builder()
                    .rate(10)
                    .interval(1)
                    .intervalUnit(TimeUnit.SECONDS)
                    .build();
        }
    }

    @Data
    @Builder
    public static class RateLimiterStatus {
        private String name;
        private long currentRequests;
        private long rate;
        private long interval;
        private TimeUnit intervalUnit;

        public double getUsagePercentage() {
            return rate > 0 ? (double) currentRequests / rate * 100 : 0;
        }
    }
}
