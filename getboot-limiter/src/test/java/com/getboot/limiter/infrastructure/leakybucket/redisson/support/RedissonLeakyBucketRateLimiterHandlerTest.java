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
package com.getboot.limiter.infrastructure.leakybucket.redisson.support;

import com.getboot.limiter.api.model.LimiterAlgorithm;
import com.getboot.limiter.api.model.LimiterRule;
import com.getboot.limiter.api.properties.LeakyBucketRateLimiterProperties;
import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;

import java.lang.reflect.Proxy;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RedissonLeakyBucketRateLimiterHandlerTest {

    @Test
    void shouldRejectUnsupportedIntervalUnit() {
        FakeLeakyBucketState state = new FakeLeakyBucketState();
        AtomicLong now = new AtomicLong(0L);
        RedissonLeakyBucketRateLimiterHandler handler = new RedissonLeakyBucketRateLimiterHandler(
                new LeakyBucketRedisSupport(redissonClient(state), "rate_limiter_leaky_bucket", now::get),
                new LeakyBucketRateLimiterProperties()
        );

        IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class, () -> handler.validateRule(rule("MICROSECONDS")));

        assertTrue(exception.getMessage().contains("MICROSECONDS"));
    }

    private static LimiterRule rule(String intervalUnit) {
        LimiterRule limiterRule = new LimiterRule();
        limiterRule.setAlgorithm(LimiterAlgorithm.LEAKY_BUCKET);
        limiterRule.setRate(5);
        limiterRule.setInterval(1);
        limiterRule.setIntervalUnit(intervalUnit);
        return limiterRule;
    }

    private static RedissonClient redissonClient(FakeLeakyBucketState state) {
        return (RedissonClient) Proxy.newProxyInstance(
                RedissonClient.class.getClassLoader(),
                new Class[]{RedissonClient.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getBucket", "getLock", "getKeys" -> throw new UnsupportedOperationException(method.getName());
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "equals" -> proxy == args[0];
                    case "toString" -> "RedissonClientProxy";
                    default -> throw new UnsupportedOperationException(method.getName());
                }
        );
    }

    private static final class FakeLeakyBucketState {
    }
}
