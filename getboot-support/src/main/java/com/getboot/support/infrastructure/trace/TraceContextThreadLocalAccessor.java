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
import io.micrometer.context.ThreadLocalAccessor;

/**
 * Trace 上下文线程变量访问器。
 *
 * <p>用于将 {@link TraceContextHolder} 接入 Micrometer 上下文传播机制。</p>
 *
 * @author qiheng
 */
public class TraceContextThreadLocalAccessor implements ThreadLocalAccessor<String> {

    public static final String KEY = "getbootTraceId";

    @Override
    public Object key() {
        return KEY;
    }

    @Override
    public String getValue() {
        return TraceContextHolder.getTraceId();
    }

    @Override
    public void setValue(String value) {
        TraceContextHolder.bindTraceId(value);
    }

    @Override
    public void setValue() {
        TraceContextHolder.clear();
    }
}
