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
package com.getboot.payment.api.wechatpay.combine;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 微信合单订单查询响应。
 *
 * @author qiheng
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WechatPayCombineOrderResponse {

    /**
     * 合单 AppId。
     */
    private String combineAppId;

    /**
     * 合单商户号。
     */
    private String combineMerchantId;

    /**
     * 合单商户订单号。
     */
    private String combineMerchantOrderNo;

    /**
     * 合单交易状态。
     */
    private String status;

    /**
     * 支付者标识。
     */
    private String payerId;

    /**
     * 合单支付微信订单号。
     */
    private String transactionId;

    /**
     * 支付成功时间。
     */
    private String successTime;

    /**
     * 商品单列表。
     */
    @Builder.Default
    private List<SubOrder> subOrders = new ArrayList<>();

    /**
     * 扩展信息。
     */
    @Builder.Default
    private Map<String, String> metadata = new LinkedHashMap<>();

    /**
     * 合单查询返回的商品单信息。
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubOrder {

        /**
         * 商品单商户号。
         */
        private String merchantId;

        /**
         * 商品单商户订单号。
         */
        private String merchantOrderNo;

        /**
         * 商品单微信订单号。
         */
        private String transactionId;

        /**
         * 商品单交易类型。
         */
        private String tradeType;

        /**
         * 商品单交易状态。
         */
        private String tradeState;

        /**
         * 商品单支付金额。
         */
        private BigDecimal totalAmount;

        /**
         * 用户实付金额。
         */
        private BigDecimal payerAmount;

        /**
         * 币种。
         */
        private String currency;

        /**
         * 支付成功时间。
         */
        private String successTime;

        /**
         * 商品单扩展信息。
         */
        @Builder.Default
        private Map<String, String> metadata = new LinkedHashMap<>();
    }
}
