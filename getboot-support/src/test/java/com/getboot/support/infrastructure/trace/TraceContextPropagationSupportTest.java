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
package com.getboot.support.infrastructure.trace;

import com.getboot.support.api.trace.TraceContextHolder;
import io.micrometer.context.ContextRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * {@link TraceContextPropagationSupport} 测试。
 *
 * @author qiheng
 */
class TraceContextPropagationSupportTest {

    /**
     * 清理线程与全局注册器状态，避免污染其他测试。
     */
    @AfterEach
    void tearDown() {
        TraceContextHolder.clear();
        ContextRegistry.getInstance().removeThreadLocalAccessor(TraceContextThreadLocalAccessor.KEY);
    }

    /**
     * 验证包装后的 Runnable、Callable 与 Supplier 都会恢复捕获时的 Trace 上下文。
     *
     * @throws Exception 调用 Callable 失败
     */
    @Test
    void shouldWrapTasksWithCapturedTraceContext() throws Exception {
        ContextRegistry.getInstance().removeThreadLocalAccessor(TraceContextThreadLocalAccessor.KEY);
        ContextRegistry.getInstance().registerThreadLocalAccessor(new TraceContextThreadLocalAccessor());

        TraceContextHolder.bindTraceId("trace-captured");

        AtomicReference<String> runnableTraceId = new AtomicReference<>();
        Runnable runnable = TraceContextPropagationSupport.wrap(() ->
                runnableTraceId.set(TraceContextHolder.getTraceId()));
        Callable<String> callable =
                TraceContextPropagationSupport.wrap((Callable<String>) TraceContextHolder::getTraceId);
        Supplier<String> supplier =
                TraceContextPropagationSupport.wrap((Supplier<String>) TraceContextHolder::getTraceId);

        TraceContextHolder.clear();

        runnable.run();
        assertEquals("trace-captured", runnableTraceId.get());
        assertNull(TraceContextHolder.getTraceId());

        assertEquals("trace-captured", callable.call());
        assertNull(TraceContextHolder.getTraceId());

        assertEquals("trace-captured", supplier.get());
        assertNull(TraceContextHolder.getTraceId());
    }
}
