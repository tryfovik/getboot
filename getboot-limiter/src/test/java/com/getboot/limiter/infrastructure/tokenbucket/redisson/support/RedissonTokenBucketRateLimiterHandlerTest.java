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
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateLimiterConfig;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;

import java.lang.reflect.Proxy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RedissonTokenBucketRateLimiterHandlerTest {

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
        assertEquals(1, state.interval);
        assertEquals(RateIntervalUnit.SECONDS, state.intervalUnit);
        assertEquals(2, state.lastPermits);
        assertEquals("rate_limiter_token_bucket:login:bucket", state.lastLimiterName);
    }

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
        assertEquals(2, state.interval);
        assertEquals(RateIntervalUnit.MINUTES, state.intervalUnit);
    }

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

    private static LimiterRule rule(long rate, long interval, String intervalUnit) {
        LimiterRule limiterRule = new LimiterRule();
        limiterRule.setAlgorithm(LimiterAlgorithm.TOKEN_BUCKET);
        limiterRule.setRate(rate);
        limiterRule.setInterval(interval);
        limiterRule.setIntervalUnit(intervalUnit);
        return limiterRule;
    }

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
                            state.interval = (Long) args[2];
                            state.intervalUnit = (RateIntervalUnit) args[3];
                            yield true;
                        }
                        yield false;
                    }
                    case "getConfig" -> new RateLimiterConfig(
                            RateType.OVERALL,
                            state.rate,
                            state.intervalUnit.toMillis(state.interval)
                    );
                    case "setRate" -> {
                        state.setRateCalls++;
                        state.rate = (Long) args[1];
                        state.interval = (Long) args[2];
                        state.intervalUnit = (RateIntervalUnit) args[3];
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

    private static final class TokenBucketLimiterState {
        private boolean initialized;
        private long rate;
        private long interval;
        private RateIntervalUnit intervalUnit = RateIntervalUnit.SECONDS;
        private long lastPermits;
        private int trySetRateCalls;
        private int setRateCalls;
        private String lastLimiterName;
    }
}
