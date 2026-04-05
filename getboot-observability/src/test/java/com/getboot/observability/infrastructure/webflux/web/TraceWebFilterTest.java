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

import com.getboot.observability.api.properties.ObservabilityTraceProperties;
import com.getboot.observability.spi.ReactiveTraceContextCustomizer;
import com.getboot.observability.spi.TraceIdGenerator;
import com.getboot.support.api.trace.TraceContextHolder;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * {@link TraceWebFilter} 测试。
 *
 * @author qiheng
 */
class TraceWebFilterTest {

    /**
     * 验证 WebFlux 过滤器会在订阅阶段绑定并在结束后恢复 Trace 上下文。
     */
    @Test
    void shouldBindAndRestoreTraceContextAtSubscriptionTime() {
        ObservabilityTraceProperties properties = new ObservabilityTraceProperties();
        properties.setHeaderName("X-Trace-Id");
        properties.setMdcKey("traceId");
        properties.setRequestAttributeName("GETBOOT_TRACE_ID");
        ReactiveTraceContextCustomizer customizer = traceContext -> Map.of("tenantId", "tenant-001");
        TraceIdGenerator traceIdGenerator = () -> "generated-trace-id";
        TraceWebFilter filter = new TraceWebFilter(
                properties,
                traceIdGenerator,
                List.of(),
                List.of(customizer)
        );
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/orders").build());
        WebFilterChain chain = serverWebExchange -> Mono.fromRunnable(() -> {
            assertEquals("generated-trace-id", TraceContextHolder.getTraceId());
            assertEquals("generated-trace-id", MDC.get("traceId"));
            assertEquals("tenant-001", MDC.get("tenantId"));
            assertEquals("generated-trace-id", serverWebExchange.getAttribute("GETBOOT_TRACE_ID"));
            assertEquals("generated-trace-id",
                    serverWebExchange.getRequest().getHeaders().getFirst("X-Trace-Id"));
            assertEquals("generated-trace-id", serverWebExchange.getResponse().getHeaders().getFirst("X-Trace-Id"));
        });

        Mono<Void> result = filter.filter(exchange, chain);

        assertNull(TraceContextHolder.getTraceId());
        assertNull(MDC.get("traceId"));
        assertNull(MDC.get("tenantId"));

        result.block();

        assertNull(TraceContextHolder.getTraceId());
        assertNull(MDC.get("traceId"));
        assertNull(MDC.get("tenantId"));
    }
}
