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
package com.getboot.mail.api.response;

import lombok.Data;

/**
 * 邮件发送响应。
 *
 * @author qiheng
 */
@Data
public class MailSendResponse {

    /**
     * 是否发送成功。
     */
    private boolean success;

    /**
     * 实际发件人。
     */
    private String fromAddress;

    /**
     * 实际收件人数。
     */
    private Integer recipientCount;

    /**
     * 消息标识。
     */
    private String messageId;
}
