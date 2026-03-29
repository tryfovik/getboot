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
package com.getboot.observability.infrastructure.prometheus.autoconfigure;

import com.getboot.observability.api.properties.ObservabilityMetricsProperties;
import com.getboot.observability.spi.prometheus.ObservabilityMeterRegistryCustomizer;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.util.Map;

/**
 * Prometheus 可观测性自动配置。
 *
 * <p>负责注册公共指标标签与业务可扩展的 MeterRegistry 定制逻辑。</p>
 *
 * @author qiheng
 */
@AutoConfiguration
@ConditionalOnClass(MeterRegistry.class)
@EnableConfigurationProperties(ObservabilityMetricsProperties.class)
@ConditionalOnProperty(prefix = "getboot.observability.metrics", name = "enabled", havingValue = "true", matchIfMissing = true)
public class PrometheusObservabilityAutoConfiguration {

    /**
     * 注册 MeterRegistry 定制器。
     *
     * @param metricsProperties 指标配置
     * @param registryCustomizers 自定义注册表定制器
     * @return MeterRegistry 定制器
     */
    @Bean
    public MeterRegistryCustomizer<MeterRegistry> getbootMeterRegistryCustomizer(
            ObservabilityMetricsProperties metricsProperties,
            ObjectProvider<ObservabilityMeterRegistryCustomizer> registryCustomizers) {
        return registry -> {
            Map<String, String> commonTags = metricsProperties.getCommonTags();
            if (commonTags != null && !commonTags.isEmpty()) {
                registry.config().commonTags(commonTags.entrySet().stream()
                        .flatMap(entry -> java.util.stream.Stream.of(entry.getKey(), entry.getValue()))
                        .toArray(String[]::new));
            }
            registryCustomizers.orderedStream().forEach(customizer -> customizer.customize(registry));
        };
    }
}
