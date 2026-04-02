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
package com.getboot.support.api.context;

import org.junit.jupiter.api.Test;
import org.springframework.context.support.GenericApplicationContext;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * {@link SpringContextHolder} 测试。
 *
 * @author qiheng
 */
class SpringContextHolderTest {

    /**
     * 验证能够从注入的 Spring 容器中读取 Bean。
     */
    @Test
    void shouldReadBeansFromInjectedApplicationContext() {
        GenericApplicationContext context = new GenericApplicationContext();
        context.registerBean("demoString", String.class, () -> "demo");
        context.refresh();

        SpringContextHolder holder = new SpringContextHolder();
        holder.setApplicationContext(context);

        assertEquals("demo", SpringContextHolder.getBean(String.class));
        assertEquals("demo", SpringContextHolder.getBeanIfAvailable(String.class));
        assertEquals(Map.of("demoString", "demo"), SpringContextHolder.getBeansOfType(String.class));
    }

    /**
     * 验证销毁后会清理静态容器引用。
     *
     * @throws Exception 销毁异常
     */
    @Test
    void shouldClearStaticContextOnDestroy() throws Exception {
        GenericApplicationContext context = new GenericApplicationContext();
        context.registerBean("demoInteger", Integer.class, () -> 1);
        context.refresh();

        SpringContextHolder holder = new SpringContextHolder();
        holder.setApplicationContext(context);
        holder.destroy();

        assertNull(SpringContextHolder.getBeanIfAvailable(Integer.class));
        assertEquals(Map.of(), SpringContextHolder.getBeansOfType(Integer.class));
        assertThrows(IllegalStateException.class, () -> SpringContextHolder.getBean(Integer.class));
    }
}
