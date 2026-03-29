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
package com.getboot.httpclient.infrastructure.resttemplate.autoconfigure;

import com.getboot.httpclient.api.properties.RestTemplateTraceProperties;
import com.getboot.httpclient.infrastructure.resttemplate.support.TraceRestTemplateInterceptor;
import com.getboot.httpclient.spi.resttemplate.RestTemplateTraceRequestCustomizer;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;

/**
 * RestTemplate HTTP 客户端自动配置。
 *
 * <p>负责注册 RestTemplate Trace 透传拦截器，并自动挂载到 Spring Boot 管理的 RestTemplate。</p>
 *
 * @author qiheng
 */
@AutoConfiguration
@ConditionalOnClass({RestTemplate.class, RestTemplateCustomizer.class, ClientHttpRequestInterceptor.class})
@EnableConfigurationProperties(RestTemplateTraceProperties.class)
@ConditionalOnProperty(prefix = "getboot.http-client.resttemplate.trace", name = "enabled", havingValue = "true", matchIfMissing = true)
public class RestTemplateHttpClientAutoConfiguration {

    /**
     * 注册 RestTemplate Trace 拦截器。
     *
     * @param properties Trace 配置
     * @param customizers 请求定制器列表
     * @return RestTemplate 拦截器
     */
    @Bean(name = "getbootTraceRestTemplateInterceptor")
    @ConditionalOnMissingBean(name = "getbootTraceRestTemplateInterceptor")
    public ClientHttpRequestInterceptor getbootTraceRestTemplateInterceptor(
            RestTemplateTraceProperties properties,
            ObjectProvider<RestTemplateTraceRequestCustomizer> customizers) {
        return new TraceRestTemplateInterceptor(properties, customizers.orderedStream().toList());
    }

    /**
     * 注册 RestTemplate 定制器。
     *
     * @param interceptor Trace 拦截器
     * @return RestTemplate 定制器
     */
    @Bean
    @ConditionalOnMissingBean(name = "getbootTraceRestTemplateCustomizer")
    public RestTemplateCustomizer getbootTraceRestTemplateCustomizer(ClientHttpRequestInterceptor interceptor) {
        return restTemplate -> restTemplate.getInterceptors().add(interceptor);
    }
}
