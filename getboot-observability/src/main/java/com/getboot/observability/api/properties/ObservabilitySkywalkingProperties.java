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
 * SkyWalking 配置。
 *
 * <p>用于定义 SkyWalking TraceId 在 MDC 中的键名等行为。</p>
 *
 * @author qiheng
 */
@Data
@ConfigurationProperties(prefix = "getboot.observability.skywalking")
public class ObservabilitySkywalkingProperties {

    /**
     * 是否启用 SkyWalking 桥接。
     */
    private boolean enabled = true;

    /**
     * SkyWalking TraceId 写入 MDC 时使用的键名。
     */
    private String mdcKey = "skywalkingTraceId";
}
