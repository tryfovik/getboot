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
package com.getboot.mq.infrastructure.rocketmq.aop;

import com.getboot.mq.api.properties.MqTraceProperties;
import com.getboot.mq.infrastructure.rocketmq.support.RocketMqTraceContextSupport;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

/**
 * RocketMQ Trace 监听切面。
 *
 * <p>用于在消息消费与事务回查时自动恢复 Trace 上下文，保障日志链路连续。</p>
 *
 * @author qiheng
 */
@Aspect
public class RocketMqTraceListenerAspect {

    private final RocketMqTraceContextSupport traceContextSupport;

    public RocketMqTraceListenerAspect(MqTraceProperties traceProperties) {
        this.traceContextSupport = new RocketMqTraceContextSupport(traceProperties);
    }

    @Around("this(org.apache.rocketmq.spring.core.RocketMQListener) && execution(* *.onMessage(..))")
    public Object aroundRocketMqListener(ProceedingJoinPoint joinPoint) throws Throwable {
        return proceedWithTrace(joinPoint);
    }

    @Around("this(org.apache.rocketmq.spring.core.RocketMQLocalTransactionListener) && "
            + "(execution(* *.executeLocalTransaction(..)) || execution(* *.checkLocalTransaction(..)))")
    public Object aroundRocketMqLocalTransactionListener(ProceedingJoinPoint joinPoint) throws Throwable {
        return proceedWithTrace(joinPoint);
    }

    private Object proceedWithTrace(ProceedingJoinPoint joinPoint) throws Throwable {
        if (!traceContextSupport.isEnabled()) {
            return joinPoint.proceed();
        }
        String traceId = resolveTraceId(joinPoint.getArgs());
        try (RocketMqTraceContextSupport.TraceScope ignored = traceContextSupport.openScope(traceId)) {
            return joinPoint.proceed();
        }
    }

    private String resolveTraceId(Object[] arguments) {
        if (arguments == null) {
            return null;
        }
        for (Object argument : arguments) {
            String traceId = traceContextSupport.resolveInboundTraceId(argument);
            if (traceId != null) {
                return traceId;
            }
        }
        return null;
    }
}
