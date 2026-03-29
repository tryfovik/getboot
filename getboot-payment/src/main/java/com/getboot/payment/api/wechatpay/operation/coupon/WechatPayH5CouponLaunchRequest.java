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

/**
 * H5 发券链接构造请求。
 *
 * @author qiheng
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WechatPayH5CouponLaunchRequest {

    /**
     * 券批次号。
     */
    private String stockId;

    /**
     * 发券凭证号。
     */
    private String outRequestNo;

    /**
     * 用户 OpenID。
     */
    private String openId;

    /**
     * 指定券码。
     */
    private String couponCode;

    /**
     * 发券商户号。
     *
     * <p>未传时默认使用模块配置中的商户号。</p>
     */
    private String sendCouponMerchant;
}
