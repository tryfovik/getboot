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
package com.getboot.payment.spi.wechatpay;

import com.getboot.payment.api.request.PaymentCloseRequest;
import com.getboot.payment.api.request.PaymentCreateRequest;
import com.getboot.payment.api.request.PaymentOrderQueryRequest;
import com.getboot.payment.api.request.PaymentRefundQueryRequest;
import com.getboot.payment.api.request.PaymentRefundRequest;
import com.getboot.payment.api.wechatpay.combine.WechatPayCombineCloseRequest;
import com.getboot.payment.api.wechatpay.combine.WechatPayCombineCreateRequest;
import com.getboot.payment.api.wechatpay.combine.WechatPayCombineOrderRequest;
import com.getboot.payment.api.wechatpay.trade.WechatPayAbnormalRefundRequest;
import com.getboot.payment.api.wechatpay.trade.WechatPayFundFlowBillRequest;
import com.getboot.payment.api.wechatpay.trade.WechatPayTradeBillRequest;

/**
 * 微信支付请求扩展 SPI。
 *
 * <p>业务方可通过实现该接口，在请求发出前统一覆盖上下文与补充长尾字段。</p>
 *
 * @author qiheng
 */
public interface WechatPayRequestCustomizer {

    /**
     * 自定义统一下单请求。
     *
     * @param request 下单请求
     * @param options 可变请求选项
     */
    default void customizeCreate(PaymentCreateRequest request, WechatPayRequestOptions options) {
    }

    /**
     * 自定义退款请求。
     *
     * @param request 退款请求
     * @param options 可变请求选项
     */
    default void customizeRefund(PaymentRefundRequest request, WechatPayRequestOptions options) {
    }

    /**
     * 自定义查单请求。
     *
     * @param request 查单请求
     * @param options 可变请求选项
     */
    default void customizeQueryOrder(PaymentOrderQueryRequest request, WechatPayRequestOptions options) {
    }

    /**
     * 自定义查退款请求。
     *
     * @param request 查退款请求
     * @param options 可变请求选项
     */
    default void customizeQueryRefund(PaymentRefundQueryRequest request, WechatPayRequestOptions options) {
    }

    /**
     * 自定义关单请求。
     *
     * @param request 关单请求
     * @param options 可变请求选项
     */
    default void customizeClose(PaymentCloseRequest request, WechatPayRequestOptions options) {
    }

    /**
     * 自定义合单下单请求。
     *
     * @param request 合单下单请求
     * @param options 可变请求选项
     */
    default void customizeCombineCreate(WechatPayCombineCreateRequest request, WechatPayRequestOptions options) {
    }

    /**
     * 自定义合单查单请求。
     *
     * @param request 合单查单请求
     * @param options 可变请求选项
     */
    default void customizeCombineQueryOrder(WechatPayCombineOrderRequest request, WechatPayRequestOptions options) {
    }

    /**
     * 自定义合单关单请求。
     *
     * @param request 合单关单请求
     * @param options 可变请求选项
     */
    default void customizeCombineClose(WechatPayCombineCloseRequest request, WechatPayRequestOptions options) {
    }

    /**
     * 自定义交易账单请求。
     *
     * @param request 交易账单请求
     * @param options 可变请求选项
     */
    default void customizeTradeBill(WechatPayTradeBillRequest request, WechatPayRequestOptions options) {
    }

    /**
     * 自定义资金账单请求。
     *
     * @param request 资金账单请求
     * @param options 可变请求选项
     */
    default void customizeFundFlowBill(WechatPayFundFlowBillRequest request, WechatPayRequestOptions options) {
    }

    /**
     * 自定义异常退款请求。
     *
     * @param request 异常退款请求
     * @param options 可变请求选项
     */
    default void customizeAbnormalRefund(WechatPayAbnormalRefundRequest request, WechatPayRequestOptions options) {
    }
}
