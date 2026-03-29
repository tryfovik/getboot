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
package com.getboot.httpclient.spi.webclient;

import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.ClientRequest;

/**
 * WebClient Trace 请求定制器。
 *
 * <p>业务方可通过注册该类型 Bean，在透传 TraceId 后继续补充出站请求头。</p>
 *
 * @author qiheng
 */
@FunctionalInterface
public interface WebClientTraceRequestCustomizer {

    /**
     * 定制 WebClient 出站请求。
     *
     * @param headers 当前待写入的请求头
     * @param request 原始请求
     * @param traceId 当前 TraceId
     * @param traceHeaderName Trace 请求头名称
     */
    void customize(HttpHeaders headers, ClientRequest request, String traceId, String traceHeaderName);
}
