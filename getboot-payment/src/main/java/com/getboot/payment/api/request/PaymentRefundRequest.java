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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 统一支付退款请求。
 *
 * @author qiheng
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRefundRequest {

    /**
     * 支付渠道。
     */
    private PaymentChannel channel;

    /**
     * 商户订单号。
     */
    private String merchantOrderNo;

    /**
     * 渠道平台侧订单号。
     */
    private String platformOrderNo;

    /**
     * 商户退款请求号。
     */
    private String refundRequestNo;

    /**
     * 原订单总金额。
     */
    private BigDecimal totalAmount;

    /**
     * 本次退款金额。
     */
    private BigDecimal refundAmount;

    /**
     * 币种，默认人民币。
     */
    @Builder.Default
    private String currency = "CNY";

    /**
     * 退款原因。
     */
    private String reason;

    /**
     * 覆盖渠道默认配置的退款异步通知地址。
     */
    private String notifyUrl;

    /**
     * 渠道扩展参数。
     */
    @Builder.Default
    private Map<String, String> metadata = new LinkedHashMap<>();
}
