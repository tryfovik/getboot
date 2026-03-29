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
package com.getboot.payment.api.wechatpay.trade;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 微信异常退款请求。
 *
 * @author qiheng
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WechatPayAbnormalRefundRequest {

    /**
     * 微信支付退款单号。
     */
    private String platformRefundNo;

    /**
     * 处理方式。
     *
     * <p>可选值：{@code USER_BANK_CARD}、{@code MERCHANT_BANK_CARD}。</p>
     */
    private String type;

    /**
     * 开户银行编码。
     */
    private String bankType;

    /**
     * 收款银行卡号。
     *
     * <p>服务端会自动加密后再发起请求。</p>
     */
    private String bankAccount;

    /**
     * 收款用户姓名。
     *
     * <p>服务端会自动加密后再发起请求。</p>
     */
    private String realName;
}
