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
package com.getboot.support.api.trace;

import org.springframework.core.NamedThreadLocal;
import org.springframework.util.StringUtils;

/**
 * Trace 上下文持有器。
 *
 * <p>用于在当前线程内保存与读取 TraceId，供 Web、Dubbo、Feign、MQ 等链路组件共享。</p>
 *
 * @author qiheng
 */
public final class TraceContextHolder {

    /**
     * 当前线程 TraceId 容器。
     */
    private static final ThreadLocal<String> TRACE_ID_HOLDER = new NamedThreadLocal<>("getbootTraceId");

    /**
     * 禁止实例化工具类。
     */
    private TraceContextHolder() {
    }

    /**
     * 获取当前线程中的 TraceId。
     *
     * @return 当前 TraceId，不存在时返回 {@code null}
     */
    public static String getTraceId() {
        return TRACE_ID_HOLDER.get();
    }

    /**
     * 绑定当前线程 TraceId，并返回绑定前的旧值。
     *
     * @param traceId 待绑定的 TraceId
     * @return 绑定前的 TraceId
     */
    public static String bindTraceId(String traceId) {
        String previousTraceId = TRACE_ID_HOLDER.get();
        if (StringUtils.hasText(traceId)) {
            TRACE_ID_HOLDER.set(traceId.trim());
            return previousTraceId;
        }
        TRACE_ID_HOLDER.remove();
        return previousTraceId;
    }

    /**
     * 恢复当前线程 TraceId。
     *
     * @param traceId 待恢复的 TraceId
     */
    public static void restoreTraceId(String traceId) {
        if (StringUtils.hasText(traceId)) {
            TRACE_ID_HOLDER.set(traceId);
            return;
        }
        TRACE_ID_HOLDER.remove();
    }

    /**
     * 清理当前线程中的 TraceId。
     */
    public static void clear() {
        TRACE_ID_HOLDER.remove();
    }
}
