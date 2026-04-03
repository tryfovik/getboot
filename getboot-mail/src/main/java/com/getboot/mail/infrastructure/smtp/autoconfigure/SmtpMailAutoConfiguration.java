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
package com.getboot.mail.infrastructure.smtp.autoconfigure;

import com.getboot.mail.api.exception.MailException;
import com.getboot.mail.api.properties.MailProperties;
import com.getboot.mail.support.MailSupport;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

/**
 * SMTP 自动配置。
 *
 * @author qiheng
 */
@AutoConfiguration
@ConditionalOnClass(JavaMailSender.class)
@ConditionalOnProperty(prefix = "getboot.mail", name = "enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnExpression("${getboot.mail.smtp.enabled:true} and '${getboot.mail.type:smtp}' == 'smtp'")
public class SmtpMailAutoConfiguration {

    /**
     * 注册 JavaMailSender。
     *
     * @param properties 邮件模块配置
     * @return JavaMailSender
     */
    @Bean
    @ConditionalOnMissingBean
    public JavaMailSender javaMailSender(MailProperties properties) {
        MailProperties.Smtp smtp = properties.getSmtp();
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(MailSupport.requireText(smtp.getHost(), "SMTP host"));
        if (smtp.getPort() == null || smtp.getPort() < 1) {
            throw new MailException("SMTP port must be greater than 0.");
        }
        mailSender.setPort(smtp.getPort());
        mailSender.setProtocol(MailSupport.requireText(smtp.getProtocol(), "SMTP protocol"));
        mailSender.setUsername(smtp.getUsername());
        mailSender.setPassword(smtp.getPassword());
        mailSender.setDefaultEncoding("UTF-8");
        mailSender.setJavaMailProperties(resolveJavaMailProperties(smtp));
        return mailSender;
    }

    /**
     * 解析 JavaMail 属性。
     *
     * @param smtp SMTP 配置
     * @return JavaMail 属性
     */
    private Properties resolveJavaMailProperties(MailProperties.Smtp smtp) {
        Properties javaMailProperties = new Properties();
        javaMailProperties.setProperty("mail.transport.protocol", smtp.getProtocol());
        javaMailProperties.setProperty("mail.smtp.auth", String.valueOf(smtp.isAuth()));
        javaMailProperties.setProperty("mail.smtp.starttls.enable", String.valueOf(smtp.isStarttlsEnabled()));
        javaMailProperties.setProperty("mail.smtp.ssl.enable", String.valueOf(smtp.isSslEnabled()));
        javaMailProperties.setProperty("mail.smtp.connectiontimeout", String.valueOf(smtp.getConnectionTimeout()));
        javaMailProperties.setProperty("mail.smtp.timeout", String.valueOf(smtp.getTimeout()));
        javaMailProperties.setProperty("mail.smtp.writetimeout", String.valueOf(smtp.getWriteTimeout()));
        smtp.getProperties().forEach(javaMailProperties::setProperty);
        return javaMailProperties;
    }
}
