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
package com.getboot.idempotency.support.aop;

import com.getboot.idempotency.api.annotation.Idempotent;
import com.getboot.idempotency.api.exception.IdempotencyException;
import com.getboot.idempotency.api.model.IdempotencyRecord;
import com.getboot.idempotency.api.properties.IdempotencyProperties;
import com.getboot.idempotency.spi.IdempotencyDuplicateRequestHandler;
import com.getboot.idempotency.spi.IdempotencyKeyResolver;
import com.getboot.idempotency.spi.IdempotencyStore;
import com.getboot.idempotency.support.IdempotencySupport;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

import java.lang.reflect.Method;
import java.time.Duration;

/**
 * Method-level idempotency aspect.
 *
 * @author qiheng
 */
@Aspect
public class IdempotencyAspect {

    private final IdempotencyStore idempotencyStore;
    private final IdempotencyKeyResolver idempotencyKeyResolver;
    private final IdempotencyDuplicateRequestHandler duplicateRequestHandler;
    private final IdempotencyProperties properties;

    public IdempotencyAspect(IdempotencyStore idempotencyStore,
                             IdempotencyKeyResolver idempotencyKeyResolver,
                             IdempotencyDuplicateRequestHandler duplicateRequestHandler,
                             IdempotencyProperties properties) {
        this.idempotencyStore = idempotencyStore;
        this.idempotencyKeyResolver = idempotencyKeyResolver;
        this.duplicateRequestHandler = duplicateRequestHandler;
        this.properties = properties;
    }

    @Around("@annotation(idempotent)")
    public Object around(ProceedingJoinPoint joinPoint, Idempotent idempotent) throws Throwable {
        Method method = IdempotencySupport.resolveMethod(joinPoint);
        String key = IdempotencySupport.resolveFullKey(
                joinPoint,
                method,
                idempotent,
                idempotencyKeyResolver,
                properties.resolveKeyPrefix()
        );
        Duration ttl = Duration.ofSeconds(
                IdempotencySupport.resolveTtlSeconds(idempotent, properties.getDefaultTtlSeconds())
        );

        IdempotencyRecord existingRecord = idempotencyStore.get(key);
        if (existingRecord != null) {
            return duplicateRequestHandler.handleDuplicate(key, existingRecord, idempotent);
        }

        if (!idempotencyStore.markProcessing(key, ttl)) {
            IdempotencyRecord latestRecord = idempotencyStore.get(key);
            if (latestRecord != null) {
                return duplicateRequestHandler.handleDuplicate(key, latestRecord, idempotent);
            }
            throw new IdempotencyException("Duplicate request detected but state is unavailable. key=" + key);
        }

        try {
            Object result = joinPoint.proceed();
            idempotencyStore.markCompleted(key, result, ttl);
            return result;
        } catch (Throwable ex) {
            idempotencyStore.delete(key);
            throw ex;
        }
    }
}
