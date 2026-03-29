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
package com.getboot.httpclient.infrastructure.feign.support;

import com.getboot.httpclient.api.properties.OpenFeignTraceProperties;
import com.getboot.httpclient.spi.feign.OpenFeignTraceRequestCustomizer;
import com.getboot.support.api.trace.TraceContextHolder;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * Feign Trace 请求拦截器。
 *
 * <p>用于将当前线程中的 TraceId 自动写入 Feign 出站请求头，保障 HTTP 调用链路可追踪。</p>
 *
 * @author qiheng
 */
public class TraceFeignRequestInterceptor implements RequestInterceptor {

    private final OpenFeignTraceProperties traceProperties;
    private final List<OpenFeignTraceRequestCustomizer> customizers;

    public TraceFeignRequestInterceptor(
            OpenFeignTraceProperties traceProperties,
            List<OpenFeignTraceRequestCustomizer> customizers) {
        this.traceProperties = traceProperties;
        this.customizers = customizers == null ? List.of() : List.copyOf(customizers);
    }

    @Override
    public void apply(RequestTemplate requestTemplate) {
        String traceId = TraceContextHolder.getTraceId();
        if (!StringUtils.hasText(traceId)) {
            return;
        }
        String traceHeaderName = traceProperties.getHeaderName();
        requestTemplate.removeHeader(traceHeaderName);
        requestTemplate.header(traceHeaderName, traceId);
        customizers.forEach(customizer -> customizer.customize(requestTemplate, traceId, traceHeaderName));
    }
}
