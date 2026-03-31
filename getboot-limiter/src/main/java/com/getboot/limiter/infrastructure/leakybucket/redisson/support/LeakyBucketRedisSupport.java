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

import org.redisson.api.RBucket;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;

import java.util.concurrent.TimeUnit;
import java.util.function.LongSupplier;

/**
 * Redis 漏桶限流底层支持。
 *
 * <p>使用 Redis 字符串保存桶状态，并通过分布式锁保证并发更新的一致性。</p>
 *
 * @author qiheng
 */
public class LeakyBucketRedisSupport {

    private static final String STATE_KEY_SUFFIX = ":state";
    private static final String LOCK_KEY_SUFFIX = ":lock";
    private static final long MIN_TTL_MILLIS = 1000L;

    private final RedissonClient redissonClient;
    private final String keyPrefix;
    private final LongSupplier currentTimeSupplier;

    public LeakyBucketRedisSupport(RedissonClient redissonClient, String keyPrefix) {
        this(redissonClient, keyPrefix, System::currentTimeMillis);
    }

    LeakyBucketRedisSupport(RedissonClient redissonClient, String keyPrefix, LongSupplier currentTimeSupplier) {
        this.redissonClient = redissonClient;
        this.keyPrefix = keyPrefix;
        this.currentTimeSupplier = currentTimeSupplier;
    }

    public boolean tryAcquire(String limiterName, long capacity, long interval, TimeUnit intervalUnit, long permits) {
        long intervalMillis = intervalUnit.toMillis(interval);
        if (intervalMillis <= 0L) {
            throw new IllegalArgumentException("Leaky bucket interval must be at least 1 millisecond.");
        }
        long now = currentTimeSupplier.getAsLong();
        RLock lock = redissonClient.getLock(buildLockKey(limiterName));
        boolean locked = lock.tryLock();
        if (!locked) {
            return false;
        }
        try {
            RBucket<String> stateBucket = redissonClient.getBucket(buildStateKey(limiterName), StringCodec.INSTANCE);
            BucketState currentState = BucketState.deserialize(stateBucket.get(), now);
            BucketState leakedState = currentState.leak(now, capacity, intervalMillis);
            if (leakedState.waterLevel + permits > capacity) {
                persistState(stateBucket, leakedState, intervalMillis);
                return false;
            }
            BucketState updatedState = leakedState.add(now, permits);
            persistState(stateBucket, updatedState, intervalMillis);
            return true;
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    public boolean delete(String limiterName) {
        long deleted = redissonClient.getKeys().delete(
                buildStateKey(limiterName),
                buildLockKey(limiterName)
        );
        return deleted > 0;
    }

    private void persistState(RBucket<String> stateBucket, BucketState state, long intervalMillis) {
        stateBucket.set(state.serialize(), ttlMillis(intervalMillis), TimeUnit.MILLISECONDS);
    }

    private long ttlMillis(long intervalMillis) {
        if (intervalMillis > Long.MAX_VALUE / 2) {
            return Long.MAX_VALUE;
        }
        return Math.max(intervalMillis * 2, MIN_TTL_MILLIS);
    }

    private String buildStateKey(String limiterName) {
        return keyPrefix + ":" + limiterName + STATE_KEY_SUFFIX;
    }

    private String buildLockKey(String limiterName) {
        return keyPrefix + ":" + limiterName + LOCK_KEY_SUFFIX;
    }

    private record BucketState(long lastLeakTimestamp, long waterLevel) {

        private static BucketState deserialize(String rawState, long now) {
            if (rawState == null || rawState.trim().isEmpty()) {
                return new BucketState(now, 0L);
            }
            String[] parts = rawState.split(":");
            if (parts.length != 2) {
                throw new IllegalStateException("Malformed leaky bucket state: " + rawState);
            }
            try {
                long lastLeakTimestamp = Long.parseLong(parts[0]);
                long waterLevel = Long.parseLong(parts[1]);
                return new BucketState(lastLeakTimestamp, Math.max(0L, waterLevel));
            } catch (NumberFormatException ex) {
                throw new IllegalStateException("Malformed leaky bucket state: " + rawState, ex);
            }
        }

        private BucketState leak(long now, long capacity, long intervalMillis) {
            if (waterLevel <= 0L || now <= lastLeakTimestamp) {
                return waterLevel <= 0L ? new BucketState(now, 0L) : this;
            }
            long leakedPermits = (long) Math.floor((double) (now - lastLeakTimestamp) * (double) capacity / intervalMillis);
            if (leakedPermits <= 0L) {
                return this;
            }
            long newWaterLevel = Math.max(0L, waterLevel - leakedPermits);
            if (newWaterLevel == 0L) {
                return new BucketState(now, 0L);
            }
            long advancedMillis = (long) Math.floor((double) leakedPermits * intervalMillis / capacity);
            long nextLastLeakTimestamp = advancedMillis > 0L
                    ? Math.min(now, lastLeakTimestamp + advancedMillis)
                    : now;
            return new BucketState(nextLastLeakTimestamp, newWaterLevel);
        }

        private BucketState add(long now, long permits) {
            long nextLastLeakTimestamp = waterLevel == 0L ? now : lastLeakTimestamp;
            return new BucketState(nextLastLeakTimestamp, waterLevel + permits);
        }

        private String serialize() {
            return lastLeakTimestamp + ":" + waterLevel;
        }
    }
}
