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
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.SourceLocation;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SpelIdempotencyKeyResolverTest {

    private final SpelIdempotencyKeyResolver resolver = new SpelIdempotencyKeyResolver();

    @Test
    void shouldResolveSpelKeyFromMethodArguments() throws Exception {
        Method method = SampleService.class.getMethod("process", String.class, OrderRequest.class);
        Idempotent idempotent = method.getAnnotation(Idempotent.class);

        String key = resolver.resolve(
                new StaticProceedingJoinPoint("order-1", new OrderRequest("user-1")),
                method,
                idempotent
        );

        assertEquals("order-1:user-1", key);
    }

    @Test
    void shouldPreferFixedKeyOverExpression() throws Exception {
        Method method = SampleService.class.getMethod("processWithFixedKey", String.class);
        Idempotent idempotent = method.getAnnotation(Idempotent.class);

        String key = resolver.resolve(new StaticProceedingJoinPoint("order-2"), method, idempotent);

        assertEquals("fixed-key", key);
    }

    @Test
    void shouldRejectNullResolvedKey() throws Exception {
        Method method = SampleService.class.getMethod("processWithNullKey", String.class);
        Idempotent idempotent = method.getAnnotation(Idempotent.class);

        assertThrows(
                IdempotencyException.class,
                () -> resolver.resolve(new StaticProceedingJoinPoint("order-3"), method, idempotent)
        );
    }

    static class SampleService {

        @Idempotent(keyExpression = "#orderNo + ':' + #request.userId")
        public void process(String orderNo, OrderRequest request) {
        }

        @Idempotent(key = "fixed-key", keyExpression = "#orderNo")
        public void processWithFixedKey(String orderNo) {
        }

        @Idempotent(keyExpression = "#missing")
        public void processWithNullKey(String orderNo) {
        }
    }

    static class OrderRequest {

        private final String userId;

        OrderRequest(String userId) {
            this.userId = userId;
        }

        public String getUserId() {
            return userId;
        }
    }

    static class StaticProceedingJoinPoint implements ProceedingJoinPoint {

        private final Object[] args;

        StaticProceedingJoinPoint(Object... args) {
            this.args = args;
        }

        @Override
        public Object proceed() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Object proceed(Object[] args) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Object getThis() {
            return null;
        }

        @Override
        public Object getTarget() {
            return null;
        }

        @Override
        public Object[] getArgs() {
            return args;
        }

        @Override
        public Signature getSignature() {
            return null;
        }

        @Override
        public SourceLocation getSourceLocation() {
            return null;
        }

        @Override
        public String getKind() {
            return null;
        }

        @Override
        public StaticPart getStaticPart() {
            return null;
        }

        @Override
        public String toShortString() {
            return "static";
        }

        @Override
        public String toLongString() {
            return "static";
        }

        @Override
        public void set$AroundClosure(org.aspectj.runtime.internal.AroundClosure arc) {
        }
    }
}
