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
import com.getboot.limiter.api.model.LimiterAlgorithm;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RateLimitOperationResolverTest {

    private final RateLimitOperationResolver resolver = new RateLimitOperationResolver();

    @Test
    void shouldResolveSpelKeyAndRuntimeRuleFromAnnotation() throws NoSuchMethodException {
        Method method = DemoService.class.getDeclaredMethod("sendSms", String.class);
        RateLimit rateLimit = method.getAnnotation(RateLimit.class);

        RateLimitOperationResolver.RateLimitOperation operation =
                resolver.resolve(method, new Object[]{"13800000000"}, rateLimit);

        assertEquals("send-sms:13800000000", operation.limiterName());
        assertEquals(LimiterAlgorithm.TOKEN_BUCKET, operation.rule().getAlgorithm());
        assertEquals(1, operation.rule().getRate());
        assertEquals(60, operation.rule().getInterval());
        assertEquals(TimeUnit.SECONDS.name(), operation.rule().getIntervalUnit());
        assertEquals(1, operation.permits());
        assertEquals(0, operation.timeout());
        assertEquals(TimeUnit.SECONDS, operation.timeoutUnit());
    }

    @Test
    void shouldFallbackToMethodNameWhenSceneAndKeyAreBlank() throws NoSuchMethodException {
        Method method = DemoService.class.getDeclaredMethod("createOrder");
        RateLimit rateLimit = method.getAnnotation(RateLimit.class);

        RateLimitOperationResolver.RateLimitOperation operation =
                resolver.resolve(method, new Object[0], rateLimit);

        assertEquals("DemoService.createOrder", operation.limiterName());
        assertEquals(LimiterAlgorithm.SLIDING_WINDOW, operation.rule().getAlgorithm());
        assertEquals(10, operation.rule().getRate());
        assertEquals(1, operation.rule().getInterval());
        assertEquals(TimeUnit.MINUTES.name(), operation.rule().getIntervalUnit());
    }

    @Test
    void shouldResolveIndexedParameterAliases() throws NoSuchMethodException {
        Method method = DemoService.class.getDeclaredMethod("sendSmsWithIndexedKey", String.class);
        RateLimit rateLimit = method.getAnnotation(RateLimit.class);

        RateLimitOperationResolver.RateLimitOperation operation =
                resolver.resolve(method, new Object[]{"13800000000"}, rateLimit);

        assertEquals("send-sms-indexed:13800000000", operation.limiterName());
    }

    private static final class DemoService {

        @RateLimit(
                scene = "send-sms",
                keyExpression = "#phone",
                algorithm = LimiterAlgorithm.TOKEN_BUCKET,
                rate = 1,
                interval = 60,
                intervalUnit = TimeUnit.SECONDS,
                timeout = 0,
                timeoutUnit = TimeUnit.SECONDS
        )
        private void sendSms(String phone) {
        }

        @RateLimit(
                scene = "send-sms-indexed",
                keyExpression = "#p0",
                algorithm = LimiterAlgorithm.TOKEN_BUCKET,
                rate = 1,
                interval = 60,
                intervalUnit = TimeUnit.SECONDS
        )
        private void sendSmsWithIndexedKey(String phone) {
        }

        @RateLimit(rate = 10, interval = 1, intervalUnit = TimeUnit.MINUTES)
        private void createOrder() {
        }
    }
}
