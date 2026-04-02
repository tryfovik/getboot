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
package com.getboot.webhook.support.processor;

import com.getboot.exception.api.code.CommonErrorCode;
import com.getboot.exception.api.exception.BusinessException;
import com.getboot.idempotency.api.model.IdempotencyRecord;
import com.getboot.idempotency.spi.IdempotencyStore;
import com.getboot.limiter.api.limiter.RateLimiter;
import com.getboot.webhook.infrastructure.servlet.filter.CachedBodyHttpServletRequest;
import com.getboot.webhook.support.validator.WebhookRequestValidator;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultWebhookRequestProcessorTest {

    @Test
    void shouldReuseCachedWebhookResultForDuplicateRequest() throws Exception {
        WebhookRequestValidator validator = createValidator();
        InMemoryIdempotencyStore store = new InMemoryIdempotencyStore();
        DefaultWebhookRequestProcessor processor =
                new DefaultWebhookRequestProcessor(validator, allowAllRateLimiter(), store);
        AtomicInteger executions = new AtomicInteger();
        HttpServletRequest request = createRequest("{\"order\":\"1\"}");

        String first = processor.handle(
                "demo-app",
                "limit:order-1",
                10,
                "webhook:",
                "checksum",
                "1710000000",
                request,
                () -> {
                    executions.incrementAndGet();
                    return "ok";
                },
                appKey -> "fingerprint:" + appKey
        );
        String second = processor.handle(
                "demo-app",
                "limit:order-1",
                10,
                "webhook:",
                "checksum",
                "1710000000",
                request,
                () -> {
                    executions.incrementAndGet();
                    return "should-not-run";
                },
                appKey -> "fingerprint:" + appKey
        );

        assertEquals("ok", first);
        assertEquals("ok", second);
        assertEquals(1, executions.get());
    }

    @Test
    void shouldRejectConcurrentWebhookRequestWhileFirstOneIsProcessing() throws Exception {
        WebhookRequestValidator validator = createValidator();
        InMemoryIdempotencyStore store = new InMemoryIdempotencyStore();
        DefaultWebhookRequestProcessor processor =
                new DefaultWebhookRequestProcessor(validator, allowAllRateLimiter(), store);
        AtomicInteger executions = new AtomicInteger();
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        HttpServletRequest request = createRequest("{\"order\":\"2\"}");

        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Future<String> first = executor.submit(() -> processor.handle(
                    "demo-app",
                    "limit:order-2",
                    10,
                    "webhook:",
                    "checksum",
                    "1710000001",
                    request,
                    () -> {
                        executions.incrementAndGet();
                        entered.countDown();
                        try {
                            release.await(2, TimeUnit.SECONDS);
                        } catch (InterruptedException ex) {
                            Thread.currentThread().interrupt();
                            throw new IllegalStateException(ex);
                        }
                        return "ok";
                    },
                    appKey -> "fingerprint:" + appKey
            ));
            assertTrue(entered.await(2, TimeUnit.SECONDS));

            Future<String> second = executor.submit(() -> processor.handle(
                    "demo-app",
                    "limit:order-2",
                    10,
                    "webhook:",
                    "checksum",
                    "1710000001",
                    request,
                    () -> "should-not-run",
                    appKey -> "fingerprint:" + appKey
            ));
            ExecutionException exception = assertThrows(ExecutionException.class, second::get);
            assertTrue(exception.getCause() instanceof BusinessException);
            assertEquals(
                    CommonErrorCode.REQUEST_PROCESSING.code(),
                    ((BusinessException) exception.getCause()).getErrorCodeValue()
            );

            release.countDown();
            assertEquals("ok", first.get(2, TimeUnit.SECONDS));
            assertEquals(1, executions.get());
        } finally {
            release.countDown();
            executor.shutdownNow();
        }
    }

    private WebhookRequestValidator createValidator() {
        return new WebhookRequestValidator(appKey -> "secret") {
            @Override
            public void validateRequest(String checksum, String appKey, String time, String requestBody) {
            }
        };
    }

    private RateLimiter allowAllRateLimiter() {
        return (key, limit, windowSize) -> true;
    }

    private HttpServletRequest createRequest(String body) throws IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCharacterEncoding("UTF-8");
        request.setContent(body.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        return new CachedBodyHttpServletRequest(request);
    }

    static class InMemoryIdempotencyStore implements IdempotencyStore {

        private final Map<String, IdempotencyRecord> records = new ConcurrentHashMap<>();

        @Override
        public IdempotencyRecord get(String key) {
            return records.get(key);
        }

        @Override
        public boolean markProcessing(String key, Duration ttl) {
            return records.putIfAbsent(key, IdempotencyRecord.processing()) == null;
        }

        @Override
        public void markCompleted(String key, Object result, Duration ttl) {
            records.put(key, IdempotencyRecord.completed(result));
        }

        @Override
        public void delete(String key) {
            records.remove(key);
        }
    }
}
