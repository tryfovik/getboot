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
import com.getboot.payment.api.model.PaymentMode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 统一支付下单请求。
 *
 * @author qiheng
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCreateRequest {

    /**
     * 支付渠道。
     */
    private PaymentChannel channel;

    /**
     * 支付方式。
     */
    private PaymentMode mode;

    /**
     * 覆盖渠道默认配置的应用 ID。
     */
    private String appId;

    /**
     * 商户订单号。
     */
    private String merchantOrderNo;

    /**
     * 订单标题。
     */
    private String subject;

    /**
     * 订单描述。
     */
    private String description;

    /**
     * 付款人标识。
     *
     * <p>例如微信 `openid`。</p>
     */
    private String payerId;

    /**
     * 发起支付的客户端 IP。
     */
    private String clientIp;

    /**
     * 订单过期时间，建议使用 RFC3339 格式。
     */
    private String timeExpire;

    /**
     * 支付金额。
     */
    private BigDecimal amount;

    /**
     * 币种，默认人民币。
     */
    @Builder.Default
    private String currency = "CNY";

    /**
     * 覆盖渠道默认配置的异步通知地址。
     */
    private String notifyUrl;

    /**
     * 覆盖渠道默认配置的同步跳转地址。
     */
    private String returnUrl;

    /**
     * 渠道扩展参数。
     */
    @Builder.Default
    private Map<String, String> metadata = new LinkedHashMap<>();
}
