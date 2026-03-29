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
package com.getboot.httpclient.infrastructure.resttemplate.support;

import com.getboot.httpclient.api.properties.RestTemplateTraceProperties;
import com.getboot.httpclient.spi.resttemplate.RestTemplateTraceRequestCustomizer;
import com.getboot.support.api.trace.TraceContextHolder;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.List;

/**
 * RestTemplate Trace 拦截器。
 *
 * <p>用于将当前线程中的 TraceId 自动写入 RestTemplate 出站请求头。</p>
 *
 * @author qiheng
 */
public class TraceRestTemplateInterceptor implements ClientHttpRequestInterceptor {

    private final RestTemplateTraceProperties properties;
    private final List<RestTemplateTraceRequestCustomizer> customizers;

    public TraceRestTemplateInterceptor(
            RestTemplateTraceProperties properties,
            List<RestTemplateTraceRequestCustomizer> customizers) {
        this.properties = properties;
        this.customizers = customizers == null ? List.of() : List.copyOf(customizers);
    }

    @Override
    public ClientHttpResponse intercept(
            HttpRequest request,
            byte[] body,
            ClientHttpRequestExecution execution) throws IOException {
        String traceId = TraceContextHolder.getTraceId();
        if (StringUtils.hasText(traceId)) {
            String traceHeaderName = properties.getHeaderName();
            request.getHeaders().remove(traceHeaderName);
            request.getHeaders().add(traceHeaderName, traceId);
            customizers.forEach(customizer -> customizer.customize(request.getHeaders(), request, traceId, traceHeaderName));
        }
        return execution.execute(request, body);
    }
}
