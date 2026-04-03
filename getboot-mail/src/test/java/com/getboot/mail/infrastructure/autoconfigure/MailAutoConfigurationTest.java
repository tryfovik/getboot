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
package com.getboot.mail.infrastructure.autoconfigure;

import com.getboot.mail.api.operator.MailOperator;
import com.getboot.mail.spi.MailTemplateRenderer;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

/**
 * 邮件自动配置测试。
 *
 * @author qiheng
 */
class MailAutoConfigurationTest {

    /**
     * 上下文运行器。
     */
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(MailAutoConfiguration.class))
            .withPropertyValues(
                    "getboot.mail.enabled=true",
                    "getboot.mail.type=smtp",
                    "getboot.mail.default-from=noreply@example.com",
                    "getboot.mail.smtp.enabled=true",
                    "getboot.mail.smtp.host=smtp.example.com"
            );

    /**
     * 验证自动配置会注册默认邮件 Bean。
     */
    @Test
    void shouldRegisterDefaultMailBeans() {
        contextRunner.run(context -> {
            assertInstanceOf(MailTemplateRenderer.class, context.getBean(MailTemplateRenderer.class));
            assertInstanceOf(JavaMailSender.class, context.getBean(JavaMailSender.class));
            assertInstanceOf(MailOperator.class, context.getBean(MailOperator.class));
        });
    }
}
