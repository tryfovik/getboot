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
package com.getboot.httpclient.infrastructure.webclient.support;

import com.getboot.httpclient.api.properties.WebClientTraceProperties;
import com.getboot.httpclient.spi.webclient.WebClientTraceRequestCustomizer;
import com.getboot.support.api.trace.TraceContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * WebClient Trace 过滤器。
 *
 * <p>用于将当前线程中的 TraceId 自动写入 WebClient 出站请求头。</p>
 *
 * @author qiheng
 */
public class TraceWebClientFilterFunction implements ExchangeFilterFunction {

    private final WebClientTraceProperties properties;
    private final List<WebClientTraceRequestCustomizer> customizers;

    public TraceWebClientFilterFunction(
            WebClientTraceProperties properties,
            List<WebClientTraceRequestCustomizer> customizers) {
        this.properties = properties;
        this.customizers = customizers == null ? List.of() : List.copyOf(customizers);
    }

    @Override
    public Mono<ClientResponse> filter(ClientRequest request, ExchangeFunction next) {
        String traceId = TraceContextHolder.getTraceId();
        if (!StringUtils.hasText(traceId)) {
            return next.exchange(request);
        }
        String traceHeaderName = properties.getHeaderName();
        ClientRequest.Builder requestBuilder = ClientRequest.from(request);
        requestBuilder.headers(headers -> {
            headers.remove(traceHeaderName);
            headers.add(traceHeaderName, traceId);
            customizers.forEach(customizer -> customizer.customize(headers, request, traceId, traceHeaderName));
        });
        return next.exchange(requestBuilder.build());
    }
}
