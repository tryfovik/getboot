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

import com.getboot.limiter.api.model.LimiterAlgorithm;
import com.getboot.limiter.api.model.LimiterRule;
import com.getboot.limiter.api.properties.TokenBucketRateLimiterProperties;
import com.getboot.limiter.spi.RateLimiterAlgorithmHandler;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RateIntervalUnit;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 令牌桶限流算法处理器。
 *
 * <p>负责把统一规则路由到 Redisson {@code RRateLimiter} 实现。</p>
 *
 * @author qiheng
 */
@RequiredArgsConstructor
public class RedissonTokenBucketRateLimiterHandler implements RateLimiterAlgorithmHandler {

    private final TokenBucketRedisSupport tokenBucketRedisSupport;
    private final TokenBucketRateLimiterProperties properties;

    @Override
    public LimiterAlgorithm algorithm() {
        return LimiterAlgorithm.TOKEN_BUCKET;
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
        defaultRule.setAlgorithm(LimiterAlgorithm.TOKEN_BUCKET);
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
        if (normalizedRule.getAlgorithm() != LimiterAlgorithm.TOKEN_BUCKET) {
            throw new IllegalArgumentException("Token bucket handler only supports TOKEN_BUCKET rules.");
        }
        if (normalizedRule.getRate() <= 0) {
            throw new IllegalArgumentException("Rate must be greater than 0.");
        }
        if (normalizedRule.getInterval() <= 0) {
            throw new IllegalArgumentException("Interval must be greater than 0.");
        }
        toRateIntervalUnit(normalizedRule);
    }

    @Override
    public boolean tryAcquire(String limiterName, LimiterRule rule, long permits) {
        if (permits <= 0) {
            throw new IllegalArgumentException("Permits must be greater than 0.");
        }
        LimiterRule normalizedRule = normalizeRule(rule);
        validateRule(normalizedRule);
        return tokenBucketRedisSupport.tryAcquire(
                limiterName,
                normalizedRule.getRate(),
                normalizedRule.getInterval(),
                toRateIntervalUnit(normalizedRule),
                permits
        );
    }

    @Override
    public boolean delete(String limiterName) {
        return tokenBucketRedisSupport.delete(limiterName);
    }

    private LimiterRule normalizeRule(LimiterRule rule) {
        if (rule == null) {
            throw new IllegalArgumentException("Limiter rule must not be null.");
        }
        LimiterRule normalizedRule = rule.copy();
        if (normalizedRule.getAlgorithm() == null) {
            normalizedRule.setAlgorithm(LimiterAlgorithm.TOKEN_BUCKET);
        }
        return normalizedRule;
    }

    private RateIntervalUnit toRateIntervalUnit(LimiterRule rule) {
        TimeUnit intervalUnit;
        try {
            intervalUnit = rule.resolveIntervalUnit();
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Unsupported interval unit for token bucket limiter: "
                    + rule.getIntervalUnit(), ex);
        }
        return switch (intervalUnit) {
            case MILLISECONDS -> RateIntervalUnit.MILLISECONDS;
            case SECONDS -> RateIntervalUnit.SECONDS;
            case MINUTES -> RateIntervalUnit.MINUTES;
            case HOURS -> RateIntervalUnit.HOURS;
            case DAYS -> RateIntervalUnit.DAYS;
            default -> throw new IllegalArgumentException(
                    "Token bucket limiter does not support interval unit '" + rule.getIntervalUnit()
                            + "'. Supported values: MILLISECONDS / SECONDS / MINUTES / HOURS / DAYS.");
        };
    }
}
