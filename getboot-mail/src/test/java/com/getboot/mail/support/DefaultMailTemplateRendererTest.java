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
package com.getboot.mail.support;

import com.getboot.mail.api.exception.MailException;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 默认邮件模板渲染器测试。
 *
 * @author qiheng
 */
class DefaultMailTemplateRendererTest {

    /**
     * 验证模板变量能够正确渲染。
     */
    @Test
    void shouldRenderTemplateVariables() {
        String rendered = new DefaultMailTemplateRenderer().render(
                "Hello {{userName}}",
                Map.of("userName", "GetBoot")
        );

        assertEquals("Hello GetBoot", rendered);
    }

    /**
     * 验证缺少变量时会抛出异常。
     */
    @Test
    void shouldThrowWhenTemplateVariableMissing() {
        assertThrows(MailException.class, () ->
                new DefaultMailTemplateRenderer().render("Hello {{userName}}", Map.of()));
    }
}
