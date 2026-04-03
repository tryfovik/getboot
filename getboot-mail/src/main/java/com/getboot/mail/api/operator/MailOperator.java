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
package com.getboot.mail.api.operator;

import com.getboot.mail.api.request.MailSendRequest;
import com.getboot.mail.api.response.MailSendResponse;

/**
 * 邮件发送门面。
 *
 * @author qiheng
 */
public interface MailOperator {

    /**
     * 发送邮件。
     *
     * @param request 邮件请求
     * @return 发送结果
     */
    MailSendResponse send(MailSendRequest request);
}
