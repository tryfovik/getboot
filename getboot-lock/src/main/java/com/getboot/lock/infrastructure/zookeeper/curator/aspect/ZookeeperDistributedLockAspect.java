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
package com.getboot.lock.infrastructure.zookeeper.curator.aspect;

import com.getboot.lock.api.annotation.DistributedLock;
import com.getboot.lock.api.constant.DistributedLockConstants;
import com.getboot.lock.api.exception.DistributedLockException;
import com.getboot.lock.api.properties.LockProperties;
import com.getboot.lock.spi.DistributedLockAcquireFailureHandler;
import com.getboot.lock.spi.DistributedLockKeyResolver;
import com.getboot.lock.support.DistributedLockSupport;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;

import java.util.concurrent.TimeUnit;

/**
 * 基于 Curator 的 ZooKeeper 分布式锁切面。
 *
 * @author qiheng
 */
@Aspect
@Order(Integer.MIN_VALUE + 1)
public class ZookeeperDistributedLockAspect {

    /**
     * 日志记录器。
     */
    private static final Logger LOG = LoggerFactory.getLogger(ZookeeperDistributedLockAspect.class);

    /**
     * Curator 客户端。
     */
    private final CuratorFramework curatorFramework;

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
     * 创建 ZooKeeper 分布式锁切面。
     *
     * @param curatorFramework Curator 客户端
     * @param distributedLockKeyResolver 锁 key 解析器
     * @param distributedLockAcquireFailureHandler 锁获取失败处理器
     * @param properties 锁配置属性
     */
    public ZookeeperDistributedLockAspect(CuratorFramework curatorFramework,
                                          DistributedLockKeyResolver distributedLockKeyResolver,
                                          DistributedLockAcquireFailureHandler distributedLockAcquireFailureHandler,
                                          LockProperties properties) {
        this.curatorFramework = curatorFramework;
        this.distributedLockKeyResolver = distributedLockKeyResolver;
        this.distributedLockAcquireFailureHandler = distributedLockAcquireFailureHandler;
        this.properties = properties;
    }

    /**
     * 在目标方法执行前后织入 ZooKeeper 分布式锁。
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
                properties.getZookeeper().getKeyPrefix()
        );
        DistributedLockSupport.resolveZookeeperLeaseMs(distributedLock);
        long waitTimeMs = DistributedLockSupport.resolveWaitTimeMs(distributedLock);
        String lockPath = DistributedLockSupport.buildZookeeperLockPath(
                properties.getZookeeper().getBasePath(),
                lockKey
        );

        InterProcessMutex mutex = new InterProcessMutex(curatorFramework, lockPath);
        boolean acquired = false;
        try {
            if (waitTimeMs == DistributedLockConstants.DEFAULT_WAIT_TIME) {
                LOG.info("Trying to acquire ZooKeeper distributed lock. key={}, path={}", lockKey, lockPath);
                mutex.acquire();
                acquired = true;
            } else {
                LOG.info("Trying to acquire ZooKeeper distributed lock. key={}, path={}, waitTimeMs={}",
                        lockKey, lockPath, waitTimeMs);
                acquired = mutex.acquire(waitTimeMs, TimeUnit.MILLISECONDS);
            }
        } catch (Exception ex) {
            if (ex instanceof InterruptedException) {
                Thread.currentThread().interrupt();
                throw new DistributedLockException("Interrupted while acquiring ZooKeeper distributed lock.", ex);
            }
            throw new DistributedLockException("Failed to acquire ZooKeeper distributed lock. key=" + lockKey, ex);
        }

        if (!acquired) {
            LOG.warn("Failed to acquire ZooKeeper distributed lock. key={}, path={}", lockKey, lockPath);
            DistributedLockSupport.handleAcquireFailure(
                    lockKey,
                    distributedLock,
                    distributedLockAcquireFailureHandler
            );
        }

        LOG.info("ZooKeeper distributed lock acquired. key={}, path={}", lockKey, lockPath);
        try {
            return joinPoint.proceed();
        } finally {
            if (mutex.isOwnedByCurrentThread()) {
                try {
                    mutex.release();
                    LOG.info("ZooKeeper distributed lock released. key={}, path={}", lockKey, lockPath);
                } catch (Exception ex) {
                    if (ex instanceof InterruptedException) {
                        Thread.currentThread().interrupt();
                    }
                    throw new DistributedLockException(
                            "Failed to release ZooKeeper distributed lock. key=" + lockKey,
                            ex
                    );
                }
            }
        }
    }
}
