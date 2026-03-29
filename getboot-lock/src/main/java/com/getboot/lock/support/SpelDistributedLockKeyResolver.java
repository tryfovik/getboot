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
import com.getboot.lock.spi.DistributedLockKeyResolver;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.lang.reflect.Method;

/**
 * 基于 SpEL 的分布式锁键解析器。
 *
 * @author qiheng
 */
public class SpelDistributedLockKeyResolver implements DistributedLockKeyResolver {

    private static final ExpressionParser PARSER = new SpelExpressionParser();
    private static final DefaultParameterNameDiscoverer PARAMETER_NAME_DISCOVERER = new DefaultParameterNameDiscoverer();

    @Override
    public String resolve(ProceedingJoinPoint joinPoint, Method method, DistributedLock distributedLock) {
        String key = distributedLock.key();
        if (!DistributedLockConstants.NONE_KEY.equals(key)) {
            return key;
        }
        if (DistributedLockConstants.NONE_KEY.equals(distributedLock.keyExpression())) {
            throw new DistributedLockException("Distributed lock key must not be empty.");
        }
        StandardEvaluationContext context = new StandardEvaluationContext();
        Object[] args = joinPoint.getArgs();
        String[] parameterNames = PARAMETER_NAME_DISCOVERER.getParameterNames(method);
        if (parameterNames != null) {
            for (int i = 0; i < parameterNames.length; i++) {
                context.setVariable(parameterNames[i], args[i]);
            }
        }
        Object value = PARSER.parseExpression(distributedLock.keyExpression()).getValue(context);
        if (value == null) {
            throw new DistributedLockException("Resolved distributed lock key must not be null.");
        }
        return value.toString();
    }
}
