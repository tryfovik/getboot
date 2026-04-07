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
import org.junit.jupiter.api.Test;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateLimiterConfig;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;

import java.time.Duration;
import java.lang.reflect.Proxy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 令牌桶算法处理器测试。
 */
class RedissonTokenBucketRateLimiterHandlerTest {

    /**
     * 验证能够初始化并获取令牌。
     */
    @Test
    void shouldConfigureRateLimiterAndAcquirePermits() {
        TokenBucketLimiterState state = new TokenBucketLimiterState();
        RedissonTokenBucketRateLimiterHandler handler = new RedissonTokenBucketRateLimiterHandler(
                new TokenBucketRedisSupport(redissonClient(state), "rate_limiter_token_bucket"),
                new TokenBucketRateLimiterProperties()
        );
        LimiterRule limiterRule = rule(5, 1, "SECONDS");

        assertTrue(handler.tryAcquire("login", limiterRule, 2));
        assertEquals(1, state.trySetRateCalls);
        assertEquals(0, state.setRateCalls);
        assertEquals(5, state.rate);
        assertEquals(Duration.ofSeconds(1), state.intervalDuration);
        assertEquals(2, state.lastPermits);
        assertEquals("rate_limiter_token_bucket:login:bucket", state.lastLimiterName);
    }

    /**
     * 验证规则变化时会刷新远端配置。
     */
    @Test
    void shouldRefreshRemoteConfigWhenRuleChanges() {
        TokenBucketLimiterState state = new TokenBucketLimiterState();
        RedissonTokenBucketRateLimiterHandler handler = new RedissonTokenBucketRateLimiterHandler(
                new TokenBucketRedisSupport(redissonClient(state), "rate_limiter_token_bucket"),
                new TokenBucketRateLimiterProperties()
        );

        assertTrue(handler.tryAcquire("login", rule(5, 1, "SECONDS"), 1));
        assertTrue(handler.tryAcquire("login", rule(10, 2, "MINUTES"), 1));

        assertEquals(2, state.trySetRateCalls);
        assertEquals(1, state.setRateCalls);
        assertEquals(10, state.rate);
        assertEquals(Duration.ofMinutes(2), state.intervalDuration);
    }

    /**
     * 验证不支持的时间单位会被拒绝。
     */
    @Test
    void shouldRejectUnsupportedTokenBucketIntervalUnit() {
        TokenBucketLimiterState state = new TokenBucketLimiterState();
        RedissonTokenBucketRateLimiterHandler handler = new RedissonTokenBucketRateLimiterHandler(
                new TokenBucketRedisSupport(redissonClient(state), "rate_limiter_token_bucket"),
                new TokenBucketRateLimiterProperties()
        );
        LimiterRule limiterRule = rule(5, 1, "MICROSECONDS");

        IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class, () -> handler.validateRule(limiterRule));

        assertTrue(exception.getMessage().contains("MICROSECONDS"));
    }

    /**
     * 创建测试用限流规则。
     *
     * @param rate 速率阈值
     * @param interval 时间窗口大小
     * @param intervalUnit 时间窗口单位
     * @return 限流规则
     */
    private static LimiterRule rule(long rate, long interval, String intervalUnit) {
        LimiterRule limiterRule = new LimiterRule();
        limiterRule.setAlgorithm(LimiterAlgorithm.TOKEN_BUCKET);
        limiterRule.setRate(rate);
        limiterRule.setInterval(interval);
        limiterRule.setIntervalUnit(intervalUnit);
        return limiterRule;
    }

    /**
     * 创建测试用 Redisson 客户端代理。
     *
     * @param state 令牌桶测试状态
     * @return Redisson 客户端
     */
    private static RedissonClient redissonClient(TokenBucketLimiterState state) {
        RRateLimiter rateLimiter = rateLimiter(state);
        return (RedissonClient) Proxy.newProxyInstance(
                RedissonClient.class.getClassLoader(),
                new Class[]{RedissonClient.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getRateLimiter" -> {
                        state.lastLimiterName = (String) args[0];
                        yield rateLimiter;
                    }
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "equals" -> proxy == args[0];
                    case "toString" -> "RedissonClientProxy";
                    default -> throw new UnsupportedOperationException(method.getName());
                }
        );
    }

    /**
     * 创建测试用 RRateLimiter 代理。
     *
     * @param state 令牌桶测试状态
     * @return RRateLimiter 代理
     */
    private static RRateLimiter rateLimiter(TokenBucketLimiterState state) {
        return (RRateLimiter) Proxy.newProxyInstance(
                RRateLimiter.class.getClassLoader(),
                new Class[]{RRateLimiter.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "trySetRate" -> {
                        state.trySetRateCalls++;
                        if (!state.initialized) {
                            state.initialized = true;
                            state.rate = (Long) args[1];
                            state.intervalDuration = (Duration) args[2];
                            yield true;
                        }
                        yield false;
                    }
                    case "getConfig" -> new RateLimiterConfig(
                            RateType.OVERALL,
                            state.rate,
                            state.intervalDuration.toMillis()
                    );
                    case "setRate" -> {
                        state.setRateCalls++;
                        state.rate = (Long) args[1];
                        state.intervalDuration = (Duration) args[2];
                        state.initialized = true;
                        yield null;
                    }
                    case "tryAcquire" -> {
                        state.lastPermits = args == null || args.length == 0 ? 1L : (Long) args[0];
                        yield true;
                    }
                    case "delete" -> true;
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "equals" -> proxy == args[0];
                    case "toString" -> "RRateLimiterProxy";
                    default -> throw new UnsupportedOperationException(method.getName());
                }
        );
    }

    /**
     * 令牌桶测试状态。
     */
    private static final class TokenBucketLimiterState {

        /**
         * 是否已完成初始化。
         */
        private boolean initialized;

        /**
         * 当前速率值。
         */
        private long rate;

        /**
         * 当前窗口时长。
         */
        private Duration intervalDuration = Duration.ofSeconds(1);

        /**
         * 最近一次申请的许可数量。
         */
        private long lastPermits;

        /**
         * trySetRate 调用次数。
         */
        private int trySetRateCalls;

        /**
         * setRate 调用次数。
         */
        private int setRateCalls;

        /**
         * 最近一次限流器名称。
         */
        private String lastLimiterName;
    }
}
