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

import java.time.Duration;
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

    /**
     * 令牌桶 Redis 支撑组件。
     */
    private final TokenBucketRedisSupport tokenBucketRedisSupport;

    /**
     * 令牌桶配置。
     */
    private final TokenBucketRateLimiterProperties properties;

    /**
     * 返回当前处理器支持的算法类型。
     *
     * @return 算法类型
     */
    @Override
    public LimiterAlgorithm algorithm() {
        return LimiterAlgorithm.TOKEN_BUCKET;
    }

    /**
     * 返回预定义限流规则集合。
     *
     * @return 预定义规则集合
     */
    @Override
    public Map<String, LimiterRule> predefinedRules() {
        Map<String, LimiterRule> predefinedRules = new HashMap<>();
        for (Map.Entry<String, LimiterRule> entry : properties.getLimiters().entrySet()) {
            predefinedRules.put(entry.getKey(), normalizeRule(entry.getValue()));
        }
        return predefinedRules;
    }

    /**
     * 返回默认限流规则。
     *
     * @return 默认规则
     */
    @Override
    public LimiterRule defaultRule() {
        LimiterRule defaultRule = new LimiterRule();
        defaultRule.setAlgorithm(LimiterAlgorithm.TOKEN_BUCKET);
        defaultRule.setRate(10);
        defaultRule.setInterval(1);
        defaultRule.setIntervalUnit(TimeUnit.SECONDS.name());
        return defaultRule;
    }

    /**
     * 返回默认等待时长。
     *
     * @return 默认等待秒数
     */
    @Override
    public long defaultTimeout() {
        return properties.getDefaultTimeout();
    }

    /**
     * 校验令牌桶规则是否合法。
     *
     * @param rule 限流规则
     */
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
        toIntervalDuration(normalizedRule);
    }

    /**
     * 尝试获取指定数量的令牌。
     *
     * @param limiterName 限流器名称
     * @param rule 限流规则
     * @param permits 令牌数量
     * @return 是否获取成功
     */
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
                toIntervalDuration(normalizedRule),
                permits
        );
    }

    /**
     * 删除限流器底层状态。
     *
     * @param limiterName 限流器名称
     * @return 是否删除成功
     */
    @Override
    public boolean delete(String limiterName) {
        return tokenBucketRedisSupport.delete(limiterName);
    }

    /**
     * 标准化令牌桶规则。
     *
     * @param rule 原始规则
     * @return 标准化后的规则
     */
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

    /**
     * 将规则中的时间单位转换为时间窗口时长。
     *
     * @param rule 限流规则
     * @return 时间窗口时长
     */
    private Duration toIntervalDuration(LimiterRule rule) {
        TimeUnit intervalUnit;
        try {
            intervalUnit = rule.resolveIntervalUnit();
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Unsupported interval unit for token bucket limiter: "
                    + rule.getIntervalUnit(), ex);
        }
        return switch (intervalUnit) {
            case MILLISECONDS -> Duration.ofMillis(rule.getInterval());
            case SECONDS -> Duration.ofSeconds(rule.getInterval());
            case MINUTES -> Duration.ofMinutes(rule.getInterval());
            case HOURS -> Duration.ofHours(rule.getInterval());
            case DAYS -> Duration.ofDays(rule.getInterval());
            default -> throw new IllegalArgumentException(
                    "Token bucket limiter does not support interval unit '" + rule.getIntervalUnit()
                            + "'. Supported values: MILLISECONDS / SECONDS / MINUTES / HOURS / DAYS.");
        };
    }
}
