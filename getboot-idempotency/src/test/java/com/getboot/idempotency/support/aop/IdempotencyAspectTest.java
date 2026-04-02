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
import com.getboot.idempotency.spi.IdempotencyStore;
import com.getboot.idempotency.support.DefaultIdempotencyDuplicateRequestHandler;
import com.getboot.idempotency.support.SpelIdempotencyKeyResolver;
import org.junit.jupiter.api.Test;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IdempotencyAspectTest {

    @Test
    void shouldReturnCachedResultForDuplicateInvocation() {
        InMemoryIdempotencyStore store = new InMemoryIdempotencyStore();
        OrderService target = new OrderService();
        OrderService proxy = createProxy(target, store);

        String first = proxy.process("order-1");
        String second = proxy.process("order-1");

        assertEquals("result-order-1", first);
        assertEquals(first, second);
        assertEquals(1, target.executions.get());
    }

    @Test
    void shouldRejectConcurrentInvocationForSameKeyWhileProcessing() throws Exception {
        InMemoryIdempotencyStore store = new InMemoryIdempotencyStore();
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        BlockingOrderService target = new BlockingOrderService(entered, release);
        BlockingOrderService proxy = createProxy(target, store);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Future<String> first = executor.submit(() -> proxy.process("order-2"));
            assertTrue(entered.await(2, TimeUnit.SECONDS));

            Future<String> second = executor.submit(() -> proxy.process("order-2"));
            ExecutionException exception = assertThrows(ExecutionException.class, second::get);
            assertTrue(exception.getCause() instanceof IdempotencyException);
            assertEquals("Request is already being processed.", exception.getCause().getMessage());

            release.countDown();
            assertEquals("result-order-2", first.get(2, TimeUnit.SECONDS));
            assertEquals(1, target.executions.get());
        } finally {
            release.countDown();
            executor.shutdownNow();
        }
    }

    @Test
    void shouldReleaseKeyWhenInvocationFails() {
        InMemoryIdempotencyStore store = new InMemoryIdempotencyStore();
        FlakyOrderService target = new FlakyOrderService();
        FlakyOrderService proxy = createProxy(target, store);

        assertThrows(IllegalStateException.class, () -> proxy.process("order-3"));

        String result = proxy.process("order-3");
        assertEquals("result-order-3", result);
        assertEquals(2, target.executions.get());
    }

    private <T> T createProxy(T target, IdempotencyStore store) {
        IdempotencyAspect aspect = new IdempotencyAspect(
                store,
                new SpelIdempotencyKeyResolver(),
                new DefaultIdempotencyDuplicateRequestHandler(),
                createProperties()
        );
        AspectJProxyFactory proxyFactory = new AspectJProxyFactory(target);
        proxyFactory.addAspect(aspect);
        return proxyFactory.getProxy();
    }

    private IdempotencyProperties createProperties() {
        IdempotencyProperties properties = new IdempotencyProperties();
        properties.setDefaultTtlSeconds(300);
        return properties;
    }

    static class OrderService {

        private final AtomicInteger executions = new AtomicInteger();

        @Idempotent(scene = "order", keyExpression = "#orderNo")
        public String process(String orderNo) {
            executions.incrementAndGet();
            return "result-" + orderNo;
        }
    }

    static class BlockingOrderService {

        private final CountDownLatch entered;
        private final CountDownLatch release;
        private final AtomicInteger executions = new AtomicInteger();

        BlockingOrderService(CountDownLatch entered, CountDownLatch release) {
            this.entered = entered;
            this.release = release;
        }

        @Idempotent(scene = "order", keyExpression = "#orderNo", message = "Request is already being processed.")
        public String process(String orderNo) {
            executions.incrementAndGet();
            entered.countDown();
            try {
                release.await(2, TimeUnit.SECONDS);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException(ex);
            }
            return "result-" + orderNo;
        }
    }

    static class FlakyOrderService {

        private final AtomicInteger executions = new AtomicInteger();
        private final AtomicBoolean first = new AtomicBoolean(true);

        @Idempotent(scene = "order", keyExpression = "#orderNo")
        public String process(String orderNo) {
            executions.incrementAndGet();
            if (first.compareAndSet(true, false)) {
                throw new IllegalStateException("boom");
            }
            return "result-" + orderNo;
        }
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
