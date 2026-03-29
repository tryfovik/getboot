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
package com.getboot.payment.api.response;

import com.getboot.payment.api.model.PaymentChannel;
import com.getboot.payment.api.model.PaymentMode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 统一支付下单响应。
 *
 * @author qiheng
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCreateResponse {

    /**
     * 支付渠道。
     */
    private PaymentChannel channel;

    /**
     * 支付方式。
     */
    private PaymentMode mode;

    /**
     * 商户订单号。
     */
    private String merchantOrderNo;

    /**
     * 渠道平台侧订单号。
     */
    private String platformOrderNo;

    /**
     * 可直接跳转的支付链接。
     */
    private String payUrl;

    /**
     * 渠道预支付单号，例如微信 `prepay_id`。
     */
    private String prepayId;

    /**
     * Native 支付场景的二维码内容。
     */
    private String qrCodeContent;

    /**
     * 前端拉起支付所需参数。
     */
    @Builder.Default
    private Map<String, String> paymentData = new LinkedHashMap<>();

    /**
     * 渠道扩展返回参数。
     */
    @Builder.Default
    private Map<String, String> metadata = new LinkedHashMap<>();
}
