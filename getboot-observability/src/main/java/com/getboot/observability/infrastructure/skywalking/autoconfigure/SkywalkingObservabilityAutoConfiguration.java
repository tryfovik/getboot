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
package com.getboot.observability.infrastructure.skywalking.autoconfigure;

import com.getboot.observability.api.properties.ObservabilitySkywalkingProperties;
import com.getboot.observability.infrastructure.skywalking.support.SkywalkingTraceContextCustomizer;
import com.getboot.observability.infrastructure.skywalking.support.SkywalkingTraceIdResolver;
import com.getboot.observability.spi.TraceContextCustomizer;
import com.getboot.observability.spi.TraceIdResolver;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

/**
 * SkyWalking 可观测性自动配置。
 *
 * <p>负责将 SkyWalking Trace 信息桥接到 GetBoot 的日志上下文中。</p>
 *
 * @author qiheng
 */
@AutoConfiguration
@ConditionalOnClass(name = "org.apache.skywalking.apm.toolkit.trace.TraceContext")
@EnableConfigurationProperties(ObservabilitySkywalkingProperties.class)
@ConditionalOnProperty(prefix = "getboot.observability.skywalking", name = "enabled", havingValue = "true", matchIfMissing = true)
public class SkywalkingObservabilityAutoConfiguration {

    /**
     * 注册 SkyWalking TraceId 解析器。
     *
     * @return TraceId 解析器
     */
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    @ConditionalOnMissingBean(name = "skywalkingTraceIdResolver")
    public TraceIdResolver skywalkingTraceIdResolver() {
        return new SkywalkingTraceIdResolver();
    }

    /**
     * 注册 SkyWalking Trace 上下文定制器。
     *
     * @param properties SkyWalking 配置
     * @return Trace 上下文定制器
     */
    @Bean
    @ConditionalOnMissingBean(name = "skywalkingTraceContextCustomizer")
    public TraceContextCustomizer skywalkingTraceContextCustomizer(ObservabilitySkywalkingProperties properties) {
        return new SkywalkingTraceContextCustomizer(properties);
    }
}
