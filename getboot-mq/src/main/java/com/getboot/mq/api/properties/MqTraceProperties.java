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
package com.getboot.mq.api.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * MQ Trace 配置。
 *
 * <p>定义消息能力层统一的 Trace 透传参数。</p>
 *
 * @author qiheng
 */
@Data
@ConfigurationProperties(prefix = "getboot.mq.trace")
public class MqTraceProperties {

    /**
     * 是否启用 MQ TraceId 透传。
     */
    private boolean enabled = true;

    /**
     * MQ 消息头中保存 TraceId 的键名。
     */
    private String headerName = "TRACE_ID";

    /**
     * 日志 MDC 中使用的键名。
     */
    private String mdcKey = "traceId";
}
