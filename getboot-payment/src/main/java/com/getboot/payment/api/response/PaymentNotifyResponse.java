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
package com.getboot.payment.api.response;

import com.getboot.payment.api.model.PaymentChannel;
import com.getboot.payment.api.model.PaymentNotifyType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 统一异步通知解析结果。
 *
 * @author qiheng
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentNotifyResponse {

    /**
     * 支付渠道。
     */
    private PaymentChannel channel;

    /**
     * 通知类型。
     */
    private PaymentNotifyType notifyType;

    /**
     * 商户订单号。
     */
    private String merchantOrderNo;

    /**
     * 渠道平台侧订单号。
     */
    private String platformOrderNo;

    /**
     * 商户退款请求号。
     */
    private String refundRequestNo;

    /**
     * 渠道状态。
     */
    private String status;

    /**
     * 是否处理成功。
     */
    private boolean success;

    /**
     * 事件时间。
     */
    private String eventTime;

    /**
     * 渠道扩展返回参数。
     */
    @Builder.Default
    private Map<String, String> metadata = new LinkedHashMap<>();
}
