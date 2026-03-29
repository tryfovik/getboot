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

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 微信合单下单响应。
 *
 * @author qiheng
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WechatPayCombineCreateResponse {

    /**
     * 合单支付方式。
     */
    private PaymentMode mode;

    /**
     * 合单商户订单号。
     */
    private String combineMerchantOrderNo;

    /**
     * 预支付标识。
     */
    private String prepayId;

    /**
     * 可直接跳转的支付地址。
     */
    private String payUrl;

    /**
     * Native 场景二维码内容。
     */
    private String qrCodeContent;

    /**
     * 前端拉起支付参数。
     */
    @Builder.Default
    private Map<String, String> paymentData = new LinkedHashMap<>();

    /**
     * 额外返回信息。
     */
    @Builder.Default
    private Map<String, String> metadata = new LinkedHashMap<>();
}
