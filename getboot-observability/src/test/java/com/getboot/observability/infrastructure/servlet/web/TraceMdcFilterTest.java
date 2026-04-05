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

import com.getboot.observability.api.properties.ObservabilityTraceProperties;
import com.getboot.observability.spi.TraceIdGenerator;
import com.getboot.support.api.trace.TraceContextHolder;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * {@link TraceMdcFilter} 测试。
 *
 * @author qiheng
 */
class TraceMdcFilterTest {

    /**
     * 验证 Servlet 过滤器会把 TraceId 补齐到请求头并在结束后恢复上下文。
     *
     * @throws Exception 过滤异常
     */
    @Test
    void shouldPropagateTraceHeaderIntoServletRequest() throws Exception {
        ObservabilityTraceProperties properties = new ObservabilityTraceProperties();
        properties.setHeaderName("X-Trace-Id");
        properties.setMdcKey("traceId");
        properties.setRequestAttributeName("GETBOOT_TRACE_ID");
        TraceIdGenerator traceIdGenerator = () -> "generated-trace-id";
        TraceMdcFilter filter = new TraceMdcFilter(properties, traceIdGenerator, List.of(), List.of());
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/orders");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (servletRequest, servletResponse) -> {
            HttpServletRequest currentRequest = (HttpServletRequest) servletRequest;
            assertEquals("generated-trace-id", TraceContextHolder.getTraceId());
            assertEquals("generated-trace-id", MDC.get("traceId"));
            assertEquals("generated-trace-id", currentRequest.getHeader("X-Trace-Id"));
            assertEquals("generated-trace-id", currentRequest.getAttribute("GETBOOT_TRACE_ID"));
            assertEquals("generated-trace-id",
                    ((MockHttpServletResponse) servletResponse).getHeader("X-Trace-Id"));
        });

        assertEquals("generated-trace-id", request.getAttribute("GETBOOT_TRACE_ID"));
        assertEquals("generated-trace-id", response.getHeader("X-Trace-Id"));
        assertNull(TraceContextHolder.getTraceId());
        assertNull(MDC.get("traceId"));
    }
}
