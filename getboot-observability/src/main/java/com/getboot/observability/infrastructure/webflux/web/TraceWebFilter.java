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

    /**
     * Trace 配置。
     */
    private final ObservabilityTraceProperties traceProperties;

    /**
     * TraceId 生成器。
     */
    private final TraceIdGenerator traceIdGenerator;

    /**
     * 响应式 TraceId 解析器列表。
     */
    private final List<ReactiveTraceIdResolver> traceIdResolvers;

    /**
     * 响应式 Trace 上下文定制器列表。
     */
    private final List<ReactiveTraceContextCustomizer> traceContextCustomizers;

    /**
     * 创建 WebFlux Trace 过滤器。
     *
     * @param traceProperties Trace 配置
     * @param traceIdGenerator TraceId 生成器
     * @param traceIdResolvers 响应式 TraceId 解析器列表
     * @param traceContextCustomizers 响应式 Trace 上下文定制器列表
     */
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

    /**
     * 在订阅阶段绑定 Trace 上下文，并在链路结束后恢复现场。
     *
     * @param exchange 当前请求交换器
     * @param chain 过滤器链
     * @return 过滤执行结果
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String traceId = resolveTraceId(exchange);
        ServerWebExchange traceExchange = propagateTraceHeader(exchange, traceId);
        Map<String, String> mdcEntries = new LinkedHashMap<>();
        mdcEntries.put(traceProperties.getMdcKey(), traceId);

        ReactiveTraceContext traceContext = new ReactiveTraceContext(traceId, traceExchange);
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

        traceExchange.getAttributes().put(traceProperties.getRequestAttributeName(), traceId);
        if (traceProperties.isResponseHeaderEnabled() && StringUtils.hasText(traceProperties.getHeaderName())) {
            traceExchange.getResponse().getHeaders().set(traceProperties.getHeaderName(), traceId);
        }

        // WebFlux 过滤链在订阅时才真正执行，因此 ThreadLocal 绑定也要延后到订阅阶段。
        return Mono.defer(() -> {
            String previousTraceId = TraceContextHolder.bindTraceId(traceId);
            Map<String, String> previousMdcValues = applyMdcEntries(mdcEntries);
            return chain.filter(traceExchange)
                    .contextCapture()
                    .doFinally(signalType -> {
                        restoreMdc(previousMdcValues);
                        TraceContextHolder.restoreTraceId(previousTraceId);
                    });
        });
    }

    /**
     * 将 TraceId 补齐到请求头，便于后续转发链路复用同一标识。
     *
     * @param exchange 当前请求交换器
     * @param traceId 当前 TraceId
     * @return 透传请求头后的交换器
     */
    private ServerWebExchange propagateTraceHeader(ServerWebExchange exchange, String traceId) {
        String headerName = traceProperties.getHeaderName();
        if (!traceProperties.isRequestHeaderPropagationEnabled() || !StringUtils.hasText(headerName)) {
            return exchange;
        }
        return exchange.mutate()
                .request(builder -> builder.headers(headers -> headers.set(headerName, traceId)))
                .build();
    }

    /**
     * 解析当前请求应使用的 TraceId。
     *
     * @param exchange 当前请求交换器
     * @return TraceId
     */
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

    /**
     * 恢复过滤前的 MDC 状态。
     *
     * @param previousMdcValues 过滤前的 MDC 值
     */
    private void restoreMdc(Map<String, String> previousMdcValues) {
        previousMdcValues.forEach((key, previousValue) -> {
            if (previousValue == null) {
                MDC.remove(key);
                return;
            }
            MDC.put(key, previousValue);
        });
    }

    /**
     * 写入当前链路所需的 MDC 条目，并返回写入前的旧值。
     *
     * @param mdcEntries 待写入的 MDC 条目
     * @return 写入前的 MDC 值
     */
    private Map<String, String> applyMdcEntries(Map<String, String> mdcEntries) {
        Map<String, String> previousMdcValues = new LinkedHashMap<>();
        mdcEntries.forEach((key, value) -> {
            previousMdcValues.put(key, MDC.get(key));
            MDC.put(key, value);
        });
        return previousMdcValues;
    }
}
