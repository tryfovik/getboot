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

import com.getboot.support.api.context.SpringContextHolder;
import com.getboot.support.api.trace.TraceContextHolder;
import com.getboot.support.infrastructure.trace.MdcThreadLocalAccessor;
import com.getboot.support.infrastructure.trace.TraceContextThreadLocalAccessor;
import io.micrometer.context.ContextRegistry;
import io.micrometer.context.ContextSnapshot;
import io.micrometer.context.ContextSnapshotFactory;
import io.micrometer.context.ThreadLocalAccessor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * support 自动配置测试。
 *
 * @author qiheng
 */
class SupportAutoConfigurationTest {

    /**
     * 测试用上下文运行器。
     */
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    SpringContextHolderAutoConfiguration.class,
                    TraceContextPropagationAutoConfiguration.class
            ));

    /**
     * 清理全局上下文注册器，避免不同测试之间互相污染。
     */
    @AfterEach
    void tearDown() {
        TraceContextHolder.clear();
        ContextRegistry.getInstance().removeThreadLocalAccessor(TraceContextThreadLocalAccessor.KEY);
        ContextRegistry.getInstance().removeThreadLocalAccessor(MdcThreadLocalAccessor.KEY);
    }

    /**
     * 验证默认自动配置会注册 support Bean，并可通过快照工厂恢复 Trace 上下文。
     */
    @Test
    void shouldRegisterDefaultSupportBeansAndCaptureTraceContext() {
        contextRunner.run(context -> {
            assertInstanceOf(SpringContextHolder.class, context.getBean(SpringContextHolder.class));
            assertInstanceOf(TraceContextThreadLocalAccessor.class,
                    context.getBean("getbootTraceContextThreadLocalAccessor"));
            assertInstanceOf(MdcThreadLocalAccessor.class, context.getBean("getbootMdcThreadLocalAccessor"));
            assertInstanceOf(ContextRegistry.class, context.getBean(ContextRegistry.class));
            assertInstanceOf(ContextSnapshotFactory.class, context.getBean(ContextSnapshotFactory.class));

            ContextRegistry contextRegistry = context.getBean(ContextRegistry.class);
            assertTrue(contextRegistry.getThreadLocalAccessors().stream()
                    .anyMatch(accessor -> TraceContextThreadLocalAccessor.KEY.equals(accessor.key())));
            assertTrue(contextRegistry.getThreadLocalAccessors().stream()
                    .anyMatch(accessor -> MdcThreadLocalAccessor.KEY.equals(accessor.key())));

            TraceContextHolder.bindTraceId("trace-support");
            ContextSnapshot snapshot = context.getBean(ContextSnapshotFactory.class).captureAll();
            TraceContextHolder.clear();

            try (ContextSnapshot.Scope ignored = snapshot.setThreadLocals()) {
                assertEquals("trace-support", TraceContextHolder.getTraceId());
            }

            assertNull(TraceContextHolder.getTraceId());
        });
    }

    /**
     * 验证用户自定义 Bean 存在时，自动配置会正常让位。
     */
    @Test
    void shouldBackOffWhenCustomBeansProvided() {
        SpringContextHolder customHolder = new SpringContextHolder();
        ThreadLocalAccessor<String> customTraceAccessor = new NamedThreadLocalAccessor(TraceContextThreadLocalAccessor.KEY);
        ThreadLocalAccessor<Object> customMdcAccessor = new NamedThreadLocalAccessor(MdcThreadLocalAccessor.KEY);
        ContextRegistry customRegistry = new ContextRegistry();
        ContextSnapshotFactory customSnapshotFactory = ContextSnapshotFactory.builder()
                .contextRegistry(customRegistry)
                .clearMissing(false)
                .build();

        contextRunner
                .withBean(SpringContextHolder.class, () -> customHolder)
                .withBean("getbootTraceContextThreadLocalAccessor", ThreadLocalAccessor.class, () -> customTraceAccessor)
                .withBean("getbootMdcThreadLocalAccessor", ThreadLocalAccessor.class, () -> customMdcAccessor)
                .withBean(ContextRegistry.class, () -> customRegistry)
                .withBean(ContextSnapshotFactory.class, () -> customSnapshotFactory)
                .run(context -> {
                    assertSame(customHolder, context.getBean(SpringContextHolder.class));
                    assertSame(customTraceAccessor, context.getBean("getbootTraceContextThreadLocalAccessor"));
                    assertSame(customMdcAccessor, context.getBean("getbootMdcThreadLocalAccessor"));
                    assertSame(customRegistry, context.getBean(ContextRegistry.class));
                    assertSame(customSnapshotFactory, context.getBean(ContextSnapshotFactory.class));
                });
    }

    /**
     * 测试用具名线程变量访问器。
     *
     * @param <T> 线程变量类型
     */
    private static final class NamedThreadLocalAccessor<T> implements ThreadLocalAccessor<T> {

        /**
         * 访问器键名。
         */
        private final String key;

        /**
         * 创建具名访问器。
         *
         * @param key 访问器键名
         */
        private NamedThreadLocalAccessor(String key) {
            this.key = key;
        }

        /**
         * 返回访问器键名。
         *
         * @return 访问器键名
         */
        @Override
        public Object key() {
            return key;
        }

        /**
         * 当前测试不读取线程变量。
         *
         * @return 始终为空
         */
        @Override
        public T getValue() {
            return null;
        }

        /**
         * 当前测试只验证自动配置回退逻辑。
         *
         * @param value 线程变量值
         */
        @Override
        public void setValue(T value) {
        }
    }
}
