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
package com.getboot.sms.api.operator;

import com.getboot.sms.api.request.SmsBatchSendRequest;
import com.getboot.sms.api.request.SmsSendRequest;
import com.getboot.sms.api.request.SmsVerificationCodeRequest;
import com.getboot.sms.api.response.SmsBatchSendResponse;
import com.getboot.sms.api.response.SmsSendResponse;

/**
 * 短信发送门面。
 *
 * @author qiheng
 */
public interface SmsOperator {

    /**
     * 发送单条短信。
     *
     * @param request 单条短信请求
     * @return 发送结果
     */
    SmsSendResponse send(SmsSendRequest request);

    /**
     * 发送批量短信。
     *
     * @param request 批量短信请求
     * @return 发送结果
     */
    SmsBatchSendResponse sendBatch(SmsBatchSendRequest request);

    /**
     * 发送验证码短信。
     *
     * @param request 验证码短信请求
     * @return 发送结果
     */
    SmsSendResponse sendVerificationCode(SmsVerificationCodeRequest request);
}
