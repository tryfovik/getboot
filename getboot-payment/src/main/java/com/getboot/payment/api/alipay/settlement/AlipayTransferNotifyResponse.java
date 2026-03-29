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
package com.getboot.payment.api.alipay.settlement;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 支付宝转账异步通知响应。
 *
 * @author qiheng
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlipayTransferNotifyResponse {

    /**
     * 商户转账请求号。
     */
    private String transferRequestNo;

    /**
     * 支付宝转账订单号。
     */
    private String platformTransferOrderNo;

    /**
     * 支付宝支付资金流水号。
     */
    private String platformPayFundOrderNo;

    /**
     * 转账状态。
     */
    private String status;

    /**
     * 是否转账成功。
     */
    private boolean success;

    /**
     * 转账金额。
     */
    private BigDecimal amount;

    /**
     * 事件时间。
     */
    private String eventTime;

    /**
     * 失败原因。
     */
    private String failReason;

    /**
     * 扩展元数据。
     */
    @Builder.Default
    private Map<String, String> metadata = new LinkedHashMap<>();
}
