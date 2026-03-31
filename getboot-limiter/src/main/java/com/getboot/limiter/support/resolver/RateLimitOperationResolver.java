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
package com.getboot.limiter.support.resolver;

import com.getboot.limiter.api.annotation.RateLimit;
import com.getboot.limiter.api.exception.RateLimitException;
import com.getboot.limiter.api.model.LimiterRule;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;

/**
 * 注解限流操作解析器。
 *
 * <p>负责从 {@link RateLimit} 注解中解析运行时规则和最终限流 key。</p>
 *
 * @author qiheng
 */
public class RateLimitOperationResolver {

    private static final ExpressionParser PARSER = new SpelExpressionParser();
    private static final DefaultParameterNameDiscoverer PARAMETER_NAME_DISCOVERER =
            new DefaultParameterNameDiscoverer();

    public RateLimitOperation resolve(Method method, Object[] args, RateLimit rateLimit) {
        String scene = resolveScene(method, rateLimit);
        String key = resolveKey(method, args, rateLimit);
        String limiterName = StringUtils.hasText(key) ? scene + ":" + key : scene;

        LimiterRule limiterRule = new LimiterRule();
        limiterRule.setAlgorithm(rateLimit.algorithm());
        limiterRule.setRate(rateLimit.rate());
        limiterRule.setInterval(rateLimit.interval());
        limiterRule.setIntervalUnit(rateLimit.intervalUnit().name());

        return new RateLimitOperation(
                limiterName,
                limiterRule,
                rateLimit.permits(),
                rateLimit.timeout(),
                rateLimit.timeoutUnit()
        );
    }

    private String resolveScene(Method method, RateLimit rateLimit) {
        if (StringUtils.hasText(rateLimit.scene())) {
            return rateLimit.scene().trim();
        }
        return method.getDeclaringClass().getSimpleName() + "." + method.getName();
    }

    private String resolveKey(Method method, Object[] args, RateLimit rateLimit) {
        if (StringUtils.hasText(rateLimit.key())) {
            return rateLimit.key().trim();
        }
        if (!StringUtils.hasText(rateLimit.keyExpression())) {
            return "";
        }
        StandardEvaluationContext context = new StandardEvaluationContext();
        Object[] methodArgs = args == null ? new Object[0] : args;
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
        Object value = PARSER.parseExpression(rateLimit.keyExpression()).getValue(context);
        if (value == null) {
            throw new RateLimitException("Resolved rate limit key must not be null.");
        }
        return value.toString();
    }

    public record RateLimitOperation(
            String limiterName,
            LimiterRule rule,
            long permits,
            long timeout,
            java.util.concurrent.TimeUnit timeoutUnit
    ) {
    }
}
