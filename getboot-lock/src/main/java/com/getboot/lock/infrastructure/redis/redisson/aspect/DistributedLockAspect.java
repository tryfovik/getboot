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
import com.getboot.lock.api.constant.DistributedLockConstants;
import com.getboot.lock.api.properties.LockProperties;
import com.getboot.lock.spi.DistributedLockAcquireFailureHandler;
import com.getboot.lock.spi.DistributedLockKeyResolver;
import com.getboot.lock.support.DistributedLockSupport;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;

import java.util.concurrent.TimeUnit;

/**
 * Redis 分布式锁切面处理类。
 *
 * @author qiheng
 */
@Aspect
@Order(Integer.MIN_VALUE + 1)
public class DistributedLockAspect {

    /**
     * 日志记录器。
     */
    private static final Logger LOG = LoggerFactory.getLogger(DistributedLockAspect.class);

    /**
     * Redisson 客户端。
     */
    private final RedissonClient redissonClient;

    /**
     * 锁 key 解析器。
     */
    private final DistributedLockKeyResolver distributedLockKeyResolver;

    /**
     * 锁获取失败处理器。
     */
    private final DistributedLockAcquireFailureHandler distributedLockAcquireFailureHandler;

    /**
     * 锁配置属性。
     */
    private final LockProperties properties;

    /**
     * 创建 Redis 分布式锁切面。
     *
     * @param redissonClient Redisson 客户端
     * @param distributedLockKeyResolver 锁 key 解析器
     * @param distributedLockAcquireFailureHandler 锁获取失败处理器
     * @param properties 锁配置属性
     */
    public DistributedLockAspect(RedissonClient redissonClient,
                                 DistributedLockKeyResolver distributedLockKeyResolver,
                                 DistributedLockAcquireFailureHandler distributedLockAcquireFailureHandler,
                                 LockProperties properties) {
        this.redissonClient = redissonClient;
        this.distributedLockKeyResolver = distributedLockKeyResolver;
        this.distributedLockAcquireFailureHandler = distributedLockAcquireFailureHandler;
        this.properties = properties;
    }

    /**
     * 在目标方法执行前后织入 Redis 分布式锁。
     *
     * @param pjp 切点对象
     * @param distributedLock 锁注解
     * @return 目标方法返回值
     * @throws Throwable 目标方法异常
     */
    @Around("@annotation(distributedLock)")
    public Object process(ProceedingJoinPoint pjp, DistributedLock distributedLock) throws Throwable {
        String lockKey = DistributedLockSupport.resolveFullLockKey(
                pjp,
                distributedLock,
                distributedLockKeyResolver,
                properties.getRedis().getKeyPrefix()
        );

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
                DistributedLockSupport.handleAcquireFailure(lockKey, distributedLock, distributedLockAcquireFailureHandler);
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
