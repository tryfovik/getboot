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
package com.getboot.observability.infrastructure.webflux.autoconfigure;

import com.getboot.observability.api.properties.ObservabilityTraceProperties;
import com.getboot.observability.infrastructure.autoconfigure.ObservabilityAutoConfiguration;
import com.getboot.observability.infrastructure.webflux.web.TraceWebFilter;
import com.getboot.observability.spi.ReactiveTraceContextCustomizer;
import com.getboot.observability.spi.ReactiveTraceIdResolver;
import com.getboot.observability.spi.TraceIdGenerator;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.server.WebFilter;

/**
 * Reactive 可观测性自动配置。
 *
 * <p>负责在 WebFlux 应用中注册 Trace WebFilter。</p>
 *
 * @author qiheng
 */
@AutoConfiguration(after = ObservabilityAutoConfiguration.class)
@ConditionalOnClass(WebFilter.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
@ConditionalOnProperty(prefix = "getboot.observability.trace", name = "enabled", havingValue = "true", matchIfMissing = true)
public class ReactiveObservabilityAutoConfiguration {

    /**
     * 注册 WebFlux Trace 过滤器。
     *
     * @param traceProperties Trace 配置
     * @param traceIdGenerator TraceId 生成器
     * @param traceIdResolvers Reactive TraceId 解析器列表
     * @param traceContextCustomizers Reactive Trace 上下文定制器列表
     * @return WebFilter
     */
    @Bean
    @ConditionalOnMissingBean(name = "traceWebFilter")
    public WebFilter traceWebFilter(
            ObservabilityTraceProperties traceProperties,
            TraceIdGenerator traceIdGenerator,
            ObjectProvider<ReactiveTraceIdResolver> traceIdResolvers,
            ObjectProvider<ReactiveTraceContextCustomizer> traceContextCustomizers) {
        return new TraceWebFilter(
                traceProperties,
                traceIdGenerator,
                traceIdResolvers.orderedStream().toList(),
                traceContextCustomizers.orderedStream().toList()
        );
    }
}
