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
 * Shared distributed lock helper methods.
 *
 * @author qiheng
 */
public final class DistributedLockSupport {

    private DistributedLockSupport() {
    }

    public static String resolveFullLockKey(ProceedingJoinPoint joinPoint,
                                            DistributedLock distributedLock,
                                            DistributedLockKeyResolver keyResolver,
                                            String keyPrefix) {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        String resolvedKey = keyResolver.resolve(joinPoint, method, distributedLock);
        return buildFullLockKey(keyPrefix, distributedLock.scene(), resolvedKey);
    }

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

    public static void handleAcquireFailure(String lockKey,
                                            DistributedLock distributedLock,
                                            DistributedLockAcquireFailureHandler failureHandler) {
        failureHandler.onFailure(lockKey, distributedLock);
        throw new DistributedLockException(
                "Distributed lock acquire failure handler completed without throwing. key=" + lockKey
        );
    }

    public static long resolveZookeeperLeaseMs(DistributedLock distributedLock) {
        if (distributedLock.expireTime() != DistributedLockConstants.DEFAULT_EXPIRE_TIME) {
            throw new DistributedLockException(
                    "ZooKeeper distributed lock does not support explicit expireTime. "
                            + "Please keep expireTime=-1 to use session-based semantics."
            );
        }
        return DistributedLockConstants.DEFAULT_EXPIRE_TIME;
    }

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
