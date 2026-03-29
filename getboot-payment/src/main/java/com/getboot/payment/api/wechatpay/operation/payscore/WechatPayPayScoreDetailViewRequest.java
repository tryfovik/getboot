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
package com.getboot.payment.api.wechatpay.operation.payscore;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 支付分订单详情页构造参数。
 *
 * @author qiheng
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WechatPayPayScoreDetailViewRequest {

    /**
     * 商户号。
     *
     * <p>未传时默认使用模块配置中的商户号。</p>
     */
    private String merchantId;

    /**
     * 服务 ID。
     */
    private String serviceId;

    /**
     * 商户服务订单号。
     */
    private String outOrderNo;

    /**
     * 时间戳。
     *
     * <p>未传时默认使用当前秒级时间戳。</p>
     */
    private String timestamp;

    /**
     * 随机串。
     *
     * <p>未传时自动生成。</p>
     */
    private String nonceStr;
}
