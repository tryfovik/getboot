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
package com.getboot.payment.api.alipay.trade.huabei;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 支付宝花呗分期交易创建请求。
 *
 * @author qiheng
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlipayHuabeiCreateRequest {

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
     * 买家支付宝用户 ID。
     */
    private String payerId;

    /**
     * 交易金额。
     */
    private BigDecimal amount;

    /**
     * 分期数，仅支持 `3 / 6 / 12`。
     */
    private String hbFqNum;

    /**
     * 卖家承担比例，仅支持 `0 / 100`。
     */
    private String hbFqSellerPercent;

    /**
     * 覆盖默认配置的异步通知地址。
     */
    private String notifyUrl;

    /**
     * 渠道扩展参数。
     */
    @Builder.Default
    private Map<String, String> metadata = new LinkedHashMap<>();
}
