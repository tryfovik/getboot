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
package com.getboot.lock.infrastructure.database.jdbc.aspect;

import com.getboot.lock.api.annotation.DistributedLock;
import com.getboot.lock.api.constant.DistributedLockConstants;
import com.getboot.lock.api.exception.DistributedLockException;
import com.getboot.lock.api.properties.LockProperties;
import com.getboot.lock.infrastructure.database.jdbc.support.JdbcDistributedLockRepository;
import com.getboot.lock.spi.DistributedLockAcquireFailureHandler;
import com.getboot.lock.spi.DistributedLockKeyResolver;
import com.getboot.lock.support.DistributedLockSupport;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;

import java.time.Instant;
import java.util.UUID;

/**
 * JDBC 分布式锁切面。
 *
 * @author qiheng
 */
@Aspect
@Order(Integer.MIN_VALUE + 1)
public class JdbcDistributedLockAspect {

    /**
     * 日志记录器。
     */
    private static final Logger LOG = LoggerFactory.getLogger(JdbcDistributedLockAspect.class);

    /**
     * JDBC 锁仓储。
     */
    private final JdbcDistributedLockRepository repository;

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
     * 创建 JDBC 分布式锁切面。
     *
     * @param repository JDBC 锁仓储
     * @param distributedLockKeyResolver 锁 key 解析器
     * @param distributedLockAcquireFailureHandler 锁获取失败处理器
     * @param properties 锁配置属性
     */
    public JdbcDistributedLockAspect(JdbcDistributedLockRepository repository,
                                     DistributedLockKeyResolver distributedLockKeyResolver,
                                     DistributedLockAcquireFailureHandler distributedLockAcquireFailureHandler,
                                     LockProperties properties) {
        this.repository = repository;
        this.distributedLockKeyResolver = distributedLockKeyResolver;
        this.distributedLockAcquireFailureHandler = distributedLockAcquireFailureHandler;
        this.properties = properties;
    }

    /**
     * 在目标方法执行前后织入 JDBC 分布式锁。
     *
     * @param joinPoint 切点对象
     * @param distributedLock 锁注解
     * @return 目标方法返回值
     * @throws Throwable 目标方法异常
     */
    @Around("@annotation(distributedLock)")
    public Object process(ProceedingJoinPoint joinPoint, DistributedLock distributedLock) throws Throwable {
        String lockKey = DistributedLockSupport.resolveFullLockKey(
                joinPoint,
                distributedLock,
                distributedLockKeyResolver,
                properties.getDatabase().getKeyPrefix()
        );
        long leaseMs = DistributedLockSupport.resolveLeaseMs(distributedLock, properties.getDatabase().getLeaseMs());
        long waitTimeMs = DistributedLockSupport.resolveWaitTimeMs(distributedLock);
        long retryIntervalMs = Math.max(1L, properties.getDatabase().getRetryIntervalMs());
        long deadline = waitTimeMs == DistributedLockConstants.DEFAULT_WAIT_TIME
                ? Long.MAX_VALUE
                : System.currentTimeMillis() + waitTimeMs;
        String ownerId = UUID.randomUUID().toString();

        while (true) {
            Instant now = Instant.now();
            if (repository.tryAcquire(lockKey, ownerId, now.plusMillis(leaseMs), now)) {
                LOG.info("Database distributed lock acquired. key={}, leaseMs={}", lockKey, leaseMs);
                try {
                    return joinPoint.proceed();
                } finally {
                    repository.release(lockKey, ownerId);
                    LOG.info("Database distributed lock released. key={}, leaseMs={}", lockKey, leaseMs);
                }
            }

            if (System.currentTimeMillis() >= deadline) {
                LOG.warn("Failed to acquire database distributed lock. key={}, leaseMs={}", lockKey, leaseMs);
                DistributedLockSupport.handleAcquireFailure(
                        lockKey,
                        distributedLock,
                        distributedLockAcquireFailureHandler
                );
            }

            long sleepMs = deadline == Long.MAX_VALUE
                    ? retryIntervalMs
                    : Math.min(retryIntervalMs, Math.max(1L, deadline - System.currentTimeMillis()));
            try {
                Thread.sleep(sleepMs);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw new DistributedLockException("Interrupted while acquiring database distributed lock.", ex);
            }
        }
    }
}
