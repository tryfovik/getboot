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
package com.getboot.httpclient.infrastructure.feign.autoconfigure;

import com.getboot.httpclient.api.properties.OpenFeignTraceProperties;
import com.getboot.httpclient.infrastructure.feign.support.TraceFeignRequestInterceptor;
import com.getboot.httpclient.spi.feign.OpenFeignTraceRequestCustomizer;
import feign.RequestInterceptor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * OpenFeign HTTP 客户端自动配置。
 *
 * <p>负责注册 Feign TraceId 透传拦截器，并为业务方暴露可扩展的请求定制入口。</p>
 *
 * @author qiheng
 */
@AutoConfiguration
@ConditionalOnClass(RequestInterceptor.class)
@EnableConfigurationProperties(OpenFeignTraceProperties.class)
@ConditionalOnProperty(prefix = "getboot.http-client.openfeign.trace", name = "enabled", havingValue = "true", matchIfMissing = true)
public class OpenFeignHttpClientAutoConfiguration {

    /**
     * 注册 Feign TraceId 透传拦截器。
     *
     * @param traceProperties Feign Trace 配置
     * @param customizers 请求定制器列表
     * @return Feign 请求拦截器
     */
    @Bean
    @ConditionalOnMissingBean(name = "getbootTraceFeignRequestInterceptor")
    public RequestInterceptor getbootTraceFeignRequestInterceptor(
            OpenFeignTraceProperties traceProperties,
            ObjectProvider<OpenFeignTraceRequestCustomizer> customizers) {
        return new TraceFeignRequestInterceptor(traceProperties, customizers.orderedStream().toList());
    }
}
