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

import com.getboot.payment.api.model.PaymentMode;
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
 * 微信合单下单请求。
 *
 * @author qiheng
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WechatPayCombineCreateRequest {

    /**
     * 合单支付方式。
     */
    private PaymentMode mode;

    /**
     * 覆盖默认配置的合单 AppId。
     */
    private String appId;

    /**
     * 合单商户订单号。
     */
    private String combineMerchantOrderNo;

    /**
     * 订单标题。
     */
    private String subject;

    /**
     * 订单描述。
     */
    private String description;

    /**
     * 支付者标识。
     *
     * <p>JSAPI / 小程序场景通常传入 openId。</p>
     */
    private String payerId;

    /**
     * H5 场景客户端 IP。
     */
    private String clientIp;

    /**
     * 订单过期时间。
     */
    private String timeExpire;

    /**
     * 覆盖默认配置的通知地址。
     */
    private String notifyUrl;

    /**
     * 合单扩展参数。
     */
    @Builder.Default
    private Map<String, String> metadata = new LinkedHashMap<>();

    /**
     * 商品单列表。
     */
    @Builder.Default
    private List<SubOrder> subOrders = new ArrayList<>();

    /**
     * 合单子单请求。
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubOrder {

        /**
         * 覆盖默认配置的商品单商户号。
         */
        private String merchantId;

        /**
         * 商品单商户订单号。
         */
        private String merchantOrderNo;

        /**
         * 商品单描述。
         */
        private String description;

        /**
         * 商品单金额。
         */
        private BigDecimal amount;

        /**
         * 币种，默认人民币。
         */
        @Builder.Default
        private String currency = "CNY";

        /**
         * 商品单扩展参数。
         */
        @Builder.Default
        private Map<String, String> metadata = new LinkedHashMap<>();
    }
}
