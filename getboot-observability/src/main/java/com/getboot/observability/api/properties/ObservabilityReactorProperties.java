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
 * Reactor 可观测配置。
 *
 * <p>用于定义 Reactor 链路的上下文传播开关。</p>
 *
 * @author qiheng
 */
@Data
@ConfigurationProperties(prefix = "getboot.observability.reactor")
public class ObservabilityReactorProperties {

    /**
     * 是否启用 Reactor Trace 上下文传播。
     */
    private boolean enabled = true;

    /**
     * 是否启用 Reactor 自动上下文传播钩子。
     */
    private boolean automaticContextPropagationEnabled = true;
}
