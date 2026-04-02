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
package com.getboot.sms.api.response;

import com.getboot.sms.api.model.SmsProviderType;
import lombok.Data;

/**
 * 批量短信发送结果。
 *
 * @author qiheng
 */
@Data
public class SmsBatchSendResponse {

    /**
     * 供应商类型。
     */
    private SmsProviderType provider;

    /**
     * 是否发送成功。
     */
    private boolean success;

    /**
     * 供应商状态码。
     */
    private String code;

    /**
     * 供应商返回信息。
     */
    private String message;

    /**
     * 供应商请求 ID。
     */
    private String requestId;

    /**
     * 供应商业务 ID。
     */
    private String bizId;

    /**
     * 本次批量发送的接收人数。
     */
    private int recipientCount;
}
