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

import java.util.Map;

/**
 * 微信支付分能力。
 *
 * @author qiheng
 */
public interface WechatPayPayScoreService {

    /**
     * 创建支付分订单。
     *
     * @param requestBody 官方请求体
     * @return 原始响应
     */
    Map<String, Object> createOrder(Object requestBody);

    /**
     * 查询支付分订单。
     *
     * @param request 查询参数
     * @return 原始响应
     */
    Map<String, Object> queryOrder(WechatPayPayScoreQueryRequest request);

    /**
     * 取消支付分订单。
     *
     * @param outOrderNo 商户服务订单号
     * @param requestBody 官方请求体
     */
    void cancelOrder(String outOrderNo, Object requestBody);

    /**
     * 完结支付分订单。
     *
     * @param outOrderNo 商户服务订单号
     * @param requestBody 官方请求体
     */
    void completeOrder(String outOrderNo, Object requestBody);

    /**
     * 修改支付分订单金额。
     *
     * @param outOrderNo 商户服务订单号
     * @param requestBody 官方请求体
     */
    void modifyOrderAmount(String outOrderNo, Object requestBody);

    /**
     * 同步支付分订单状态。
     *
     * @param outOrderNo 商户服务订单号
     * @param requestBody 官方请求体
     */
    void syncOrder(String outOrderNo, Object requestBody);

    /**
     * 申请支付分订单退款。
     *
     * @param requestBody 官方请求体
     * @return 原始响应
     */
    Map<String, Object> createOrderRefund(Object requestBody);

    /**
     * 查询支付分订单退款。
     *
     * @param outRefundNo 商户退款单号
     * @return 原始响应
     */
    Map<String, Object> queryOrderRefund(String outRefundNo);

    /**
     * 构造 JSAPI 调起支付分确认页参数。
     *
     * @param packageValue 查询支付分创单返回的 package 参数
     * @return 前端业务视图参数
     */
    WechatPayJsapiBusinessViewRequest buildJsapiConfirmOrderView(String packageValue);

    /**
     * 构造 JSAPI 调起支付分订单详情页参数。
     *
     * @param request 详情页请求
     * @return 前端业务视图参数
     */
    WechatPayJsapiBusinessViewRequest buildJsapiOrderDetailView(WechatPayPayScoreDetailViewRequest request);
}
