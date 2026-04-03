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
package com.getboot.lock.support;

import com.getboot.lock.api.annotation.DistributedLock;
import com.getboot.lock.api.constant.DistributedLockConstants;
import com.getboot.lock.api.exception.DistributedLockException;
import com.getboot.lock.spi.DistributedLockAcquireFailureHandler;
import com.getboot.lock.spi.DistributedLockKeyResolver;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * 分布式锁公共辅助方法。
 *
 * @author qiheng
 */
public final class DistributedLockSupport {

    /**
     * 工具类私有构造方法。
     */
    private DistributedLockSupport() {
    }

    /**
     * 解析完整锁 key。
     *
     * @param joinPoint 切点对象
     * @param distributedLock 锁注解
     * @param keyResolver 锁 key 解析器
     * @param keyPrefix 锁 key 前缀
     * @return 完整锁 key
     */
    public static String resolveFullLockKey(ProceedingJoinPoint joinPoint,
                                            DistributedLock distributedLock,
                                            DistributedLockKeyResolver keyResolver,
                                            String keyPrefix) {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        String resolvedKey = keyResolver.resolve(joinPoint, method, distributedLock);
        return buildFullLockKey(keyPrefix, distributedLock.scene(), resolvedKey);
    }

    /**
     * 拼接完整锁 key。
     *
     * @param keyPrefix 锁 key 前缀
     * @param scene 业务场景
     * @param resolvedKey 已解析的业务 key
     * @return 完整锁 key
     */
    public static String buildFullLockKey(String keyPrefix, String scene, String resolvedKey) {
        if (!StringUtils.hasText(keyPrefix)) {
            throw new DistributedLockException("Distributed lock key prefix must not be empty.");
        }
        if (!StringUtils.hasText(scene)) {
            throw new DistributedLockException("Distributed lock scene must not be empty.");
        }
        if (!StringUtils.hasText(resolvedKey)) {
            throw new DistributedLockException("Resolved distributed lock key must not be empty.");
        }
        return keyPrefix + ":" + scene + "#" + resolvedKey;
    }

    /**
     * 解析锁租约时长。
     *
     * @param distributedLock 锁注解
     * @param defaultLeaseMs 默认租约时长
     * @return 最终租约时长
     */
    public static long resolveLeaseMs(DistributedLock distributedLock, long defaultLeaseMs) {
        int expireTime = distributedLock.expireTime();
        if (expireTime == DistributedLockConstants.DEFAULT_EXPIRE_TIME) {
            if (defaultLeaseMs <= 0) {
                throw new DistributedLockException("Distributed lock default lease must be positive.");
            }
            return defaultLeaseMs;
        }
        if (expireTime <= 0) {
            throw new DistributedLockException("Distributed lock expireTime must be positive.");
        }
        return expireTime;
    }

    /**
     * 解析等待时长。
     *
     * @param distributedLock 锁注解
     * @return 等待时长
     */
    public static long resolveWaitTimeMs(DistributedLock distributedLock) {
        int waitTime = distributedLock.waitTime();
        if (waitTime == DistributedLockConstants.DEFAULT_WAIT_TIME) {
            return DistributedLockConstants.DEFAULT_WAIT_TIME;
        }
        if (waitTime < 0) {
            throw new DistributedLockException("Distributed lock waitTime must be zero or positive.");
        }
        return waitTime;
    }

    /**
     * 处理锁获取失败场景。
     *
     * @param lockKey 完整锁 key
     * @param distributedLock 锁注解
     * @param failureHandler 失败处理器
     */
    public static void handleAcquireFailure(String lockKey,
                                            DistributedLock distributedLock,
                                            DistributedLockAcquireFailureHandler failureHandler) {
        failureHandler.onFailure(lockKey, distributedLock);
        throw new DistributedLockException(
                "Distributed lock acquire failure handler completed without throwing. key=" + lockKey
        );
    }

    /**
     * 校验 ZooKeeper 锁租约配置。
     *
     * @param distributedLock 锁注解
     * @return 默认占位值
     */
    public static long resolveZookeeperLeaseMs(DistributedLock distributedLock) {
        if (distributedLock.expireTime() != DistributedLockConstants.DEFAULT_EXPIRE_TIME) {
            throw new DistributedLockException(
                    "ZooKeeper distributed lock does not support explicit expireTime. "
                            + "Please keep expireTime=-1 to use session-based semantics."
            );
        }
        return DistributedLockConstants.DEFAULT_EXPIRE_TIME;
    }

    /**
     * 构建 ZooKeeper 锁路径。
     *
     * @param basePath 根路径
     * @param lockKey 完整锁 key
     * @return 锁节点路径
     */
    public static String buildZookeeperLockPath(String basePath, String lockKey) {
        if (!StringUtils.hasText(basePath)) {
            throw new DistributedLockException("ZooKeeper basePath must not be empty.");
        }
        if (!StringUtils.hasText(lockKey)) {
            throw new DistributedLockException("ZooKeeper lock key must not be empty.");
        }

        String normalizedBasePath = normalizeZookeeperBasePath(basePath);
        String encodedLockKey = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(lockKey.getBytes(StandardCharsets.UTF_8));
        return normalizedBasePath + "/" + encodedLockKey;
    }

    /**
     * 规范化 ZooKeeper 根路径。
     *
     * @param basePath 原始根路径
     * @return 规范化后的根路径
     */
    private static String normalizeZookeeperBasePath(String basePath) {
        String normalized = basePath.trim();
        if (!normalized.startsWith("/")) {
            throw new DistributedLockException("ZooKeeper basePath must start with '/'.");
        }
        while (normalized.endsWith("/") && normalized.length() > 1) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        if ("/".equals(normalized)) {
            throw new DistributedLockException("ZooKeeper basePath must not be root path '/'.");
        }
        return normalized;
    }
}
