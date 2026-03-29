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
package com.getboot.payment.support.alipay;

import com.alipay.easysdk.payment.app.models.AlipayTradeAppPayResponse;
import com.alipay.easysdk.payment.common.models.AlipayDataDataserviceBillDownloadurlQueryResponse;
import com.alipay.easysdk.payment.common.models.AlipayTradeCancelResponse;
import com.alipay.easysdk.payment.common.models.AlipayTradeCloseResponse;
import com.alipay.easysdk.payment.common.models.AlipayTradeFastpayRefundQueryResponse;
import com.alipay.easysdk.payment.common.models.AlipayTradeQueryResponse;
import com.alipay.easysdk.payment.common.models.AlipayTradeRefundResponse;
import com.alipay.easysdk.payment.facetoface.models.AlipayTradePayResponse;
import com.alipay.easysdk.payment.facetoface.models.AlipayTradePrecreateResponse;
import com.alipay.easysdk.payment.huabei.models.HuabeiConfig;
import com.alipay.easysdk.payment.page.models.AlipayTradePagePayResponse;
import com.alipay.easysdk.payment.wap.models.AlipayTradeWapPayResponse;
import com.alipay.easysdk.util.generic.models.AlipayOpenApiGenericResponse;

import java.util.Map;

/**
 * 支付宝官方 SDK 网关。
 *
 * <p>用于隔离 SDK 可变客户端与业务层，便于模块内复用与测试。</p>
 *
 * @author qiheng
 */
public interface AlipayGateway {

    /**
     * 生成 App 支付订单串。
     */
    AlipayTradeAppPayResponse appPay(
            String subject,
            String outTradeNo,
            String totalAmount,
            String notifyUrl,
            Map<String, Object> optionalArgs,
            AlipayRequestContext requestContext) throws Exception;

    /**
     * 生成电脑网站支付表单。
     */
    AlipayTradePagePayResponse pagePay(
            String subject,
            String outTradeNo,
            String totalAmount,
            String returnUrl,
            String notifyUrl,
            Map<String, Object> optionalArgs,
            AlipayRequestContext requestContext) throws Exception;

    /**
     * 生成手机网站支付表单。
     */
    AlipayTradeWapPayResponse wapPay(
            String subject,
            String outTradeNo,
            String totalAmount,
            String quitUrl,
            String returnUrl,
            String notifyUrl,
            Map<String, Object> optionalArgs,
            AlipayRequestContext requestContext) throws Exception;

    /**
     * 预创建扫码支付订单。
     */
    AlipayTradePrecreateResponse preCreate(
            String subject,
            String outTradeNo,
            String totalAmount,
            String notifyUrl,
            Map<String, Object> optionalArgs,
            AlipayRequestContext requestContext) throws Exception;

    /**
     * 发起当面付条码支付。
     */
    default AlipayTradePayResponse facePay(
            String subject,
            String outTradeNo,
            String totalAmount,
            String authCode,
            String notifyUrl,
            Map<String, Object> optionalArgs,
            AlipayRequestContext requestContext) throws Exception {
        throw new UnsupportedOperationException("AlipayGateway#facePay is not implemented");
    }

    /**
     * 创建花呗分期交易。
     */
    default com.alipay.easysdk.payment.huabei.models.AlipayTradeCreateResponse huabeiCreate(
            String subject,
            String outTradeNo,
            String totalAmount,
            String buyerId,
            HuabeiConfig extendParams,
            String notifyUrl,
            Map<String, Object> optionalArgs,
            AlipayRequestContext requestContext) throws Exception {
        throw new UnsupportedOperationException("AlipayGateway#huabeiCreate is not implemented");
    }

    /**
     * 查询交易。
     */
    AlipayTradeQueryResponse query(
            String outTradeNo,
            Map<String, Object> optionalArgs,
            AlipayRequestContext requestContext) throws Exception;

    /**
     * 发起退款。
     */
    AlipayTradeRefundResponse refund(
            String outTradeNo,
            String refundAmount,
            String notifyUrl,
            Map<String, Object> optionalArgs,
            AlipayRequestContext requestContext) throws Exception;

    /**
     * 查询退款。
     */
    AlipayTradeFastpayRefundQueryResponse queryRefund(
            String outTradeNo,
            String outRequestNo,
            Map<String, Object> optionalArgs,
            AlipayRequestContext requestContext) throws Exception;

    /**
     * 关闭交易。
     */
    AlipayTradeCloseResponse close(
            String outTradeNo,
            Map<String, Object> optionalArgs,
            AlipayRequestContext requestContext) throws Exception;

    /**
     * 撤销交易。
     */
    AlipayTradeCancelResponse cancel(
            String outTradeNo,
            Map<String, Object> optionalArgs,
            AlipayRequestContext requestContext) throws Exception;

    /**
     * 查询账单下载地址。
     */
    AlipayDataDataserviceBillDownloadurlQueryResponse downloadBill(
            String billType,
            String billDate,
            Map<String, Object> optionalArgs,
            AlipayRequestContext requestContext) throws Exception;

    /**
     * 校验支付宝异步通知签名。
     */
    boolean verifyNotify(Map<String, String> parameters) throws Exception;

    /**
     * 发起通用 OpenAPI 调用。
     */
    default AlipayOpenApiGenericResponse execute(
            String method,
            Map<String, String> textParams,
            Map<String, Object> bizParams) throws Exception {
        return execute(method, textParams, bizParams, null);
    }

    /**
     * 发起带上下文的通用 OpenAPI 调用。
     */
    default AlipayOpenApiGenericResponse execute(
            String method,
            Map<String, String> textParams,
            Map<String, Object> bizParams,
            AlipayRequestContext requestContext) throws Exception {
        throw new UnsupportedOperationException("AlipayGateway#execute is not implemented");
    }
}
