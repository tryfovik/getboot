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
package com.getboot.observability.infrastructure.reactor.autoconfigure;

import com.getboot.observability.api.properties.ObservabilityReactorProperties;
import com.getboot.observability.infrastructure.autoconfigure.ObservabilityAutoConfiguration;
import com.getboot.observability.infrastructure.reactor.support.ReactorContextPropagationInitializer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Reactor 可观测性自动配置。
 *
 * <p>负责启用 Reactor 自动上下文传播能力，保障 TraceId 与 MDC 在响应式线程切换中继续可用。</p>
 *
 * @author qiheng
 */
@AutoConfiguration(after = ObservabilityAutoConfiguration.class)
@ConditionalOnClass(name = "reactor.core.publisher.Hooks")
@EnableConfigurationProperties(ObservabilityReactorProperties.class)
@ConditionalOnProperty(prefix = "getboot.observability.reactor", name = "enabled", havingValue = "true", matchIfMissing = true)
public class ReactorObservabilityAutoConfiguration {

    /**
     * 注册 Reactor 自动上下文传播初始化器。
     *
     * @param properties Reactor 配置
     * @return 初始化器
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "getboot.observability.reactor", name = "automatic-context-propagation-enabled", havingValue = "true", matchIfMissing = true)
    public ReactorContextPropagationInitializer reactorContextPropagationInitializer(ObservabilityReactorProperties properties) {
        return new ReactorContextPropagationInitializer(properties);
    }
}
