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
package com.getboot.mail.api.properties;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 邮件配置绑定测试。
 *
 * @author qiheng
 */
class MailPropertiesBindingTest {

    /**
     * 验证 kebab-case 配置能够绑定到邮件属性。
     */
    @Test
    void shouldBindMailPropertiesFromKebabCaseConfiguration() {
        Map<String, String> source = new LinkedHashMap<>();
        source.put("getboot.mail.enabled", "false");
        source.put("getboot.mail.type", "smtp");
        source.put("getboot.mail.default-from", "noreply@example.com");
        source.put("getboot.mail.default-content-type", "text/html;charset=UTF-8");
        source.put("getboot.mail.smtp.enabled", "true");
        source.put("getboot.mail.smtp.host", "smtp.example.com");
        source.put("getboot.mail.smtp.port", "465");
        source.put("getboot.mail.smtp.username", "demo");
        source.put("getboot.mail.smtp.password", "pwd");
        source.put("getboot.mail.smtp.protocol", "smtp");
        source.put("getboot.mail.smtp.auth", "true");
        source.put("getboot.mail.smtp.ssl-enabled", "true");
        source.put("getboot.mail.smtp.connection-timeout", "3000");
        source.put("getboot.mail.smtp.timeout", "4000");
        source.put("getboot.mail.smtp.write-timeout", "5000");
        source.put("getboot.mail.smtp.properties.mail.debug", "false");

        MailProperties properties = new Binder(new MapConfigurationPropertySource(source))
                .bind("getboot.mail", Bindable.of(MailProperties.class))
                .orElseThrow(() -> new IllegalStateException("mail properties should bind"));

        assertFalse(properties.isEnabled());
        assertEquals("smtp", properties.getType());
        assertEquals("noreply@example.com", properties.getDefaultFrom());
        assertEquals("text/html;charset=UTF-8", properties.getDefaultContentType());
        assertTrue(properties.getSmtp().isEnabled());
        assertEquals("smtp.example.com", properties.getSmtp().getHost());
        assertEquals(465, properties.getSmtp().getPort());
        assertEquals("demo", properties.getSmtp().getUsername());
        assertEquals("pwd", properties.getSmtp().getPassword());
        assertEquals("smtp", properties.getSmtp().getProtocol());
        assertTrue(properties.getSmtp().isAuth());
        assertTrue(properties.getSmtp().isSslEnabled());
        assertEquals(3000, properties.getSmtp().getConnectionTimeout());
        assertEquals(4000, properties.getSmtp().getTimeout());
        assertEquals(5000, properties.getSmtp().getWriteTimeout());
        assertEquals("false", properties.getSmtp().getProperties().get("mail.debug"));
    }
}
