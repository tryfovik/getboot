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
import com.getboot.idempotency.api.exception.IdempotencyException;
import com.getboot.idempotency.spi.IdempotencyKeyResolver;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;

/**
 * SpEL-based idempotency key resolver.
 *
 * @author qiheng
 */
public class SpelIdempotencyKeyResolver implements IdempotencyKeyResolver {

    private static final ExpressionParser PARSER = new SpelExpressionParser();
    private static final DefaultParameterNameDiscoverer PARAMETER_NAME_DISCOVERER =
            new DefaultParameterNameDiscoverer();

    @Override
    public String resolve(ProceedingJoinPoint joinPoint, Method method, Idempotent idempotent) {
        if (StringUtils.hasText(idempotent.key())) {
            return idempotent.key().trim();
        }
        if (!StringUtils.hasText(idempotent.keyExpression())) {
            throw new IdempotencyException("Idempotent key must not be empty.");
        }

        Object[] methodArgs = joinPoint.getArgs() == null ? new Object[0] : joinPoint.getArgs();
        StandardEvaluationContext context = new StandardEvaluationContext();
        for (int i = 0; i < methodArgs.length; i++) {
            context.setVariable("p" + i, methodArgs[i]);
            context.setVariable("a" + i, methodArgs[i]);
        }
        String[] parameterNames = PARAMETER_NAME_DISCOVERER.getParameterNames(method);
        if (parameterNames != null) {
            for (int i = 0; i < parameterNames.length && i < methodArgs.length; i++) {
                context.setVariable(parameterNames[i], methodArgs[i]);
            }
        }
        context.setVariable("args", methodArgs);

        Object value = PARSER.parseExpression(idempotent.keyExpression()).getValue(context);
        if (value == null) {
            throw new IdempotencyException("Resolved idempotent key must not be null.");
        }
        return value.toString();
    }
}
