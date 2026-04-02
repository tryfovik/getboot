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

import io.micrometer.context.ContextRegistry;
import io.micrometer.context.ContextSnapshot;
import io.micrometer.context.ContextSnapshotFactory;

import java.util.concurrent.Callable;
import java.util.function.Supplier;

/**
 * Trace 上下文传播工具。
 *
 * <p>用于在手工线程池、{@code CompletableFuture} 等场景中显式包装任务，传播 TraceId 与 MDC 上下文。</p>
 *
 * @author qiheng
 */
public final class TraceContextPropagationSupport {

    /**
     * 默认上下文快照工厂。
     */
    private static final ContextSnapshotFactory CONTEXT_SNAPSHOT_FACTORY = ContextSnapshotFactory.builder()
            .contextRegistry(ContextRegistry.getInstance())
            .clearMissing(true)
            .build();

    /**
     * 禁止实例化工具类。
     */
    private TraceContextPropagationSupport() {
    }

    /**
     * 捕获当前线程上下文快照。
     *
     * @return 上下文快照
     */
    public static ContextSnapshot captureAll() {
        return CONTEXT_SNAPSHOT_FACTORY.captureAll();
    }

    /**
     * 包装 Runnable，使其在目标线程中恢复当前 Trace 上下文。
     *
     * @param runnable 原始任务
     * @return 包装后的任务
     */
    public static Runnable wrap(Runnable runnable) {
        ContextSnapshot snapshot = captureAll();
        return () -> {
            try (ContextSnapshot.Scope ignored = snapshot.setThreadLocals()) {
                runnable.run();
            }
        };
    }

    /**
     * 包装 Callable，使其在目标线程中恢复当前 Trace 上下文。
     *
     * @param callable 原始任务
     * @param <T> 返回值类型
     * @return 包装后的任务
     */
    public static <T> Callable<T> wrap(Callable<T> callable) {
        ContextSnapshot snapshot = captureAll();
        return () -> {
            try (ContextSnapshot.Scope ignored = snapshot.setThreadLocals()) {
                return callable.call();
            }
        };
    }

    /**
     * 包装 Supplier，使其在目标线程中恢复当前 Trace 上下文。
     *
     * @param supplier 原始任务
     * @param <T> 返回值类型
     * @return 包装后的任务
     */
    public static <T> Supplier<T> wrap(Supplier<T> supplier) {
        ContextSnapshot snapshot = captureAll();
        return () -> {
            try (ContextSnapshot.Scope ignored = snapshot.setThreadLocals()) {
                return supplier.get();
            }
        };
    }
}
