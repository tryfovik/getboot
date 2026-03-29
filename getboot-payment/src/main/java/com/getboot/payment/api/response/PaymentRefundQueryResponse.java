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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 统一退款查询响应。
 *
 * @author qiheng
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRefundQueryResponse {

    /**
     * 支付渠道。
     */
    private PaymentChannel channel;

    /**
     * 商户订单号。
     */
    private String merchantOrderNo;

    /**
     * 商户退款请求号。
     */
    private String refundRequestNo;

    /**
     * 渠道平台侧退款单号。
     */
    private String platformRefundNo;

    /**
     * 退款状态。
     */
    private String status;

    /**
     * 退款金额。
     */
    private BigDecimal refundAmount;

    /**
     * 币种。
     */
    private String currency;

    /**
     * 退款成功时间。
     */
    private String successTime;

    /**
     * 渠道扩展返回参数。
     */
    @Builder.Default
    private Map<String, String> metadata = new LinkedHashMap<>();
}
