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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 微信合单支付通知解析结果。
 *
 * @author qiheng
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WechatPayCombineNotifyResponse {

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
     * 合单微信订单号。
     */
    private String transactionId;

    /**
     * 是否支付成功。
     */
    private boolean success;

    /**
     * 支付成功时间。
     */
    private String successTime;

    /**
     * 支付者标识。
     */
    private String payerId;

    /**
     * 商品单信息。
     */
    @Builder.Default
    private List<WechatPayCombineOrderResponse.SubOrder> subOrders = new ArrayList<>();

    /**
     * 扩展信息。
     */
    @Builder.Default
    private Map<String, String> metadata = new LinkedHashMap<>();
}
