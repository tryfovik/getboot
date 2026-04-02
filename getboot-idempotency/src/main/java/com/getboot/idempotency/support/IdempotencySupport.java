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
package com.getboot.idempotency.support;

import com.getboot.idempotency.api.annotation.Idempotent;
import com.getboot.idempotency.api.constant.IdempotencyConstants;
import com.getboot.idempotency.api.exception.IdempotencyException;
import com.getboot.idempotency.spi.IdempotencyKeyResolver;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.aop.support.AopUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;

/**
 * Shared idempotency helper methods.
 *
 * @author qiheng
 */
public final class IdempotencySupport {

    private IdempotencySupport() {
    }

    public static Method resolveMethod(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Object target = joinPoint.getTarget();
        if (target == null) {
            return method;
        }
        return AopUtils.getMostSpecificMethod(method, target.getClass());
    }

    public static String resolveFullKey(ProceedingJoinPoint joinPoint,
                                        Method method,
                                        Idempotent idempotent,
                                        IdempotencyKeyResolver keyResolver,
                                        String keyPrefix) {
        String resolvedKey = keyResolver.resolve(joinPoint, method, idempotent);
        String scene = resolveScene(method, idempotent);
        return buildFullKey(keyPrefix, scene, resolvedKey);
    }

    public static String resolveScene(Method method, Idempotent idempotent) {
        if (StringUtils.hasText(idempotent.scene())) {
            return idempotent.scene().trim();
        }
        return method.getDeclaringClass().getSimpleName() + "." + method.getName();
    }

    public static String buildFullKey(String keyPrefix, String scene, String resolvedKey) {
        if (!StringUtils.hasText(keyPrefix)) {
            throw new IdempotencyException("Idempotent key prefix must not be empty.");
        }
        if (!StringUtils.hasText(scene)) {
            throw new IdempotencyException("Idempotent scene must not be empty.");
        }
        if (!StringUtils.hasText(resolvedKey)) {
            throw new IdempotencyException("Resolved idempotent key must not be empty.");
        }
        return keyPrefix + ":" + scene + "#" + resolvedKey;
    }

    public static long resolveTtlSeconds(Idempotent idempotent, long defaultTtlSeconds) {
        long ttlSeconds = idempotent.ttlSeconds();
        if (ttlSeconds == IdempotencyConstants.USE_DEFAULT_TTL_SECONDS) {
            ttlSeconds = defaultTtlSeconds;
        }
        if (ttlSeconds <= 0) {
            throw new IdempotencyException("Idempotent ttlSeconds must be positive.");
        }
        return ttlSeconds;
    }
}
