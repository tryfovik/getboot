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
package com.getboot.limiter.support.aop;

import com.getboot.limiter.api.annotation.RateLimit;
import com.getboot.limiter.api.exception.RateLimitException;
import com.getboot.limiter.api.registry.RateLimiterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;

/**
 * 方法级限流切面。
 *
 * <p>拦截带有 {@link RateLimit} 注解的方法，并委托限流注册表完成许可校验。</p>
 *
 * @author qiheng
 */
@Slf4j
@Aspect
@RequiredArgsConstructor
@ConditionalOnClass(name = "org.aspectj.lang.ProceedingJoinPoint")
public class RateLimitAspect {

    private final RateLimiterRegistry rateLimiterRegistry;

    @Around("@annotation(rateLimit)")
    public Object around(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
        boolean acquired = rateLimiterRegistry.tryAcquire(
                rateLimit.value(),
                rateLimit.permits(),
                rateLimit.timeout(),
                rateLimit.timeUnit()
        );
        if (!acquired) {
            String methodName = ((MethodSignature) joinPoint.getSignature()).getMethod().getName();
            log.warn("Method invocation throttled by sliding window limiter: {}.{}",
                    joinPoint.getTarget().getClass().getSimpleName(), methodName);
            throw new RateLimitException(rateLimit.message());
        }
        return joinPoint.proceed();
    }
}
