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
package com.getboot.support.infrastructure.autoconfigure;

import com.getboot.support.infrastructure.trace.MdcThreadLocalAccessor;
import com.getboot.support.infrastructure.trace.TraceContextThreadLocalAccessor;
import io.micrometer.context.ContextRegistry;
import io.micrometer.context.ContextSnapshotFactory;
import io.micrometer.context.ThreadLocalAccessor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * Trace 上下文传播自动配置。
 *
 * <p>用于注册 Micrometer 上下文传播所需的线程变量访问器与快照工厂。</p>
 *
 * @author qiheng
 */
@AutoConfiguration
@ConditionalOnClass({ContextRegistry.class, ContextSnapshotFactory.class})
public class TraceContextPropagationAutoConfiguration {

    /**
     * 注册 TraceId 线程变量访问器。
     *
     * @return 线程变量访问器
     */
    @Bean(name = "getbootTraceContextThreadLocalAccessor")
    @ConditionalOnMissingBean(name = "getbootTraceContextThreadLocalAccessor")
    public ThreadLocalAccessor<String> getbootTraceContextThreadLocalAccessor() {
        return new TraceContextThreadLocalAccessor();
    }

    /**
     * 注册 MDC 线程变量访问器。
     *
     * @return 线程变量访问器
     */
    @Bean(name = "getbootMdcThreadLocalAccessor")
    @ConditionalOnMissingBean(name = "getbootMdcThreadLocalAccessor")
    public ThreadLocalAccessor<?> getbootMdcThreadLocalAccessor() {
        return new MdcThreadLocalAccessor();
    }

    /**
     * 注册上下文注册器。
     *
     * @param threadLocalAccessors 线程变量访问器列表
     * @return 上下文注册器
     */
    @Bean
    @ConditionalOnMissingBean
    public ContextRegistry getbootContextRegistry(ObjectProvider<ThreadLocalAccessor<?>> threadLocalAccessors) {
        ContextRegistry contextRegistry = ContextRegistry.getInstance();
        threadLocalAccessors.orderedStream().forEach(accessor -> {
            if (accessor instanceof TraceContextThreadLocalAccessor) {
                contextRegistry.removeThreadLocalAccessor(TraceContextThreadLocalAccessor.KEY);
            }
            if (accessor instanceof MdcThreadLocalAccessor) {
                contextRegistry.removeThreadLocalAccessor(MdcThreadLocalAccessor.KEY);
            }
            contextRegistry.registerThreadLocalAccessor(accessor);
        });
        return contextRegistry;
    }

    /**
     * 注册上下文快照工厂。
     *
     * @param contextRegistry 上下文注册器
     * @return 快照工厂
     */
    @Bean
    @ConditionalOnMissingBean
    public ContextSnapshotFactory getbootContextSnapshotFactory(ContextRegistry contextRegistry) {
        return ContextSnapshotFactory.builder()
                .contextRegistry(contextRegistry)
                .clearMissing(true)
                .build();
    }
}
