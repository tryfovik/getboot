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
package com.getboot.observability.support;

import io.micrometer.context.ContextSnapshot;
import io.micrometer.context.ContextSnapshotFactory;
import org.springframework.core.task.TaskDecorator;

/**
 * Trace 任务装饰器。
 *
 * <p>用于在异步线程执行前恢复父线程的 TraceId 与 MDC 上下文。</p>
 *
 * @author qiheng
 */
public class TraceTaskDecorator implements TaskDecorator {

    private final ContextSnapshotFactory contextSnapshotFactory;

    public TraceTaskDecorator(ContextSnapshotFactory contextSnapshotFactory) {
        this.contextSnapshotFactory = contextSnapshotFactory;
    }

    @Override
    public Runnable decorate(Runnable runnable) {
        ContextSnapshot contextSnapshot = contextSnapshotFactory.captureAll();
        return () -> {
            try (ContextSnapshot.Scope ignored = contextSnapshot.setThreadLocals()) {
                runnable.run();
            }
        };
    }
}
