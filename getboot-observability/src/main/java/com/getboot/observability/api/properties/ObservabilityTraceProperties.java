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
package com.getboot.observability.api.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Trace 链路配置。
 *
 * <p>用于定义 Trace 请求头、响应头与 MDC 相关行为。</p>
 *
 * @author qiheng
 */
@Data
@ConfigurationProperties(prefix = "getboot.observability.trace")
public class ObservabilityTraceProperties {

    /**
     * 是否启用 Trace 过滤器。
     */
    private boolean enabled = true;

    /**
     * Trace 请求头名称。
     */
    private String headerName = "X-Trace-Id";

    /**
     * 是否在响应头中回写 TraceId。
     */
    private boolean responseHeaderEnabled = true;

    /**
     * 日志 MDC 中使用的键名。
     */
    private String mdcKey = "traceId";

    /**
     * 请求属性中保存 TraceId 的键名。
     */
    private String requestAttributeName = "GETBOOT_TRACE_ID";

    /**
     * 是否启用异步线程 Trace 上下文透传。
     */
    private boolean asyncPropagationEnabled = true;
}
