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
package com.getboot.observability.infrastructure.skywalking.support;

import com.getboot.observability.spi.ReactiveTraceIdResolver;
import com.getboot.observability.spi.TraceIdResolver;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.server.ServerWebExchange;

/**
 * SkyWalking TraceId 解析器。
 *
 * <p>用于优先将 SkyWalking 当前链路 TraceId 作为 GetBoot 主 TraceId。</p>
 *
 * @author qiheng
 */
public class SkywalkingTraceIdResolver implements TraceIdResolver, ReactiveTraceIdResolver {

    @Override
    public String resolve(HttpServletRequest request) {
        return SkywalkingTraceSupport.resolveTraceId();
    }

    @Override
    public String resolve(ServerWebExchange exchange) {
        return SkywalkingTraceSupport.resolveTraceId();
    }
}
