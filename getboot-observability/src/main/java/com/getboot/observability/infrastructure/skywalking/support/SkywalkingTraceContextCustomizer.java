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

import com.getboot.observability.api.context.TraceContext;
import com.getboot.observability.api.properties.ObservabilitySkywalkingProperties;
import com.getboot.observability.spi.TraceContextCustomizer;
import org.springframework.util.StringUtils;

import java.util.Map;

/**
 * SkyWalking Trace 上下文定制器。
 *
 * <p>用于将 SkyWalking 当前 TraceId 注入到日志 MDC 中，便于日志与链路追踪系统关联。</p>
 *
 * @author qiheng
 */
public class SkywalkingTraceContextCustomizer implements TraceContextCustomizer {

    private final ObservabilitySkywalkingProperties properties;

    public SkywalkingTraceContextCustomizer(ObservabilitySkywalkingProperties properties) {
        this.properties = properties;
    }

    @Override
    public Map<String, String> customize(TraceContext traceContext) {
        String skywalkingTraceId = SkywalkingTraceSupport.resolveTraceId();
        if (!StringUtils.hasText(skywalkingTraceId)) {
            return Map.of();
        }
        return Map.of(properties.getMdcKey(), skywalkingTraceId);
    }
}
