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
package com.getboot.limiter.support.registry;

import com.getboot.limiter.api.model.LimiterAlgorithm;
import com.getboot.limiter.api.model.LimiterRule;
import com.getboot.limiter.spi.RateLimiterAlgorithmHandler;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultRateLimiterRegistryTest {

    @Test
    void shouldRoutePredefinedLimiterToMatchingAlgorithm() {
        FakeHandler slidingWindowHandler = new FakeHandler(LimiterAlgorithm.SLIDING_WINDOW, Map.of());
        FakeHandler tokenBucketHandler = new FakeHandler(
                LimiterAlgorithm.TOKEN_BUCKET,
                Map.of("sms", rule(LimiterAlgorithm.TOKEN_BUCKET, 5, 1, "SECONDS"))
        );
        DefaultRateLimiterRegistry registry =
                new DefaultRateLimiterRegistry(List.of(slidingWindowHandler, tokenBucketHandler));

        assertTrue(registry.tryAcquire("sms"));
        assertEquals(0, slidingWindowHandler.tryAcquireCalls);
        assertEquals(1, tokenBucketHandler.tryAcquireCalls);
        assertEquals("sms", tokenBucketHandler.lastLimiterName);
        assertEquals(LimiterAlgorithm.TOKEN_BUCKET, tokenBucketHandler.lastRule.getAlgorithm());
    }

    @Test
    void shouldDefaultMissingAlgorithmToSlidingWindowWhenConfiguringRule() {
        FakeHandler slidingWindowHandler = new FakeHandler(LimiterAlgorithm.SLIDING_WINDOW, Map.of());
        FakeHandler tokenBucketHandler = new FakeHandler(LimiterAlgorithm.TOKEN_BUCKET, Map.of());
        DefaultRateLimiterRegistry registry =
                new DefaultRateLimiterRegistry(List.of(slidingWindowHandler, tokenBucketHandler));
        LimiterRule limiterRule = new LimiterRule();
        limiterRule.setRate(12);
        limiterRule.setInterval(2);
        limiterRule.setIntervalUnit("SECONDS");

        registry.configureRateLimiter("login", limiterRule);

        assertTrue(registry.tryAcquire("login", 2));
        assertEquals(1, slidingWindowHandler.tryAcquireCalls);
        assertEquals(2, slidingWindowHandler.lastPermits);
        assertEquals(LimiterAlgorithm.SLIDING_WINDOW, slidingWindowHandler.lastRule.getAlgorithm());
    }

    @Test
    void shouldFailFastWhenPredefinedLimiterNameDuplicatedAcrossAlgorithms() {
        FakeHandler slidingWindowHandler = new FakeHandler(
                LimiterAlgorithm.SLIDING_WINDOW,
                Map.of("duplicate", rule(LimiterAlgorithm.SLIDING_WINDOW, 10, 1, "SECONDS"))
        );
        FakeHandler tokenBucketHandler = new FakeHandler(
                LimiterAlgorithm.TOKEN_BUCKET,
                Map.of("duplicate", rule(LimiterAlgorithm.TOKEN_BUCKET, 5, 1, "SECONDS"))
        );

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> new DefaultRateLimiterRegistry(List.of(slidingWindowHandler, tokenBucketHandler)));

        assertTrue(exception.getMessage().contains("duplicate"));
    }

    @Test
    void shouldRouteRuntimeRuleWithoutPreconfiguration() {
        FakeHandler slidingWindowHandler = new FakeHandler(LimiterAlgorithm.SLIDING_WINDOW, Map.of());
        FakeHandler tokenBucketHandler = new FakeHandler(LimiterAlgorithm.TOKEN_BUCKET, Map.of());
        DefaultRateLimiterRegistry registry =
                new DefaultRateLimiterRegistry(List.of(slidingWindowHandler, tokenBucketHandler));

        assertTrue(registry.tryAcquire(
                "send-sms:13800000000",
                rule(LimiterAlgorithm.TOKEN_BUCKET, 1, 60, "SECONDS"),
                1,
                0,
                TimeUnit.SECONDS
        ));
        assertEquals(0, slidingWindowHandler.tryAcquireCalls);
        assertEquals(1, tokenBucketHandler.tryAcquireCalls);
        assertEquals("send-sms:13800000000", tokenBucketHandler.lastLimiterName);
        assertEquals(LimiterAlgorithm.TOKEN_BUCKET, tokenBucketHandler.lastRule.getAlgorithm());
    }

    private static LimiterRule rule(LimiterAlgorithm algorithm, long rate, long interval, String intervalUnit) {
        LimiterRule limiterRule = new LimiterRule();
        limiterRule.setAlgorithm(algorithm);
        limiterRule.setRate(rate);
        limiterRule.setInterval(interval);
        limiterRule.setIntervalUnit(intervalUnit);
        return limiterRule;
    }

    private static final class FakeHandler implements RateLimiterAlgorithmHandler {

        private final LimiterAlgorithm algorithm;
        private final Map<String, LimiterRule> predefinedRules;
        private final LimiterRule defaultRule;

        private int tryAcquireCalls;
        private String lastLimiterName;
        private LimiterRule lastRule;
        private long lastPermits;

        private FakeHandler(LimiterAlgorithm algorithm, Map<String, LimiterRule> predefinedRules) {
            this.algorithm = algorithm;
            this.predefinedRules = predefinedRules;
            this.defaultRule = rule(algorithm, 10, 1, "SECONDS");
        }

        @Override
        public LimiterAlgorithm algorithm() {
            return algorithm;
        }

        @Override
        public Map<String, LimiterRule> predefinedRules() {
            return predefinedRules;
        }

        @Override
        public LimiterRule defaultRule() {
            return defaultRule.copy();
        }

        @Override
        public long defaultTimeout() {
            return 1;
        }

        @Override
        public void validateRule(LimiterRule rule) {
            if (rule.getAlgorithm() != algorithm) {
                throw new IllegalArgumentException("Unexpected algorithm: " + rule.getAlgorithm());
            }
            if (rule.getRate() <= 0 || rule.getInterval() <= 0) {
                throw new IllegalArgumentException("Rate and interval must be greater than 0.");
            }
        }

        @Override
        public boolean tryAcquire(String limiterName, LimiterRule rule, long permits) {
            this.tryAcquireCalls++;
            this.lastLimiterName = limiterName;
            this.lastRule = rule.copy();
            this.lastPermits = permits;
            return true;
        }

        @Override
        public boolean delete(String limiterName) {
            return true;
        }
    }
}
