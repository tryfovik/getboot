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
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.slf4j.MDC;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.util.Collections;
import java.util.Enumeration;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
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

    /**
     * Trace 配置。
     */
    private final ObservabilityTraceProperties traceProperties;

    /**
     * TraceId 生成器。
     */
    private final TraceIdGenerator traceIdGenerator;

    /**
     * TraceId 解析器列表。
     */
    private final List<TraceIdResolver> traceIdResolvers;

    /**
     * Trace 上下文定制器列表。
     */
    private final List<TraceContextCustomizer> traceContextCustomizers;

    /**
     * 创建 Servlet Trace 过滤器。
     *
     * @param traceProperties Trace 配置
     * @param traceIdGenerator TraceId 生成器
     * @param traceIdResolvers TraceId 解析器列表
     * @param traceContextCustomizers Trace 上下文定制器列表
     */
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

    /**
     * 处理当前请求的 Trace 绑定、MDC 注入与清理逻辑。
     *
     * @param request 当前请求
     * @param response 当前响应
     * @param filterChain 过滤器链
     * @throws ServletException Servlet 异常
     * @throws IOException IO 异常
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        String traceId = resolveTraceId(request);
        HttpServletRequest requestToUse = decorateRequestWithTraceHeader(request, traceId);
        String previousTraceId = TraceContextHolder.bindTraceId(traceId);
        Map<String, String> mdcEntries = new LinkedHashMap<>();
        mdcEntries.put(traceProperties.getMdcKey(), traceId);

        TraceContext traceContext = new TraceContext(traceId, requestToUse, response);
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
        requestToUse.setAttribute(traceProperties.getRequestAttributeName(), traceId);
        if (traceProperties.isResponseHeaderEnabled() && StringUtils.hasText(traceProperties.getHeaderName())) {
            response.setHeader(traceProperties.getHeaderName(), traceId);
        }

        try {
            filterChain.doFilter(requestToUse, response);
        } finally {
            restoreMdc(previousMdcValues);
            TraceContextHolder.restoreTraceId(previousTraceId);
        }
    }

    /**
     * 将 TraceId 补齐到请求头，便于应用内后续处理与转发链路复用同一标识。
     *
     * @param request 当前请求
     * @param traceId 当前 TraceId
     * @return 包装后的请求
     */
    private HttpServletRequest decorateRequestWithTraceHeader(HttpServletRequest request, String traceId) {
        String headerName = traceProperties.getHeaderName();
        if (!traceProperties.isRequestHeaderPropagationEnabled() || !StringUtils.hasText(headerName)) {
            return request;
        }
        return new HttpServletRequestWrapper(request) {
            @Override
            public String getHeader(String name) {
                if (headerName.equalsIgnoreCase(name)) {
                    return traceId;
                }
                return super.getHeader(name);
            }

            @Override
            public Enumeration<String> getHeaders(String name) {
                if (headerName.equalsIgnoreCase(name)) {
                    return Collections.enumeration(List.of(traceId));
                }
                return super.getHeaders(name);
            }

            @Override
            public Enumeration<String> getHeaderNames() {
                LinkedHashSet<String> headerNames = new LinkedHashSet<>();
                Enumeration<String> names = super.getHeaderNames();
                while (names != null && names.hasMoreElements()) {
                    headerNames.add(names.nextElement());
                }
                headerNames.add(headerName);
                return Collections.enumeration(headerNames);
            }
        };
    }

    /**
     * 解析当前请求应使用的 TraceId。
     *
     * @param request 当前请求
     * @return TraceId
     */
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
}
