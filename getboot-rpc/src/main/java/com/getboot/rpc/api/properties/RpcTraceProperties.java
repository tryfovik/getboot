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
package com.getboot.rpc.api.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * RPC Trace 配置。
 *
 * <p>用于定义 Dubbo 链路 TraceId 透传的开关与日志键名。</p>
 *
 * @author qiheng
 */
@Data
@ConfigurationProperties(prefix = "getboot.rpc.trace")
public class RpcTraceProperties {

    /**
     * 是否启用 Dubbo TraceId 透传。
     */
    private boolean enabled = true;

    /**
     * 日志 MDC 中使用的键名。
     */
    private String mdcKey = "traceId";
}
