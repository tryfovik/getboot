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
package com.getboot.payment.spi.alipay;

import com.getboot.payment.api.alipay.settlement.AlipayAccountQueryRequest;
import com.getboot.payment.api.alipay.settlement.AlipayElectronicReceiptApplyRequest;
import com.getboot.payment.api.alipay.settlement.AlipayElectronicReceiptQueryRequest;
import com.getboot.payment.api.alipay.settlement.AlipayTransferQueryRequest;
import com.getboot.payment.api.alipay.settlement.AlipayTransferRequest;
import com.getboot.payment.api.alipay.trade.AlipayTradeBillRequest;
import com.getboot.payment.api.alipay.trade.AlipayTradeCancelRequest;
import com.getboot.payment.api.alipay.trade.facetoface.AlipayFaceToFacePayRequest;
import com.getboot.payment.api.alipay.trade.huabei.AlipayHuabeiCreateRequest;
import com.getboot.payment.api.request.PaymentCloseRequest;
import com.getboot.payment.api.request.PaymentCreateRequest;
import com.getboot.payment.api.request.PaymentOrderQueryRequest;
import com.getboot.payment.api.request.PaymentRefundQueryRequest;
import com.getboot.payment.api.request.PaymentRefundRequest;

/**
 * 支付宝请求扩展 SPI。
 *
 * <p>业务方可通过实现该接口，在请求发出前统一覆盖上下文与扩展参数。</p>
 *
 * @author qiheng
 */
public interface AlipayRequestCustomizer {

    /**
     * 自定义统一下单请求。
     *
     * @param request 下单请求
     * @param options 可变请求选项
     */
    default void customizeCreate(PaymentCreateRequest request, AlipayRequestOptions options) {
    }

    /**
     * 自定义退款请求。
     *
     * @param request 退款请求
     * @param options 可变请求选项
     */
    default void customizeRefund(PaymentRefundRequest request, AlipayRequestOptions options) {
    }

    /**
     * 自定义查单请求。
     *
     * @param request 查单请求
     * @param options 可变请求选项
     */
    default void customizeQueryOrder(PaymentOrderQueryRequest request, AlipayRequestOptions options) {
    }

    /**
     * 自定义查退款请求。
     *
     * @param request 查退款请求
     * @param options 可变请求选项
     */
    default void customizeQueryRefund(PaymentRefundQueryRequest request, AlipayRequestOptions options) {
    }

    /**
     * 自定义关单请求。
     *
     * @param request 关单请求
     * @param options 可变请求选项
     */
    default void customizeClose(PaymentCloseRequest request, AlipayRequestOptions options) {
    }

    /**
     * 自定义条码支付请求。
     *
     * @param request 条码支付请求
     * @param options 可变请求选项
     */
    default void customizeFaceToFacePay(AlipayFaceToFacePayRequest request, AlipayRequestOptions options) {
    }

    /**
     * 自定义花呗分期创建请求。
     *
     * @param request 花呗分期请求
     * @param options 可变请求选项
     */
    default void customizeHuabeiCreate(AlipayHuabeiCreateRequest request, AlipayRequestOptions options) {
    }

    /**
     * 自定义账单下载地址查询请求。
     *
     * @param request 查询请求
     * @param options 可变请求选项
     */
    default void customizeDownloadBill(AlipayTradeBillRequest request, AlipayRequestOptions options) {
    }

    /**
     * 自定义撤销请求。
     *
     * @param request 撤销请求
     * @param options 可变请求选项
     */
    default void customizeCancel(AlipayTradeCancelRequest request, AlipayRequestOptions options) {
    }

    /**
     * 自定义单笔转账请求。
     *
     * @param request 转账请求
     * @param options 可变请求选项
     */
    default void customizeTransfer(AlipayTransferRequest request, AlipayRequestOptions options) {
    }

    /**
     * 自定义转账查询请求。
     *
     * @param request 查询请求
     * @param options 可变请求选项
     */
    default void customizeTransferQuery(AlipayTransferQueryRequest request, AlipayRequestOptions options) {
    }

    /**
     * 自定义账户查询请求。
     *
     * @param request 查询请求
     * @param options 可变请求选项
     */
    default void customizeAccountQuery(AlipayAccountQueryRequest request, AlipayRequestOptions options) {
    }

    /**
     * 自定义电子回单申请请求。
     *
     * @param request 申请请求
     * @param options 可变请求选项
     */
    default void customizeApplyElectronicReceipt(
            AlipayElectronicReceiptApplyRequest request,
            AlipayRequestOptions options) {
    }

    /**
     * 自定义电子回单查询请求。
     *
     * @param request 查询请求
     * @param options 可变请求选项
     */
    default void customizeQueryElectronicReceipt(
            AlipayElectronicReceiptQueryRequest request,
            AlipayRequestOptions options) {
    }
}
