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
package com.getboot.httpclient.api.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * OpenFeign Trace 配置。
 *
 * <p>用于定义 Feign 出站请求中 TraceId 透传行为。</p>
 *
 * @author qiheng
 */
@Data
@ConfigurationProperties(prefix = "getboot.http-client.openfeign.trace")
public class OpenFeignTraceProperties {

    /**
     * 是否启用 Feign TraceId 透传。
     */
    private boolean enabled = true;

    /**
     * 出站请求头名称。
     */
    private String headerName = "X-Trace-Id";
}
