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
package com.getboot.support.api.trace;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * {@link TraceContextHolder} 测试。
 *
 * @author qiheng
 */
class TraceContextHolderTest {

    /**
     * 验证 TraceId 绑定与恢复逻辑。
     */
    @Test
    void shouldBindAndRestoreTraceId() {
        TraceContextHolder.clear();

        assertNull(TraceContextHolder.bindTraceId("trace-1"));
        assertEquals("trace-1", TraceContextHolder.getTraceId());

        String previous = TraceContextHolder.bindTraceId("trace-2");
        assertEquals("trace-1", previous);
        assertEquals("trace-2", TraceContextHolder.getTraceId());

        TraceContextHolder.restoreTraceId(previous);
        assertEquals("trace-1", TraceContextHolder.getTraceId());

        TraceContextHolder.clear();
        assertNull(TraceContextHolder.getTraceId());
    }

    /**
     * 验证绑定空白 TraceId 时会清空线程上下文。
     */
    @Test
    void shouldClearTraceIdWhenBlankValueIsBound() {
        TraceContextHolder.bindTraceId("trace-1");

        TraceContextHolder.bindTraceId("   ");

        assertNull(TraceContextHolder.getTraceId());
    }
}
