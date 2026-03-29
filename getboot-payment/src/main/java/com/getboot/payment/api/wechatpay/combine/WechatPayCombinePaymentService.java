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

import java.util.Set;

/**
 * 微信合单支付能力接口。
 *
 * <p>该接口承接不适合塞入统一 {@code PaymentService} 的微信合单专属能力。</p>
 *
 * @author qiheng
 */
public interface WechatPayCombinePaymentService {

    /**
     * 获取当前支持的合单支付方式。
     *
     * @return 支持的支付方式集合
     */
    Set<PaymentMode> supportedModes();

    /**
     * 创建合单支付订单。
     *
     * @param request 下单请求
     * @return 下单响应
     */
    WechatPayCombineCreateResponse create(WechatPayCombineCreateRequest request);

    /**
     * 查询合单支付订单。
     *
     * @param request 查询请求
     * @return 查询结果
     */
    WechatPayCombineOrderResponse queryOrder(WechatPayCombineOrderRequest request);

    /**
     * 关闭合单支付订单。
     *
     * @param request 关单请求
     * @return 关单结果
     */
    WechatPayCombineCloseResponse close(WechatPayCombineCloseRequest request);

    /**
     * 解析合单支付通知。
     *
     * @param request 通知请求
     * @return 通知解析结果
     */
    WechatPayCombineNotifyResponse parseNotify(WechatPayCombineNotifyRequest request);
}
