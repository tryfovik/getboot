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
package com.getboot.observability.spi;

import com.getboot.observability.api.context.ReactiveTraceContext;
import java.util.Map;

/**
 * Reactive Trace 上下文定制器。
 *
 * <p>业务方可通过注册该类型 Bean，为 WebFlux 场景的日志 MDC 增加额外链路上下文字段。</p>
 *
 * @author qiheng
 */
@FunctionalInterface
public interface ReactiveTraceContextCustomizer {

    /**
     * 定制 Reactive Trace 上下文。
     *
     * @param traceContext Reactive Trace 上下文
     * @return 需要追加到 MDC 的键值对
     */
    Map<String, String> customize(ReactiveTraceContext traceContext);
}
