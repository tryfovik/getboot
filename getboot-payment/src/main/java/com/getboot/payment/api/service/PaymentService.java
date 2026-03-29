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
package com.getboot.payment.api.service;

import com.getboot.payment.api.model.PaymentChannel;
import com.getboot.payment.api.model.PaymentMode;
import com.getboot.payment.api.request.PaymentCloseRequest;
import com.getboot.payment.api.request.PaymentCreateRequest;
import com.getboot.payment.api.request.PaymentNotifyRequest;
import com.getboot.payment.api.request.PaymentOrderQueryRequest;
import com.getboot.payment.api.request.PaymentRefundRequest;
import com.getboot.payment.api.request.PaymentRefundQueryRequest;
import com.getboot.payment.api.response.PaymentCloseResponse;
import com.getboot.payment.api.response.PaymentCreateResponse;
import com.getboot.payment.api.response.PaymentNotifyResponse;
import com.getboot.payment.api.response.PaymentOrderQueryResponse;
import com.getboot.payment.api.response.PaymentRefundResponse;
import com.getboot.payment.api.response.PaymentRefundQueryResponse;

import java.util.Set;

/**
 * 支付能力接口。
 *
 * <p>每个渠道实现提供一个对应的支付服务 Bean，并通过注册表按渠道路由。</p>
 *
 * @author qiheng
 */
public interface PaymentService {

    /**
     * 当前服务支持的支付渠道。
     *
     * @return 支付渠道
     */
    PaymentChannel channel();

    /**
     * 当前服务支持的支付方式集合。
     *
     * @return 支付方式集合
     */
    Set<PaymentMode> supportedModes();

    /**
     * 判断当前服务是否支持指定支付方式。
     *
     * @param mode 支付方式
     * @return 是否支持
     */
    default boolean supports(PaymentMode mode) {
        return mode != null && supportedModes().contains(mode);
    }

    /**
     * 创建支付订单。
     *
     * @param request 下单请求
     * @return 下单响应
     */
    PaymentCreateResponse create(PaymentCreateRequest request);

    /**
     * 发起退款。
     *
     * @param request 退款请求
     * @return 退款响应
     */
    PaymentRefundResponse refund(PaymentRefundRequest request);

    /**
     * 查询支付订单状态。
     *
     * @param request 查询请求
     * @return 查询响应
     */
    PaymentOrderQueryResponse queryOrder(PaymentOrderQueryRequest request);

    /**
     * 查询退款状态。
     *
     * @param request 查询请求
     * @return 查询响应
     */
    PaymentRefundQueryResponse queryRefund(PaymentRefundQueryRequest request);

    /**
     * 关闭支付订单。
     *
     * @param request 关闭请求
     * @return 关闭响应
     */
    PaymentCloseResponse close(PaymentCloseRequest request);

    /**
     * 解析异步通知。
     *
     * @param request 通知请求
     * @return 解析结果
     */
    PaymentNotifyResponse parseNotify(PaymentNotifyRequest request);
}
