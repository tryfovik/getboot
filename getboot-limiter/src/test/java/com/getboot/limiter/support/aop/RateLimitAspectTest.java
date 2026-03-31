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
import com.getboot.limiter.api.model.LimiterAlgorithm;
import com.getboot.limiter.api.model.LimiterRule;
import com.getboot.limiter.api.registry.RateLimiterRegistry;
import com.getboot.limiter.support.resolver.RateLimitOperationResolver;
import org.junit.jupiter.api.Test;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RateLimitAspectTest {

    @Test
    void shouldResolveImplementationMethodForJdkProxy() {
        CapturingRateLimiterRegistry registry = new CapturingRateLimiterRegistry();
        RateLimitAspect aspect = new RateLimitAspect(registry, new RateLimitOperationResolver());
        SmsService target = new SmsServiceImpl();

        AspectJProxyFactory proxyFactory = new AspectJProxyFactory(target);
        proxyFactory.setInterfaces(SmsService.class);
        proxyFactory.addAspect(aspect);

        SmsService proxy = proxyFactory.getProxy();
        proxy.send("13800000000");

        assertEquals("send-sms:13800000000", registry.limiterName);
        assertEquals(LimiterAlgorithm.TOKEN_BUCKET, registry.rule.getAlgorithm());
        assertEquals(1, registry.permits);
        assertEquals(0, registry.timeout);
        assertEquals(TimeUnit.SECONDS, registry.timeUnit);
    }

    interface SmsService {

        void send(String phone);
    }

    static final class SmsServiceImpl implements SmsService {

        @Override
        @RateLimit(
                scene = "send-sms",
                keyExpression = "#phone",
                algorithm = LimiterAlgorithm.TOKEN_BUCKET,
                rate = 1,
                interval = 60,
                intervalUnit = TimeUnit.SECONDS
        )
        public void send(String phone) {
        }
    }

    static final class CapturingRateLimiterRegistry implements RateLimiterRegistry {

        private String limiterName;
        private LimiterRule rule;
        private long permits;
        private long timeout;
        private TimeUnit timeUnit;

        @Override
        public boolean tryAcquire(String limiterName, LimiterRule rule, long permits, long timeout, TimeUnit timeUnit) {
            this.limiterName = limiterName;
            this.rule = rule;
            this.permits = permits;
            this.timeout = timeout;
            this.timeUnit = timeUnit;
            return true;
        }

        @Override
        public void configureRateLimiter(String limiterName, LimiterRule config) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean tryAcquire(String limiterName) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean tryAcquire(String limiterName, long permits) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean tryAcquire(String limiterName, long timeout, TimeUnit timeUnit) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean tryAcquire(String limiterName, long permits, long timeout, TimeUnit timeUnit) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void updateRateLimiterConfig(String limiterName, LimiterRule newConfig) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean deleteRateLimiter(String limiterName) {
            throw new UnsupportedOperationException();
        }
    }
}
