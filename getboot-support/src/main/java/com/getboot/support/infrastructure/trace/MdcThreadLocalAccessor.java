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

import io.micrometer.context.ThreadLocalAccessor;
import org.slf4j.MDC;

import java.util.Map;

/**
 * MDC 线程变量访问器。
 *
 * <p>用于将日志 MDC 上下文接入 Micrometer 上下文传播机制。</p>
 *
 * @author qiheng
 */
public class MdcThreadLocalAccessor implements ThreadLocalAccessor<Map<String, String>> {

    /**
     * Micrometer 上下文中的 MDC 键名。
     */
    public static final String KEY = "getbootMdc";

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
     * 读取当前线程中的 MDC 值。
     *
     * @return MDC 副本
     */
    @Override
    public Map<String, String> getValue() {
        return MDC.getCopyOfContextMap();
    }

    /**
     * 向当前线程写入 MDC 值。
     *
     * @param value 待写入的 MDC 值
     */
    @Override
    public void setValue(Map<String, String> value) {
        if (value == null || value.isEmpty()) {
            MDC.clear();
            return;
        }
        MDC.setContextMap(value);
    }

    /**
     * 清空当前线程中的 MDC 值。
     */
    @Override
    public void setValue() {
        MDC.clear();
    }
}
