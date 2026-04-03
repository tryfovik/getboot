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
package com.getboot.mail.api.request;

import com.getboot.mail.api.model.MailAttachment;
import lombok.Data;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 邮件发送请求。
 *
 * @author qiheng
 */
@Data
public class MailSendRequest {

    /**
     * 指定发件人。
     */
    private String fromAddress;

    /**
     * 收件人列表。
     */
    private List<String> toAddresses = new ArrayList<>();

    /**
     * 抄送人列表。
     */
    private List<String> ccAddresses = new ArrayList<>();

    /**
     * 密送人列表。
     */
    private List<String> bccAddresses = new ArrayList<>();

    /**
     * 回复地址。
     */
    private String replyToAddress;

    /**
     * 主题模板。
     */
    private String subjectTemplate;

    /**
     * 正文模板。
     */
    private String contentTemplate;

    /**
     * 模板变量。
     */
    private Map<String, Object> templateVariables = new LinkedHashMap<>();

    /**
     * 是否使用 HTML 正文。
     */
    private boolean html = false;

    /**
     * 指定内容类型。
     */
    private String contentType;

    /**
     * 附件列表。
     */
    private List<MailAttachment> attachments = new ArrayList<>();

    /**
     * 设置收件人列表。
     *
     * @param toAddresses 收件人列表
     */
    public void setToAddresses(List<String> toAddresses) {
        this.toAddresses = toAddresses == null ? new ArrayList<>() : new ArrayList<>(toAddresses);
    }

    /**
     * 设置抄送人列表。
     *
     * @param ccAddresses 抄送人列表
     */
    public void setCcAddresses(List<String> ccAddresses) {
        this.ccAddresses = ccAddresses == null ? new ArrayList<>() : new ArrayList<>(ccAddresses);
    }

    /**
     * 设置密送人列表。
     *
     * @param bccAddresses 密送人列表
     */
    public void setBccAddresses(List<String> bccAddresses) {
        this.bccAddresses = bccAddresses == null ? new ArrayList<>() : new ArrayList<>(bccAddresses);
    }

    /**
     * 设置模板变量。
     *
     * @param templateVariables 模板变量
     */
    public void setTemplateVariables(Map<String, Object> templateVariables) {
        this.templateVariables = templateVariables == null ? new LinkedHashMap<>() : new LinkedHashMap<>(templateVariables);
    }

    /**
     * 设置附件列表。
     *
     * @param attachments 附件列表
     */
    public void setAttachments(List<MailAttachment> attachments) {
        this.attachments = attachments == null ? new ArrayList<>() : new ArrayList<>(attachments);
    }
}
