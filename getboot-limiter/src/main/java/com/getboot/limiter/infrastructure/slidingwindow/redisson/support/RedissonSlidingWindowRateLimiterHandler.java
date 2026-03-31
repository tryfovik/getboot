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

import com.getboot.limiter.api.model.LimiterAlgorithm;
import com.getboot.limiter.api.model.LimiterRule;
import com.getboot.limiter.api.properties.SlidingWindowRateLimiterProperties;
import com.getboot.limiter.spi.RateLimiterAlgorithmHandler;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 滑动窗口限流算法处理器。
 *
 * <p>负责把统一规则路由到 Redis 有序集合实现。</p>
 *
 * @author qiheng
 */
@RequiredArgsConstructor
public class RedissonSlidingWindowRateLimiterHandler implements RateLimiterAlgorithmHandler {

    private final SlidingWindowRedisSupport slidingWindowRedisSupport;
    private final SlidingWindowRateLimiterProperties properties;

    @Override
    public LimiterAlgorithm algorithm() {
        return LimiterAlgorithm.SLIDING_WINDOW;
    }

    @Override
    public Map<String, LimiterRule> predefinedRules() {
        Map<String, LimiterRule> predefinedRules = new HashMap<>();
        for (Map.Entry<String, LimiterRule> entry : properties.getLimiters().entrySet()) {
            predefinedRules.put(entry.getKey(), normalizeRule(entry.getValue()));
        }
        return predefinedRules;
    }

    @Override
    public LimiterRule defaultRule() {
        LimiterRule defaultRule = new LimiterRule();
        defaultRule.setAlgorithm(LimiterAlgorithm.SLIDING_WINDOW);
        defaultRule.setRate(10);
        defaultRule.setInterval(1);
        defaultRule.setIntervalUnit(TimeUnit.SECONDS.name());
        return defaultRule;
    }

    @Override
    public long defaultTimeout() {
        return properties.getDefaultTimeout();
    }

    @Override
    public void validateRule(LimiterRule rule) {
        LimiterRule normalizedRule = normalizeRule(rule);
        if (normalizedRule.getAlgorithm() != LimiterAlgorithm.SLIDING_WINDOW) {
            throw new IllegalArgumentException("Sliding window handler only supports SLIDING_WINDOW rules.");
        }
        if (normalizedRule.getRate() <= 0) {
            throw new IllegalArgumentException("Rate must be greater than 0.");
        }
        if (normalizedRule.getInterval() <= 0) {
            throw new IllegalArgumentException("Interval must be greater than 0.");
        }
        resolveIntervalUnit(normalizedRule);
    }

    @Override
    public boolean tryAcquire(String limiterName, LimiterRule rule, long permits) {
        if (permits <= 0) {
            throw new IllegalArgumentException("Permits must be greater than 0.");
        }
        LimiterRule normalizedRule = normalizeRule(rule);
        validateRule(normalizedRule);
        return slidingWindowRedisSupport.tryAcquire(
                limiterName,
                normalizedRule.getRate(),
                normalizedRule.getInterval(),
                resolveIntervalUnit(normalizedRule),
                permits
        );
    }

    @Override
    public boolean delete(String limiterName) {
        return slidingWindowRedisSupport.delete(limiterName);
    }

    private LimiterRule normalizeRule(LimiterRule rule) {
        if (rule == null) {
            throw new IllegalArgumentException("Limiter rule must not be null.");
        }
        LimiterRule normalizedRule = rule.copy();
        if (normalizedRule.getAlgorithm() == null) {
            normalizedRule.setAlgorithm(LimiterAlgorithm.SLIDING_WINDOW);
        }
        return normalizedRule;
    }

    private TimeUnit resolveIntervalUnit(LimiterRule rule) {
        try {
            return rule.resolveIntervalUnit();
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Unsupported interval unit for sliding window limiter: "
                    + rule.getIntervalUnit(), ex);
        }
    }
}
