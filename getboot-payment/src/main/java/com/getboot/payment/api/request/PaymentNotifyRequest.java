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
package com.getboot.payment.api.request;

import com.getboot.payment.api.model.PaymentChannel;
import com.getboot.payment.api.model.PaymentNotifyType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 统一异步通知解析请求。
 *
 * @author qiheng
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentNotifyRequest {

    /**
     * 支付渠道。
     */
    private PaymentChannel channel;

    /**
     * 通知类型。
     */
    private PaymentNotifyType notifyType;

    /**
     * 回调原始报文。
     */
    private String body;

    /**
     * 回调请求头。
     */
    @Builder.Default
    private Map<String, String> headers = new LinkedHashMap<>();
}
