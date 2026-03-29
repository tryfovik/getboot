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
package com.getboot.observability.infrastructure.autoconfigure;

import com.getboot.observability.api.properties.ObservabilityTraceProperties;
import com.getboot.observability.support.TraceTaskDecorator;
import com.getboot.observability.support.TraceTaskDecoratorBeanPostProcessor;
import com.getboot.observability.support.UuidTraceIdGenerator;
import com.getboot.observability.spi.TraceIdGenerator;
import io.micrometer.context.ContextSnapshotFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.TaskDecorator;

/**
 * 可观测性基础自动配置。
 *
 * <p>负责注册默认 TraceId 生成器以及异步线程 Trace 上下文传播能力。</p>
 *
 * @author qiheng
 */
@AutoConfiguration
@EnableConfigurationProperties(ObservabilityTraceProperties.class)
@ConditionalOnProperty(prefix = "getboot.observability.trace", name = "enabled", havingValue = "true", matchIfMissing = true)
public class ObservabilityAutoConfiguration {

    /**
     * 注册默认 TraceId 生成器。
     *
     * @return TraceId 生成器
     */
    @Bean
    @ConditionalOnMissingBean
    public TraceIdGenerator traceIdGenerator() {
        return new UuidTraceIdGenerator();
    }

    /**
     * 注册异步线程 Trace 装饰器。
     *
     * @param contextSnapshotFactory 上下文快照工厂
     * @return 任务装饰器
     */
    @Bean
    @ConditionalOnMissingBean(name = "getbootTraceTaskDecorator")
    @ConditionalOnProperty(prefix = "getboot.observability.trace", name = "async-propagation-enabled", havingValue = "true", matchIfMissing = true)
    public TaskDecorator getbootTraceTaskDecorator(ContextSnapshotFactory contextSnapshotFactory) {
        return new TraceTaskDecorator(contextSnapshotFactory);
    }

    /**
     * 注册任务装饰器后处理器。
     *
     * @param taskDecorator 任务装饰器
     * @return Bean 后处理器
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "getboot.observability.trace", name = "async-propagation-enabled", havingValue = "true", matchIfMissing = true)
    public TraceTaskDecoratorBeanPostProcessor traceTaskDecoratorBeanPostProcessor(TaskDecorator taskDecorator) {
        return new TraceTaskDecoratorBeanPostProcessor(taskDecorator);
    }
}
