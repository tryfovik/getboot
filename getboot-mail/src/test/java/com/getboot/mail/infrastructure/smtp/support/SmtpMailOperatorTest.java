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
package com.getboot.mail.infrastructure.smtp.support;

import com.getboot.mail.api.model.MailAttachment;
import com.getboot.mail.api.properties.MailProperties;
import com.getboot.mail.api.request.MailSendRequest;
import com.getboot.mail.support.DefaultMailTemplateRenderer;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * SMTP 邮件门面测试。
 *
 * @author qiheng
 */
class SmtpMailOperatorTest {

    /**
     * 验证邮件请求会映射到 MimeMessage。
     *
     * @throws Exception 邮件构造异常
     */
    @Test
    void shouldBuildMimeMessageFromRequest() throws Exception {
        JavaMailSender mailSender = mock(JavaMailSender.class);
        MimeMessage mimeMessage = new MimeMessage(Session.getInstance(new Properties()));
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doNothing().when(mailSender).send(org.mockito.ArgumentMatchers.any(MimeMessage.class));

        MailProperties properties = new MailProperties();
        properties.setDefaultFrom("noreply@example.com");

        SmtpMailOperator operator = new SmtpMailOperator(
                mailSender,
                new DefaultMailTemplateRenderer(),
                properties
        );

        MailAttachment attachment = new MailAttachment();
        attachment.setFilename("demo.txt");
        attachment.setContentType("text/plain");
        attachment.setContent("hello".getBytes());

        MailSendRequest request = new MailSendRequest();
        request.setToAddresses(List.of("user@example.com"));
        request.setCcAddresses(List.of("cc@example.com"));
        request.setSubjectTemplate("Welcome {{userName}}");
        request.setContentTemplate("Hello {{userName}}");
        request.setTemplateVariables(Map.of("userName", "GetBoot"));
        request.setAttachments(List.of(attachment));

        var response = operator.send(request);

        ArgumentCaptor<MimeMessage> captor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(mailSender).send(captor.capture());
        MimeMessage sentMessage = captor.getValue();

        assertEquals("Welcome GetBoot", sentMessage.getSubject());
        assertEquals("noreply@example.com", ((InternetAddress) sentMessage.getFrom()[0]).getAddress());
        assertEquals("user@example.com", ((InternetAddress) sentMessage.getRecipients(MimeMessage.RecipientType.TO)[0]).getAddress());
        assertEquals("cc@example.com", ((InternetAddress) sentMessage.getRecipients(MimeMessage.RecipientType.CC)[0]).getAddress());
        assertTrue(response.isSuccess());
        assertEquals(2, response.getRecipientCount());
    }

    /**
     * 验证未显式指定 html 标记时，会回退到默认内容类型决定正文格式。
     *
     * @throws Exception 邮件构造异常
     */
    @Test
    void shouldUseDefaultContentTypeToResolveHtmlBody() throws Exception {
        JavaMailSender mailSender = mock(JavaMailSender.class);
        MimeMessage mimeMessage = new MimeMessage(Session.getInstance(new Properties()));
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doNothing().when(mailSender).send(org.mockito.ArgumentMatchers.any(MimeMessage.class));

        MailProperties properties = new MailProperties();
        properties.setDefaultFrom("noreply@example.com");
        properties.setDefaultContentType("text/html;charset=UTF-8");

        SmtpMailOperator operator = new SmtpMailOperator(
                mailSender,
                new DefaultMailTemplateRenderer(),
                properties
        );

        MailSendRequest request = new MailSendRequest();
        request.setToAddresses(List.of("user@example.com"));
        request.setSubjectTemplate("Welcome");
        request.setContentTemplate("<p>Hello</p>");

        operator.send(request);

        ArgumentCaptor<MimeMessage> captor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(mailSender).send(captor.capture());
        MimeMessage sentMessage = captor.getValue();
        sentMessage.saveChanges();

        assertTrue(sentMessage.getContentType().toLowerCase().contains("text/html"));
    }
}
