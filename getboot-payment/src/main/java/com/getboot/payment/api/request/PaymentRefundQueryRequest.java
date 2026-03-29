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

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 统一退款查询请求。
 *
 * @author qiheng
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRefundQueryRequest {

    /**
     * 支付渠道。
     */
    private PaymentChannel channel;

    /**
     * 商户订单号。
     *
     * <p>支付宝退款查询要求携带原商户订单号，因此统一抽象中也保留该字段。</p>
     */
    private String merchantOrderNo;

    /**
     * 渠道平台订单号。
     */
    private String platformOrderNo;

    /**
     * 商户退款请求号。
     */
    private String refundRequestNo;

    /**
     * 渠道扩展参数。
     */
    @Builder.Default
    private Map<String, String> metadata = new LinkedHashMap<>();
}
