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

    /**
     * Micrometer 上下文中的 TraceId 键名。
     */
    public static final String KEY = "getbootTraceId";

    /**
     * 返回当前访问器在 Micrometer 上下文中的键。
     *
     * @return 上下文键
     */
    @Override
    public Object key() {
        return KEY;
    }

    /**
     * 读取当前线程中的 TraceId。
     *
     * @return 当前 TraceId
     */
    @Override
    public String getValue() {
        return TraceContextHolder.getTraceId();
    }

    /**
     * 向当前线程写入 TraceId。
     *
     * @param value TraceId
     */
    @Override
    public void setValue(String value) {
        TraceContextHolder.bindTraceId(value);
    }

    /**
     * 清理当前线程中的 TraceId。
     */
    @Override
    public void setValue() {
        TraceContextHolder.clear();
    }
}
