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
package com.getboot.observability.infrastructure.reactor.support;

import com.getboot.observability.api.properties.ObservabilityReactorProperties;
import org.springframework.beans.factory.InitializingBean;
import reactor.core.publisher.Hooks;

/**
 * Reactor 上下文传播初始化器。
 *
 * <p>用于在应用启动阶段启用 Reactor 自动上下文传播钩子。</p>
 *
 * @author qiheng
 */
public class ReactorContextPropagationInitializer implements InitializingBean {

    private final ObservabilityReactorProperties properties;

    public ReactorContextPropagationInitializer(ObservabilityReactorProperties properties) {
        this.properties = properties;
    }

    @Override
    public void afterPropertiesSet() {
        if (properties.isAutomaticContextPropagationEnabled()) {
            Hooks.enableAutomaticContextPropagation();
        }
    }
}
