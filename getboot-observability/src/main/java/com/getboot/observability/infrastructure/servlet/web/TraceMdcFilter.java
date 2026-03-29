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
package com.getboot.observability.infrastructure.servlet.web;

import com.getboot.observability.api.context.TraceContext;
import com.getboot.observability.api.properties.ObservabilityTraceProperties;
import com.getboot.observability.spi.TraceContextCustomizer;
import com.getboot.observability.spi.TraceIdGenerator;
import com.getboot.observability.spi.TraceIdResolver;
import com.getboot.support.api.trace.TraceContextHolder;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Trace 日志上下文过滤器。
 *
 * <p>负责解析或生成 TraceId，并将其写入请求属性、响应头与日志 MDC。</p>
 *
 * @author qiheng
 */
public class TraceMdcFilter extends OncePerRequestFilter {

    private final ObservabilityTraceProperties traceProperties;
    private final TraceIdGenerator traceIdGenerator;
    private final List<TraceIdResolver> traceIdResolvers;
    private final List<TraceContextCustomizer> traceContextCustomizers;

    public TraceMdcFilter(
            ObservabilityTraceProperties traceProperties,
            TraceIdGenerator traceIdGenerator,
            List<TraceIdResolver> traceIdResolvers,
            List<TraceContextCustomizer> traceContextCustomizers) {
        this.traceProperties = traceProperties;
        this.traceIdGenerator = traceIdGenerator;
        this.traceIdResolvers = traceIdResolvers == null ? List.of() : List.copyOf(traceIdResolvers);
        this.traceContextCustomizers = traceContextCustomizers == null ? List.of() : List.copyOf(traceContextCustomizers);
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        String traceId = resolveTraceId(request);
        String previousTraceId = TraceContextHolder.bindTraceId(traceId);
        Map<String, String> mdcEntries = new LinkedHashMap<>();
        mdcEntries.put(traceProperties.getMdcKey(), traceId);

        TraceContext traceContext = new TraceContext(traceId, request, response);
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
        request.setAttribute(traceProperties.getRequestAttributeName(), traceId);
        if (traceProperties.isResponseHeaderEnabled() && StringUtils.hasText(traceProperties.getHeaderName())) {
            response.setHeader(traceProperties.getHeaderName(), traceId);
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            restoreMdc(previousMdcValues);
            TraceContextHolder.restoreTraceId(previousTraceId);
        }
    }

    private String resolveTraceId(HttpServletRequest request) {
        for (TraceIdResolver traceIdResolver : traceIdResolvers) {
            String resolvedTraceId = traceIdResolver.resolve(request);
            if (StringUtils.hasText(resolvedTraceId)) {
                return resolvedTraceId.trim();
            }
        }
        String headerName = traceProperties.getHeaderName();
        if (StringUtils.hasText(headerName)) {
            String headerTraceId = request.getHeader(headerName);
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
