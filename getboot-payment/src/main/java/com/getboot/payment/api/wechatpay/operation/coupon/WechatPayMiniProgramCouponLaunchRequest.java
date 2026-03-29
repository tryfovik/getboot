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
package com.getboot.payment.api.wechatpay.operation.coupon;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 小程序发券插件拉起请求。
 *
 * @author qiheng
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WechatPayMiniProgramCouponLaunchRequest {

    /**
     * 发券商户号。
     *
     * <p>未传时默认使用模块配置中的商户号。</p>
     */
    private String sendCouponMerchant;

    /**
     * 发券参数列表，一次最多 10 张券。
     */
    @Builder.Default
    private List<Coupon> coupons = new ArrayList<>();

    /**
     * 单张券参数。
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Coupon {

        /**
         * 券批次号。
         */
        private String stockId;

        /**
         * 发券凭证号。
         */
        private String outRequestNo;

        /**
         * 指定券码。
         */
        private String couponCode;
    }
}
