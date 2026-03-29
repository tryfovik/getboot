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
package com.getboot.lock.infrastructure.redis.redisson.aspect;

import com.getboot.lock.api.annotation.DistributedLock;
import com.getboot.lock.api.properties.LockProperties;
import com.getboot.lock.spi.DistributedLockAcquireFailureHandler;
import com.getboot.lock.spi.DistributedLockKeyResolver;
import com.getboot.lock.api.constant.DistributedLockConstants;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * Redis 分布式锁切面处理类。
 *
 * @author qiheng
 */
@Aspect
@Order(Integer.MIN_VALUE + 1)
public class DistributedLockAspect {

    private static final Logger LOG = LoggerFactory.getLogger(DistributedLockAspect.class);

    private final RedissonClient redissonClient;
    private final DistributedLockKeyResolver distributedLockKeyResolver;
    private final DistributedLockAcquireFailureHandler distributedLockAcquireFailureHandler;
    private final LockProperties properties;

    public DistributedLockAspect(RedissonClient redissonClient,
                                 DistributedLockKeyResolver distributedLockKeyResolver,
                                 DistributedLockAcquireFailureHandler distributedLockAcquireFailureHandler,
                                 LockProperties properties) {
        this.redissonClient = redissonClient;
        this.distributedLockKeyResolver = distributedLockKeyResolver;
        this.distributedLockAcquireFailureHandler = distributedLockAcquireFailureHandler;
        this.properties = properties;
    }

    @Around("@annotation(distributedLock)")
    public Object process(ProceedingJoinPoint pjp, DistributedLock distributedLock) throws Throwable {
        Method method = ((MethodSignature) pjp.getSignature()).getMethod();
        String key = distributedLockKeyResolver.resolve(pjp, method, distributedLock);
        String scene = distributedLock.scene();
        String lockKey = properties.getRedis().getKeyPrefix() + ":" + scene + "#" + key;

        int expireTime = distributedLock.expireTime();
        int waitTime = distributedLock.waitTime();
        RLock rLock = redissonClient.getLock(lockKey);
        try {
            boolean lockResult;
            if (waitTime == DistributedLockConstants.DEFAULT_WAIT_TIME) {
                if (expireTime == DistributedLockConstants.DEFAULT_EXPIRE_TIME) {
                    LOG.info("Trying to acquire distributed lock. key={}", lockKey);
                    rLock.lock();
                } else {
                    LOG.info("Trying to acquire distributed lock. key={}, expireTimeMs={}", lockKey, expireTime);
                    rLock.lock(expireTime, TimeUnit.MILLISECONDS);
                }
                lockResult = true;
            } else {
                if (expireTime == DistributedLockConstants.DEFAULT_EXPIRE_TIME) {
                    LOG.info("Trying to acquire distributed lock. key={}, waitTimeMs={}", lockKey, waitTime);
                    lockResult = rLock.tryLock(waitTime, TimeUnit.MILLISECONDS);
                } else {
                    LOG.info("Trying to acquire distributed lock. key={}, expireTimeMs={}, waitTimeMs={}",
                            lockKey, expireTime, waitTime);
                    lockResult = rLock.tryLock(waitTime, expireTime, TimeUnit.MILLISECONDS);
                }
            }

            if (!lockResult) {
                LOG.warn("Failed to acquire distributed lock. key={}, expireTimeMs={}", lockKey, expireTime);
                distributedLockAcquireFailureHandler.onFailure(lockKey, distributedLock);
            }

            LOG.info("Distributed lock acquired. key={}, expireTimeMs={}", lockKey, expireTime);
            return pjp.proceed();
        } finally {
            if (rLock.isHeldByCurrentThread()) {
                rLock.unlock();
                LOG.info("Distributed lock released. key={}, expireTimeMs={}", lockKey, expireTime);
            }
        }
    }
}
