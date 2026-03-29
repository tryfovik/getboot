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
package com.getboot.observability.infrastructure.webflux.web;

import com.getboot.observability.api.context.ReactiveTraceContext;
import com.getboot.observability.api.properties.ObservabilityTraceProperties;
import com.getboot.observability.spi.ReactiveTraceContextCustomizer;
import com.getboot.observability.spi.ReactiveTraceIdResolver;
import com.getboot.observability.spi.TraceIdGenerator;
import com.getboot.support.api.trace.TraceContextHolder;
import org.slf4j.MDC;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * WebFlux Trace 过滤器。
 *
 * <p>负责解析或生成 TraceId，并将其写入请求属性、响应头、日志 MDC 与 Reactor 上下文。</p>
 *
 * @author qiheng
 */
public class TraceWebFilter implements WebFilter {

    private final ObservabilityTraceProperties traceProperties;
    private final TraceIdGenerator traceIdGenerator;
    private final List<ReactiveTraceIdResolver> traceIdResolvers;
    private final List<ReactiveTraceContextCustomizer> traceContextCustomizers;

    public TraceWebFilter(
            ObservabilityTraceProperties traceProperties,
            TraceIdGenerator traceIdGenerator,
            List<ReactiveTraceIdResolver> traceIdResolvers,
            List<ReactiveTraceContextCustomizer> traceContextCustomizers) {
        this.traceProperties = traceProperties;
        this.traceIdGenerator = traceIdGenerator;
        this.traceIdResolvers = traceIdResolvers == null ? List.of() : List.copyOf(traceIdResolvers);
        this.traceContextCustomizers = traceContextCustomizers == null ? List.of() : List.copyOf(traceContextCustomizers);
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String traceId = resolveTraceId(exchange);
        String previousTraceId = TraceContextHolder.bindTraceId(traceId);
        Map<String, String> mdcEntries = new LinkedHashMap<>();
        mdcEntries.put(traceProperties.getMdcKey(), traceId);

        ReactiveTraceContext traceContext = new ReactiveTraceContext(traceId, exchange);
        traceContextCustomizers.forEach(customizer -> {
            Map<String, String> customizedEntries = customizer.customize(traceContext);
            if (customizedEntries == null || customizedEntries.isEmpty()) {
                return;
            }
            customizedEntries.forEach((key, value) -> {
                if (StringUtils.hasText(key) && StringUtils.hasText(value)) {
                    mdcEntries.put(key, value);
                }
            });
        });

        Map<String, String> previousMdcValues = new LinkedHashMap<>();
        mdcEntries.forEach((key, value) -> {
            previousMdcValues.put(key, MDC.get(key));
            MDC.put(key, value);
        });
        exchange.getAttributes().put(traceProperties.getRequestAttributeName(), traceId);
        if (traceProperties.isResponseHeaderEnabled() && StringUtils.hasText(traceProperties.getHeaderName())) {
            exchange.getResponse().getHeaders().set(traceProperties.getHeaderName(), traceId);
        }

        try {
            return chain.filter(exchange).contextCapture();
        } finally {
            restoreMdc(previousMdcValues);
            TraceContextHolder.restoreTraceId(previousTraceId);
        }
    }

    private String resolveTraceId(ServerWebExchange exchange) {
        for (ReactiveTraceIdResolver traceIdResolver : traceIdResolvers) {
            String resolvedTraceId = traceIdResolver.resolve(exchange);
            if (StringUtils.hasText(resolvedTraceId)) {
                return resolvedTraceId.trim();
            }
        }
        String headerName = traceProperties.getHeaderName();
        if (StringUtils.hasText(headerName)) {
            String headerTraceId = exchange.getRequest().getHeaders().getFirst(headerName);
            if (StringUtils.hasText(headerTraceId)) {
                return headerTraceId.trim();
            }
        }
        return traceIdGenerator.generate();
    }

    private void restoreMdc(Map<String, String> previousMdcValues) {
        previousMdcValues.forEach((key, previousValue) -> {
            if (previousValue == null) {
                MDC.remove(key);
                return;
            }
            MDC.put(key, previousValue);
        });
    }
}
