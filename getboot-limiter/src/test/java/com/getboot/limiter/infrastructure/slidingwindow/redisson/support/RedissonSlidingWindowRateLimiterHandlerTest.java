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
import org.junit.jupiter.api.Test;
import org.redisson.api.RLock;
import org.redisson.api.RScoredSortedSet;
import org.redisson.api.RedissonClient;

import java.lang.reflect.Proxy;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 滑动窗口算法处理器测试。
 */
class RedissonSlidingWindowRateLimiterHandlerTest {

    /**
     * 验证能够写入请求事件并使用正确的 Redis key。
     */
    @Test
    void shouldAcquirePermitsWithinSlidingWindow() {
        SlidingWindowState state = new SlidingWindowState();
        RedissonSlidingWindowRateLimiterHandler handler = new RedissonSlidingWindowRateLimiterHandler(
                new SlidingWindowRedisSupport(redissonClient(state), "rate_limiter"),
                new SlidingWindowRateLimiterProperties()
        );

        assertTrue(handler.tryAcquire("login", rule(5, 1, "SECONDS"), 2));
        assertEquals("rate_limiter:login:window", state.lastWindowName);
        assertEquals("rate_limiter:login:lock", state.lastLockName);
        assertEquals(2, state.entries.size());
        assertTrue(state.expireCalled);
        assertEquals(Duration.ofSeconds(2), state.lastExpireDuration);
    }

    /**
     * 验证剩余配额不足时会拒绝本次申请。
     */
    @Test
    void shouldRejectWhenPermitsExceedSlidingWindowCapacity() {
        SlidingWindowState state = new SlidingWindowState();
        state.entries.put("existing-1", Double.MAX_VALUE);
        state.entries.put("existing-2", Double.MAX_VALUE);
        state.entries.put("existing-3", Double.MAX_VALUE);
        state.entries.put("existing-4", Double.MAX_VALUE);
        RedissonSlidingWindowRateLimiterHandler handler = new RedissonSlidingWindowRateLimiterHandler(
                new SlidingWindowRedisSupport(redissonClient(state), "rate_limiter"),
                new SlidingWindowRateLimiterProperties()
        );

        assertFalse(handler.tryAcquire("login", rule(5, 1, "SECONDS"), 2));
        assertEquals(4, state.entries.size());
    }

    /**
     * 验证不支持的时间单位会被拒绝。
     */
    @Test
    void shouldRejectUnsupportedSlidingWindowIntervalUnit() {
        SlidingWindowState state = new SlidingWindowState();
        RedissonSlidingWindowRateLimiterHandler handler = new RedissonSlidingWindowRateLimiterHandler(
                new SlidingWindowRedisSupport(redissonClient(state), "rate_limiter"),
                new SlidingWindowRateLimiterProperties()
        );

        IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class, () -> handler.validateRule(rule(5, 1, "UNKNOWN_UNIT")));
        assertTrue(exception.getMessage().contains("UNKNOWN_UNIT"));
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
        limiterRule.setAlgorithm(LimiterAlgorithm.SLIDING_WINDOW);
        limiterRule.setRate(rate);
        limiterRule.setInterval(interval);
        limiterRule.setIntervalUnit(intervalUnit);
        return limiterRule;
    }

    /**
     * 创建测试用 Redisson 客户端代理。
     *
     * @param state 滑动窗口测试状态
     * @return Redisson 客户端
     */
    private static RedissonClient redissonClient(SlidingWindowState state) {
        RScoredSortedSet<String> sortedSet = scoredSortedSet(state);
        RLock lock = lock(state);
        return (RedissonClient) Proxy.newProxyInstance(
                RedissonClient.class.getClassLoader(),
                new Class[]{RedissonClient.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getScoredSortedSet" -> {
                        state.lastWindowName = (String) args[0];
                        yield sortedSet;
                    }
                    case "getLock" -> {
                        state.lastLockName = (String) args[0];
                        yield lock;
                    }
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "equals" -> proxy == args[0];
                    case "toString" -> "RedissonClientProxy";
                    default -> throw new UnsupportedOperationException(method.getName());
                }
        );
    }

    /**
     * 创建测试用有序集合代理。
     *
     * @param state 滑动窗口测试状态
     * @return 有序集合代理
     */
    @SuppressWarnings("unchecked")
    private static RScoredSortedSet<String> scoredSortedSet(SlidingWindowState state) {
        return (RScoredSortedSet<String>) Proxy.newProxyInstance(
                RScoredSortedSet.class.getClassLoader(),
                new Class[]{RScoredSortedSet.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "removeRangeByScore" -> {
                        double startScore = ((Number) args[0]).doubleValue();
                        double endScore = ((Number) args[2]).doubleValue();
                        state.entries.entrySet().removeIf(entry -> entry.getValue() >= startScore && entry.getValue() <= endScore);
                        yield 0;
                    }
                    case "size" -> state.entries.size();
                    case "add" -> {
                        state.entries.put((String) args[1], ((Number) args[0]).doubleValue());
                        yield true;
                    }
                    case "expire" -> {
                        state.expireCalled = true;
                        state.lastExpireDuration = (Duration) args[0];
                        yield true;
                    }
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "equals" -> proxy == args[0];
                    case "toString" -> "RScoredSortedSetProxy";
                    default -> throw new UnsupportedOperationException(method.getName());
                }
        );
    }

    /**
     * 创建测试用锁代理。
     *
     * @param state 滑动窗口测试状态
     * @return 锁代理
     */
    private static RLock lock(SlidingWindowState state) {
        return (RLock) Proxy.newProxyInstance(
                RLock.class.getClassLoader(),
                new Class[]{RLock.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "tryLock" -> {
                        state.locked = true;
                        yield true;
                    }
                    case "isHeldByCurrentThread" -> state.locked;
                    case "unlock" -> {
                        state.locked = false;
                        yield null;
                    }
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "equals" -> proxy == args[0];
                    case "toString" -> "RLockProxy";
                    default -> throw new UnsupportedOperationException(method.getName());
                }
        );
    }

    /**
     * 滑动窗口测试状态。
     */
    private static final class SlidingWindowState {

        /**
         * 当前窗口事件。
         */
        private final Map<String, Double> entries = new LinkedHashMap<>();

        /**
         * 最近一次窗口 key。
         */
        private String lastWindowName;

        /**
         * 最近一次锁 key。
         */
        private String lastLockName;

        /**
         * 是否已持有锁。
         */
        private boolean locked;

        /**
         * 是否设置过过期时间。
         */
        private boolean expireCalled;

        /**
         * 最近一次过期时间。
         */
        private Duration lastExpireDuration;
    }
}
