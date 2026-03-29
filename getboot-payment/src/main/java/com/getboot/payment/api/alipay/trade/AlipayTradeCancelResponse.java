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
package com.getboot.payment.api.alipay.trade;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 支付宝交易撤销响应。
 *
 * @author qiheng
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlipayTradeCancelResponse {

    /**
     * 商户订单号。
     */
    private String merchantOrderNo;

    /**
     * 支付宝交易号。
     */
    private String platformOrderNo;

    /**
     * 官方返回的重试标记。
     */
    private String retryFlag;

    /**
     * 官方返回的撤销动作结果。
     */
    private String action;

    /**
     * 退款时间。
     */
    private String refundTime;

    /**
     * 渠道扩展返回参数。
     */
    @Builder.Default
    private Map<String, String> metadata = new LinkedHashMap<>();
}
