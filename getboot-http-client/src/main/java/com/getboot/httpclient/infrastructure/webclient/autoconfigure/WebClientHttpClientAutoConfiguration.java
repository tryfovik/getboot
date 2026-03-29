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
package com.getboot.httpclient.infrastructure.webclient.autoconfigure;

import com.getboot.httpclient.api.properties.WebClientTraceProperties;
import com.getboot.httpclient.infrastructure.webclient.support.TraceWebClientFilterFunction;
import com.getboot.httpclient.spi.webclient.WebClientTraceRequestCustomizer;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.reactive.function.client.WebClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;

/**
 * WebClient HTTP 客户端自动配置。
 *
 * <p>负责注册 WebClient Trace 透传过滤器，并自动挂载到 Spring Boot 管理的 WebClient.Builder。</p>
 *
 * @author qiheng
 */
@AutoConfiguration
@ConditionalOnClass({ExchangeFilterFunction.class, WebClientCustomizer.class})
@EnableConfigurationProperties(WebClientTraceProperties.class)
@ConditionalOnProperty(prefix = "getboot.http-client.webclient.trace", name = "enabled", havingValue = "true", matchIfMissing = true)
public class WebClientHttpClientAutoConfiguration {

    /**
     * 注册 WebClient Trace 过滤器。
     *
     * @param properties Trace 配置
     * @param customizers 请求定制器列表
     * @return 过滤器
     */
    @Bean(name = "getbootTraceWebClientFilterFunction")
    @ConditionalOnMissingBean(name = "getbootTraceWebClientFilterFunction")
    public ExchangeFilterFunction getbootTraceWebClientFilterFunction(
            WebClientTraceProperties properties,
            ObjectProvider<WebClientTraceRequestCustomizer> customizers) {
        return new TraceWebClientFilterFunction(properties, customizers.orderedStream().toList());
    }

    /**
     * 注册 WebClient 定制器。
     *
     * @param traceFilterFunction Trace 过滤器
     * @return WebClient 定制器
     */
    @Bean
    @ConditionalOnMissingBean(name = "getbootTraceWebClientCustomizer")
    public WebClientCustomizer getbootTraceWebClientCustomizer(ExchangeFilterFunction traceFilterFunction) {
        return webClientBuilder -> webClientBuilder.filter(traceFilterFunction);
    }
}
