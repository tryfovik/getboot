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
package com.getboot.payment.api.alipay.trade.facetoface;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 支付宝当面付条码支付响应。
 *
 * @author qiheng
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlipayFaceToFacePayResponse {

    /**
     * 商户订单号。
     */
    private String merchantOrderNo;

    /**
     * 支付宝交易号。
     */
    private String platformOrderNo;

    /**
     * 交易状态。
     */
    private String status;

    /**
     * 实际支付金额。
     */
    private BigDecimal paidAmount;

    /**
     * 支付币种。
     */
    private String currency;

    /**
     * 付款人标识。
     */
    private String payerId;

    /**
     * 支付成功时间。
     */
    private String successTime;

    /**
     * 渠道扩展返回参数。
     */
    @Builder.Default
    private Map<String, String> metadata = new LinkedHashMap<>();
}
