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
import com.getboot.mail.api.properties.MailProperties;
import com.getboot.mail.infrastructure.smtp.support.SmtpMailOperator;
import com.getboot.mail.spi.MailTemplateRenderer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.javamail.JavaMailSender;

/**
 * 邮件门面自动配置。
 *
 * @author qiheng
 */
@AutoConfiguration
@ConditionalOnProperty(prefix = "getboot.mail", name = "enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnExpression("'${getboot.mail.type:smtp}' == 'smtp'")
public class MailOperatorAutoConfiguration {

    /**
     * 注册默认邮件门面。
     *
     * @param mailSender JavaMail 发送器
     * @param templateRenderer 模板渲染器
     * @param properties 邮件模块配置
     * @return 邮件门面
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(JavaMailSender.class)
    public MailOperator mailOperator(
            JavaMailSender mailSender,
            MailTemplateRenderer templateRenderer,
            MailProperties properties) {
        return new SmtpMailOperator(mailSender, templateRenderer, properties);
    }
}
