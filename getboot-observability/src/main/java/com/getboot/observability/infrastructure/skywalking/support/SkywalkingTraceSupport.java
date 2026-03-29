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
package com.getboot.observability.infrastructure.skywalking.support;

import org.springframework.util.StringUtils;

/**
 * SkyWalking Trace 支撑工具。
 *
 * <p>用于以反射方式读取 SkyWalking 当前链路 TraceId，避免对工具包形成强编译依赖。</p>
 *
 * @author qiheng
 */
public final class SkywalkingTraceSupport {

    private static final String TRACE_CONTEXT_CLASS_NAME = "org.apache.skywalking.apm.toolkit.trace.TraceContext";
    private static final String UNAVAILABLE_TRACE_ID = "N/A";

    private SkywalkingTraceSupport() {
    }

    /**
     * 解析当前 SkyWalking TraceId。
     *
     * @return SkyWalking TraceId，不存在时返回 {@code null}
     */
    public static String resolveTraceId() {
        try {
            Class<?> traceContextClass = Class.forName(TRACE_CONTEXT_CLASS_NAME);
            Object traceId = traceContextClass.getMethod("traceId").invoke(null);
            if (traceId == null) {
                return null;
            }
            String resolvedTraceId = traceId.toString();
            if (!StringUtils.hasText(resolvedTraceId) || UNAVAILABLE_TRACE_ID.equalsIgnoreCase(resolvedTraceId)) {
                return null;
            }
            return resolvedTraceId;
        } catch (Exception exception) {
            return null;
        }
    }
}
