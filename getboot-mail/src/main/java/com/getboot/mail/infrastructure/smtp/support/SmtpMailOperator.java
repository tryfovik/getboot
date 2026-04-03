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

import com.getboot.mail.api.exception.MailException;
import com.getboot.mail.api.model.MailAttachment;
import com.getboot.mail.api.operator.MailOperator;
import com.getboot.mail.api.properties.MailProperties;
import com.getboot.mail.api.request.MailSendRequest;
import com.getboot.mail.api.response.MailSendResponse;
import com.getboot.mail.spi.MailTemplateRenderer;
import com.getboot.mail.support.MailSupport;
import jakarta.mail.internet.MimeMessage;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import java.util.List;

/**
 * SMTP 邮件门面实现。
 *
 * @author qiheng
 */
public class SmtpMailOperator implements MailOperator {

    /**
     * JavaMail 发送器。
     */
    private final JavaMailSender mailSender;

    /**
     * 模板渲染器。
     */
    private final MailTemplateRenderer templateRenderer;

    /**
     * 邮件模块配置。
     */
    private final MailProperties properties;

    /**
     * 创建 SMTP 邮件门面实现。
     *
     * @param mailSender JavaMail 发送器
     * @param templateRenderer 模板渲染器
     * @param properties 邮件模块配置
     */
    public SmtpMailOperator(JavaMailSender mailSender, MailTemplateRenderer templateRenderer, MailProperties properties) {
        this.mailSender = mailSender;
        this.templateRenderer = templateRenderer;
        this.properties = properties;
    }

    /**
     * 发送邮件。
     *
     * @param request 邮件请求
     * @return 发送结果
     */
    @Override
    public MailSendResponse send(MailSendRequest request) {
        if (request == null) {
            throw new MailException("Mail request must not be null.");
        }

        List<String> toAddresses = MailSupport.requireAddresses(request.getToAddresses(), "Mail toAddresses");
        String fromAddress = resolveFromAddress(request);
        String subject = templateRenderer.render(request.getSubjectTemplate(), request.getTemplateVariables());
        String content = templateRenderer.render(request.getContentTemplate(), request.getTemplateVariables());
        List<String> ccAddresses = MailSupport.normalizeAddresses(request.getCcAddresses());
        List<String> bccAddresses = MailSupport.normalizeAddresses(request.getBccAddresses());
        boolean html = resolveHtml(request);

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, !request.getAttachments().isEmpty(), "UTF-8");
            helper.setFrom(fromAddress);
            helper.setTo(toAddresses.toArray(String[]::new));
            if (!ccAddresses.isEmpty()) {
                helper.setCc(ccAddresses.toArray(String[]::new));
            }
            if (!bccAddresses.isEmpty()) {
                helper.setBcc(bccAddresses.toArray(String[]::new));
            }
            if (MailSupport.hasText(request.getReplyToAddress())) {
                helper.setReplyTo(request.getReplyToAddress().trim());
            }
            helper.setSubject(subject);
            helper.setText(content, html);

            for (MailAttachment attachment : request.getAttachments()) {
                addAttachment(helper, attachment);
            }

            mailSender.send(mimeMessage);

            MailSendResponse response = new MailSendResponse();
            response.setSuccess(true);
            response.setFromAddress(fromAddress);
            response.setRecipientCount(toAddresses.size() + ccAddresses.size() + bccAddresses.size());
            response.setMessageId(mimeMessage.getMessageID());
            return response;
        } catch (MailSendException exception) {
            throw new MailException("Mail send failed.", exception);
        } catch (Exception exception) {
            throw new MailException("Mail build failed.", exception);
        }
    }

    /**
     * 解析发件人。
     *
     * @param request 邮件请求
     * @return 发件人
     */
    private String resolveFromAddress(MailSendRequest request) {
        if (MailSupport.hasText(request.getFromAddress())) {
            return request.getFromAddress().trim();
        }
        return MailSupport.requireText(properties.getDefaultFrom(), "Mail defaultFrom");
    }

    /**
     * 解析正文是否按 HTML 发送。
     *
     * @param request 邮件请求
     * @return 是否按 HTML 发送
     */
    private boolean resolveHtml(MailSendRequest request) {
        if (MailSupport.hasText(request.getContentType())) {
            return MailSupport.isHtmlContentType(request.getContentType());
        }
        if (request.isHtml()) {
            return true;
        }
        return MailSupport.isHtmlContentType(properties.getDefaultContentType());
    }

    /**
     * 添加附件。
     *
     * @param helper 邮件辅助器
     * @param attachment 附件
     */
    private void addAttachment(MimeMessageHelper helper, MailAttachment attachment) {
        if (attachment == null) {
            return;
        }
        String filename = MailSupport.requireText(attachment.getFilename(), "Mail attachment filename");
        byte[] content = attachment.getContent();
        if (content == null || content.length == 0) {
            throw new MailException("Mail attachment content must not be empty.");
        }
        try {
            if (MailSupport.hasText(attachment.getContentType())) {
                helper.addAttachment(
                        filename,
                        new ByteArrayResource(content),
                        attachment.getContentType().trim()
                );
            } else {
                helper.addAttachment(filename, new ByteArrayResource(content));
            }
        } catch (Exception exception) {
            throw new MailException("Mail attachment build failed.", exception);
        }
    }
}
