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

import org.junit.jupiter.api.Test;
import org.redisson.api.RBucket;
import org.redisson.api.RKeys;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.lang.reflect.Proxy;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LeakyBucketRedisSupportTest {

    @Test
    void shouldRejectWhenBucketStillFull() {
        FakeLeakyBucketState state = new FakeLeakyBucketState();
        AtomicLong now = new AtomicLong(0L);
        LeakyBucketRedisSupport redisSupport =
                new LeakyBucketRedisSupport(redissonClient(state), "rate_limiter_leaky_bucket", now::get);

        assertTrue(redisSupport.tryAcquire("login", 5, 1, TimeUnit.SECONDS, 5));
        assertFalse(redisSupport.tryAcquire("login", 5, 1, TimeUnit.SECONDS, 1));
        assertEquals("0:5", state.bucketValue);
        assertEquals("rate_limiter_leaky_bucket:login:state", state.lastBucketName);
        assertEquals("rate_limiter_leaky_bucket:login:lock", state.lastLockName);
    }

    @Test
    void shouldLeakWaterBeforeNextAcquire() {
        FakeLeakyBucketState state = new FakeLeakyBucketState();
        AtomicLong now = new AtomicLong(0L);
        LeakyBucketRedisSupport redisSupport =
                new LeakyBucketRedisSupport(redissonClient(state), "rate_limiter_leaky_bucket", now::get);

        assertTrue(redisSupport.tryAcquire("login", 5, 1, TimeUnit.SECONDS, 5));

        now.set(500L);
        assertTrue(redisSupport.tryAcquire("login", 5, 1, TimeUnit.SECONDS, 1));
        assertEquals("400:4", state.bucketValue);

        now.set(1000L);
        assertTrue(redisSupport.tryAcquire("login", 5, 1, TimeUnit.SECONDS, 2));
        assertEquals("1000:3", state.bucketValue);
        assertEquals(2000L, state.ttl);
        assertEquals(TimeUnit.MILLISECONDS, state.ttlUnit);
    }

    @Test
    void shouldReturnFalseWhenLockCannotBeAcquired() {
        FakeLeakyBucketState state = new FakeLeakyBucketState();
        state.lockAvailable = false;
        AtomicLong now = new AtomicLong(0L);
        LeakyBucketRedisSupport redisSupport =
                new LeakyBucketRedisSupport(redissonClient(state), "rate_limiter_leaky_bucket", now::get);

        assertFalse(redisSupport.tryAcquire("login", 5, 1, TimeUnit.SECONDS, 1));
        assertEquals(null, state.bucketValue);
    }

    private static RedissonClient redissonClient(FakeLeakyBucketState state) {
        RBucket<String> bucket = bucket(state);
        RLock lock = lock(state);
        RKeys keys = keys(state);
        return (RedissonClient) Proxy.newProxyInstance(
                RedissonClient.class.getClassLoader(),
                new Class[]{RedissonClient.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getBucket" -> {
                        state.lastBucketName = (String) args[0];
                        yield bucket;
                    }
                    case "getLock" -> {
                        state.lastLockName = (String) args[0];
                        yield lock;
                    }
                    case "getKeys" -> keys;
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "equals" -> proxy == args[0];
                    case "toString" -> "RedissonClientProxy";
                    default -> throw new UnsupportedOperationException(method.getName());
                }
        );
    }

    @SuppressWarnings("unchecked")
    private static RBucket<String> bucket(FakeLeakyBucketState state) {
        return (RBucket<String>) Proxy.newProxyInstance(
                RBucket.class.getClassLoader(),
                new Class[]{RBucket.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "get" -> state.bucketValue;
                    case "set" -> {
                        state.bucketValue = (String) args[0];
                        if (args.length == 3) {
                            state.ttl = (Long) args[1];
                            state.ttlUnit = (TimeUnit) args[2];
                        }
                        yield null;
                    }
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "equals" -> proxy == args[0];
                    case "toString" -> "RBucketProxy";
                    default -> throw new UnsupportedOperationException(method.getName());
                }
        );
    }

    private static RLock lock(FakeLeakyBucketState state) {
        return (RLock) Proxy.newProxyInstance(
                RLock.class.getClassLoader(),
                new Class[]{RLock.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "tryLock" -> {
                        if (state.lockAvailable) {
                            state.locked = true;
                            yield true;
                        }
                        yield false;
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

    private static RKeys keys(FakeLeakyBucketState state) {
        return (RKeys) Proxy.newProxyInstance(
                RKeys.class.getClassLoader(),
                new Class[]{RKeys.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "delete" -> {
                        state.deleteCalls++;
                        yield 2L;
                    }
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "equals" -> proxy == args[0];
                    case "toString" -> "RKeysProxy";
                    default -> throw new UnsupportedOperationException(method.getName());
                }
        );
    }

    private static final class FakeLeakyBucketState {
        private boolean lockAvailable = true;
        private boolean locked;
        private String bucketValue;
        private String lastBucketName;
        private String lastLockName;
        private long ttl;
        private TimeUnit ttlUnit;
        private int deleteCalls;
    }
}
