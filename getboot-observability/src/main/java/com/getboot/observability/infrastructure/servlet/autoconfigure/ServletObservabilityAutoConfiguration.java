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
package com.getboot.observability.infrastructure.servlet.autoconfigure;

import com.getboot.observability.api.properties.ObservabilityTraceProperties;
import com.getboot.observability.infrastructure.autoconfigure.ObservabilityAutoConfiguration;
import com.getboot.observability.infrastructure.servlet.web.TraceMdcFilter;
import com.getboot.observability.spi.TraceContextCustomizer;
import com.getboot.observability.spi.TraceIdGenerator;
import com.getboot.observability.spi.TraceIdResolver;
import jakarta.servlet.Filter;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Servlet 可观测性自动配置。
 *
 * <p>负责在 Servlet Web 应用中注册 Trace 过滤器。</p>
 *
 * @author qiheng
 */
@AutoConfiguration(after = ObservabilityAutoConfiguration.class)
@ConditionalOnClass({Filter.class, OncePerRequestFilter.class})
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnProperty(prefix = "getboot.observability.trace", name = "enabled", havingValue = "true", matchIfMissing = true)
public class ServletObservabilityAutoConfiguration {

    /**
     * 注册 Trace 过滤器。
     *
     * @param traceProperties Trace 配置
     * @param traceIdGenerator TraceId 生成器
     * @param traceIdResolvers TraceId 解析器列表
     * @param traceContextCustomizers Trace 上下文定制器列表
     * @return Filter 注册器
     */
    @Bean
    @ConditionalOnMissingBean(name = "traceMdcFilter")
    public FilterRegistrationBean<TraceMdcFilter> traceMdcFilter(
            ObservabilityTraceProperties traceProperties,
            TraceIdGenerator traceIdGenerator,
            ObjectProvider<TraceIdResolver> traceIdResolvers,
            ObjectProvider<TraceContextCustomizer> traceContextCustomizers) {
        FilterRegistrationBean<TraceMdcFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(
                new TraceMdcFilter(
                        traceProperties,
                        traceIdGenerator,
                        traceIdResolvers.orderedStream().toList(),
                        traceContextCustomizers.orderedStream().toList()
                )
        );
        registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        registrationBean.addUrlPatterns("/*");
        return registrationBean;
    }
}
