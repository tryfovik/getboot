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
import java.util.List;

/**
 * 微信合单关单请求。
 *
 * @author qiheng
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WechatPayCombineCloseRequest {

    /**
     * 覆盖默认配置的 AppId。
     */
    private String appId;

    /**
     * 合单商户订单号。
     */
    private String combineMerchantOrderNo;

    /**
     * 参与关闭的商品单列表。
     */
    @Builder.Default
    private List<SubOrder> subOrders = new ArrayList<>();

    /**
     * 商品单标识。
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
    }
}
