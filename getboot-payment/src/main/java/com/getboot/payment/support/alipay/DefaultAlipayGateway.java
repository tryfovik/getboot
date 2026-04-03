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

import com.alipay.easysdk.kernel.Client;
import com.alipay.easysdk.kernel.Context;
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
 * 默认支付宝 SDK 网关实现。
 *
 * @author qiheng
 */
public class DefaultAlipayGateway implements AlipayGateway {

    /**
     * 共享 SDK 上下文。
     */
    private final Context context;

    /**
     * 构造默认网关。
     *
     * @param context SDK 上下文
     */
    public DefaultAlipayGateway(Context context) {
        this.context = context;
    }

    /**
     * 调用 APP 支付下单接口。
     *
     * @param subject 订单标题
     * @param outTradeNo 商户订单号
     * @param totalAmount 订单金额
     * @param notifyUrl 通知地址
     * @param optionalArgs 可选参数
     * @param requestContext 调用上下文
     * @return 支付宝响应
     * @throws Exception SDK 调用异常
     */
    @Override
    public AlipayTradeAppPayResponse appPay(
            String subject,
            String outTradeNo,
            String totalAmount,
            String notifyUrl,
            Map<String, Object> optionalArgs,
            AlipayRequestContext requestContext) throws Exception {
        com.alipay.easysdk.payment.app.Client client =
                configure(new com.alipay.easysdk.payment.app.Client(newKernelClient()), notifyUrl, optionalArgs, requestContext);
        return client.pay(subject, outTradeNo, totalAmount);
    }

    /**
     * 调用页面支付下单接口。
     *
     * @param subject 订单标题
     * @param outTradeNo 商户订单号
     * @param totalAmount 订单金额
     * @param returnUrl 返回地址
     * @param notifyUrl 通知地址
     * @param optionalArgs 可选参数
     * @param requestContext 调用上下文
     * @return 支付宝响应
     * @throws Exception SDK 调用异常
     */
    @Override
    public AlipayTradePagePayResponse pagePay(
            String subject,
            String outTradeNo,
            String totalAmount,
            String returnUrl,
            String notifyUrl,
            Map<String, Object> optionalArgs,
            AlipayRequestContext requestContext) throws Exception {
        com.alipay.easysdk.payment.page.Client client =
                configure(new com.alipay.easysdk.payment.page.Client(newKernelClient()), notifyUrl, optionalArgs, requestContext);
        return client.pay(subject, outTradeNo, totalAmount, returnUrl);
    }

    /**
     * 调用 WAP 支付下单接口。
     *
     * @param subject 订单标题
     * @param outTradeNo 商户订单号
     * @param totalAmount 订单金额
     * @param quitUrl 退出地址
     * @param returnUrl 返回地址
     * @param notifyUrl 通知地址
     * @param optionalArgs 可选参数
     * @param requestContext 调用上下文
     * @return 支付宝响应
     * @throws Exception SDK 调用异常
     */
    @Override
    public AlipayTradeWapPayResponse wapPay(
            String subject,
            String outTradeNo,
            String totalAmount,
            String quitUrl,
            String returnUrl,
            String notifyUrl,
            Map<String, Object> optionalArgs,
            AlipayRequestContext requestContext) throws Exception {
        com.alipay.easysdk.payment.wap.Client client =
                configure(new com.alipay.easysdk.payment.wap.Client(newKernelClient()), notifyUrl, optionalArgs, requestContext);
        return client.pay(subject, outTradeNo, totalAmount, quitUrl, returnUrl);
    }

    /**
     * 调用预下单接口。
     *
     * @param subject 订单标题
     * @param outTradeNo 商户订单号
     * @param totalAmount 订单金额
     * @param notifyUrl 通知地址
     * @param optionalArgs 可选参数
     * @param requestContext 调用上下文
     * @return 支付宝响应
     * @throws Exception SDK 调用异常
     */
    @Override
    public AlipayTradePrecreateResponse preCreate(
            String subject,
            String outTradeNo,
            String totalAmount,
            String notifyUrl,
            Map<String, Object> optionalArgs,
            AlipayRequestContext requestContext) throws Exception {
        com.alipay.easysdk.payment.facetoface.Client client =
                configure(new com.alipay.easysdk.payment.facetoface.Client(newKernelClient()), notifyUrl, optionalArgs, requestContext);
        return client.preCreate(subject, outTradeNo, totalAmount);
    }

    /**
     * 调用当面付接口。
     *
     * @param subject 订单标题
     * @param outTradeNo 商户订单号
     * @param totalAmount 订单金额
     * @param authCode 付款码
     * @param notifyUrl 通知地址
     * @param optionalArgs 可选参数
     * @param requestContext 调用上下文
     * @return 支付宝响应
     * @throws Exception SDK 调用异常
     */
    @Override
    public AlipayTradePayResponse facePay(
            String subject,
            String outTradeNo,
            String totalAmount,
            String authCode,
            String notifyUrl,
            Map<String, Object> optionalArgs,
            AlipayRequestContext requestContext) throws Exception {
        com.alipay.easysdk.payment.facetoface.Client client =
                configure(new com.alipay.easysdk.payment.facetoface.Client(newKernelClient()), notifyUrl, optionalArgs, requestContext);
        return client.pay(subject, outTradeNo, totalAmount, authCode);
    }

    /**
     * 调用花呗分期建单接口。
     *
     * @param subject 订单标题
     * @param outTradeNo 商户订单号
     * @param totalAmount 订单金额
     * @param buyerId 买家 ID
     * @param extendParams 花呗扩展参数
     * @param notifyUrl 通知地址
     * @param optionalArgs 可选参数
     * @param requestContext 调用上下文
     * @return 支付宝响应
     * @throws Exception SDK 调用异常
     */
    @Override
    public com.alipay.easysdk.payment.huabei.models.AlipayTradeCreateResponse huabeiCreate(
            String subject,
            String outTradeNo,
            String totalAmount,
            String buyerId,
            HuabeiConfig extendParams,
            String notifyUrl,
            Map<String, Object> optionalArgs,
            AlipayRequestContext requestContext) throws Exception {
        com.alipay.easysdk.payment.huabei.Client client =
                configure(new com.alipay.easysdk.payment.huabei.Client(newKernelClient()), notifyUrl, optionalArgs, requestContext);
        return client.create(subject, outTradeNo, totalAmount, buyerId, extendParams);
    }

    /**
     * 调用订单查询接口。
     *
     * @param outTradeNo 商户订单号
     * @param optionalArgs 可选参数
     * @param requestContext 调用上下文
     * @return 支付宝响应
     * @throws Exception SDK 调用异常
     */
    @Override
    public AlipayTradeQueryResponse query(
            String outTradeNo,
            Map<String, Object> optionalArgs,
            AlipayRequestContext requestContext) throws Exception {
        com.alipay.easysdk.payment.common.Client client =
                configure(new com.alipay.easysdk.payment.common.Client(newKernelClient()), null, optionalArgs, requestContext);
        return client.query(outTradeNo);
    }

    /**
     * 调用退款接口。
     *
     * @param outTradeNo 商户订单号
     * @param refundAmount 退款金额
     * @param notifyUrl 通知地址
     * @param optionalArgs 可选参数
     * @param requestContext 调用上下文
     * @return 支付宝响应
     * @throws Exception SDK 调用异常
     */
    @Override
    public AlipayTradeRefundResponse refund(
            String outTradeNo,
            String refundAmount,
            String notifyUrl,
            Map<String, Object> optionalArgs,
            AlipayRequestContext requestContext) throws Exception {
        com.alipay.easysdk.payment.common.Client client =
                configure(new com.alipay.easysdk.payment.common.Client(newKernelClient()), notifyUrl, optionalArgs, requestContext);
        return client.refund(outTradeNo, refundAmount);
    }

    /**
     * 调用退款查询接口。
     *
     * @param outTradeNo 商户订单号
     * @param outRequestNo 退款请求号
     * @param optionalArgs 可选参数
     * @param requestContext 调用上下文
     * @return 支付宝响应
     * @throws Exception SDK 调用异常
     */
    @Override
    public AlipayTradeFastpayRefundQueryResponse queryRefund(
            String outTradeNo,
            String outRequestNo,
            Map<String, Object> optionalArgs,
            AlipayRequestContext requestContext) throws Exception {
        com.alipay.easysdk.payment.common.Client client =
                configure(new com.alipay.easysdk.payment.common.Client(newKernelClient()), null, optionalArgs, requestContext);
        return client.queryRefund(outTradeNo, outRequestNo);
    }

    /**
     * 调用关单接口。
     *
     * @param outTradeNo 商户订单号
     * @param optionalArgs 可选参数
     * @param requestContext 调用上下文
     * @return 支付宝响应
     * @throws Exception SDK 调用异常
     */
    @Override
    public AlipayTradeCloseResponse close(
            String outTradeNo,
            Map<String, Object> optionalArgs,
            AlipayRequestContext requestContext) throws Exception {
        com.alipay.easysdk.payment.common.Client client =
                configure(new com.alipay.easysdk.payment.common.Client(newKernelClient()), null, optionalArgs, requestContext);
        return client.close(outTradeNo);
    }

    /**
     * 调用撤销接口。
     *
     * @param outTradeNo 商户订单号
     * @param optionalArgs 可选参数
     * @param requestContext 调用上下文
     * @return 支付宝响应
     * @throws Exception SDK 调用异常
     */
    @Override
    public AlipayTradeCancelResponse cancel(
            String outTradeNo,
            Map<String, Object> optionalArgs,
            AlipayRequestContext requestContext) throws Exception {
        com.alipay.easysdk.payment.common.Client client =
                configure(new com.alipay.easysdk.payment.common.Client(newKernelClient()), null, optionalArgs, requestContext);
        return client.cancel(outTradeNo);
    }

    /**
     * 调用账单下载地址查询接口。
     *
     * @param billType 账单类型
     * @param billDate 账单日期
     * @param optionalArgs 可选参数
     * @param requestContext 调用上下文
     * @return 支付宝响应
     * @throws Exception SDK 调用异常
     */
    @Override
    public AlipayDataDataserviceBillDownloadurlQueryResponse downloadBill(
            String billType,
            String billDate,
            Map<String, Object> optionalArgs,
            AlipayRequestContext requestContext) throws Exception {
        com.alipay.easysdk.payment.common.Client client =
                configure(new com.alipay.easysdk.payment.common.Client(newKernelClient()), null, optionalArgs, requestContext);
        return client.downloadBill(billType, billDate);
    }

    /**
     * 校验支付宝异步通知签名。
     *
     * @param parameters 通知参数
     * @return 是否验签通过
     * @throws Exception SDK 调用异常
     */
    @Override
    public boolean verifyNotify(Map<String, String> parameters) throws Exception {
        return new com.alipay.easysdk.payment.common.Client(newKernelClient()).verifyNotify(parameters);
    }

    /**
     * 执行泛化 OpenAPI 调用。
     *
     * @param method 方法名
     * @param textParams 文本参数
     * @param bizParams 业务参数
     * @return 泛化响应
     * @throws Exception SDK 调用异常
     */
    @Override
    public AlipayOpenApiGenericResponse execute(
            String method,
            Map<String, String> textParams,
            Map<String, Object> bizParams) throws Exception {
        return execute(method, textParams, bizParams, null);
    }

    /**
     * 携带上下文执行泛化 OpenAPI 调用。
     *
     * @param method 方法名
     * @param textParams 文本参数
     * @param bizParams 业务参数
     * @param requestContext 调用上下文
     * @return 泛化响应
     * @throws Exception SDK 调用异常
     */
    @Override
    public AlipayOpenApiGenericResponse execute(
            String method,
            Map<String, String> textParams,
            Map<String, Object> bizParams,
            AlipayRequestContext requestContext) throws Exception {
        return configure(new com.alipay.easysdk.util.generic.Client(newKernelClient()), requestContext)
                .execute(method, textParams, bizParams);
    }

    /**
     * 创建新的内核客户端。
     *
     * @return 内核客户端
     */
    private Client newKernelClient() {
        return new Client(context);
    }

    /**
     * 配置 APP 支付客户端。
     *
     * @param client SDK 客户端
     * @param notifyUrl 通知地址
     * @param optionalArgs 可选参数
     * @param requestContext 调用上下文
     * @return 已配置客户端
     */
    private com.alipay.easysdk.payment.app.Client configure(
            com.alipay.easysdk.payment.app.Client client,
            String notifyUrl,
            Map<String, Object> optionalArgs,
            AlipayRequestContext requestContext) {
        applyShared(client, notifyUrl, optionalArgs, requestContext);
        return client;
    }

    /**
     * 配置页面支付客户端。
     *
     * @param client SDK 客户端
     * @param notifyUrl 通知地址
     * @param optionalArgs 可选参数
     * @param requestContext 调用上下文
     * @return 已配置客户端
     */
    private com.alipay.easysdk.payment.page.Client configure(
            com.alipay.easysdk.payment.page.Client client,
            String notifyUrl,
            Map<String, Object> optionalArgs,
            AlipayRequestContext requestContext) {
        applyShared(client, notifyUrl, optionalArgs, requestContext);
        return client;
    }

    /**
     * 配置 WAP 支付客户端。
     *
     * @param client SDK 客户端
     * @param notifyUrl 通知地址
     * @param optionalArgs 可选参数
     * @param requestContext 调用上下文
     * @return 已配置客户端
     */
    private com.alipay.easysdk.payment.wap.Client configure(
            com.alipay.easysdk.payment.wap.Client client,
            String notifyUrl,
            Map<String, Object> optionalArgs,
            AlipayRequestContext requestContext) {
        applyShared(client, notifyUrl, optionalArgs, requestContext);
        return client;
    }

    /**
     * 配置当面付客户端。
     *
     * @param client SDK 客户端
     * @param notifyUrl 通知地址
     * @param optionalArgs 可选参数
     * @param requestContext 调用上下文
     * @return 已配置客户端
     */
    private com.alipay.easysdk.payment.facetoface.Client configure(
            com.alipay.easysdk.payment.facetoface.Client client,
            String notifyUrl,
            Map<String, Object> optionalArgs,
            AlipayRequestContext requestContext) {
        applyShared(client, notifyUrl, optionalArgs, requestContext);
        return client;
    }

    /**
     * 配置通用支付客户端。
     *
     * @param client SDK 客户端
     * @param notifyUrl 通知地址
     * @param optionalArgs 可选参数
     * @param requestContext 调用上下文
     * @return 已配置客户端
     */
    private com.alipay.easysdk.payment.common.Client configure(
            com.alipay.easysdk.payment.common.Client client,
            String notifyUrl,
            Map<String, Object> optionalArgs,
            AlipayRequestContext requestContext) {
        applyShared(client, notifyUrl, optionalArgs, requestContext);
        return client;
    }

    /**
     * 配置花呗分期客户端。
     *
     * @param client SDK 客户端
     * @param notifyUrl 通知地址
     * @param optionalArgs 可选参数
     * @param requestContext 调用上下文
     * @return 已配置客户端
     */
    private com.alipay.easysdk.payment.huabei.Client configure(
            com.alipay.easysdk.payment.huabei.Client client,
            String notifyUrl,
            Map<String, Object> optionalArgs,
            AlipayRequestContext requestContext) {
        applyShared(client, notifyUrl, optionalArgs, requestContext);
        return client;
    }

    /**
     * 配置泛化调用客户端。
     *
     * @param client SDK 客户端
     * @param requestContext 调用上下文
     * @return 已配置客户端
     */
    private com.alipay.easysdk.util.generic.Client configure(
            com.alipay.easysdk.util.generic.Client client,
            AlipayRequestContext requestContext) {
        if (requestContext != null) {
            if (requestContext.appAuthToken() != null) {
                client.agent(requestContext.appAuthToken());
            }
            if (requestContext.authToken() != null) {
                client.auth(requestContext.authToken());
            }
            if (requestContext.route() != null) {
                client.route(requestContext.route());
            }
        }
        return client;
    }

    /**
     * 应用 APP 客户端通用配置。
     *
     * @param client SDK 客户端
     * @param notifyUrl 通知地址
     * @param optionalArgs 可选参数
     * @param requestContext 调用上下文
     */
    private void applyShared(
            com.alipay.easysdk.payment.app.Client client,
            String notifyUrl,
            Map<String, Object> optionalArgs,
            AlipayRequestContext requestContext) {
        if (requestContext != null) {
            if (requestContext.appAuthToken() != null) {
                client.agent(requestContext.appAuthToken());
            }
            if (requestContext.authToken() != null) {
                client.auth(requestContext.authToken());
            }
            if (requestContext.route() != null) {
                client.route(requestContext.route());
            }
        }
        if (notifyUrl != null) {
            client.asyncNotify(notifyUrl);
        }
        if (optionalArgs != null && !optionalArgs.isEmpty()) {
            client.batchOptional(optionalArgs);
        }
    }

    /**
     * 应用页面支付客户端通用配置。
     *
     * @param client SDK 客户端
     * @param notifyUrl 通知地址
     * @param optionalArgs 可选参数
     * @param requestContext 调用上下文
     */
    private void applyShared(
            com.alipay.easysdk.payment.page.Client client,
            String notifyUrl,
            Map<String, Object> optionalArgs,
            AlipayRequestContext requestContext) {
        if (requestContext != null) {
            if (requestContext.appAuthToken() != null) {
                client.agent(requestContext.appAuthToken());
            }
            if (requestContext.authToken() != null) {
                client.auth(requestContext.authToken());
            }
            if (requestContext.route() != null) {
                client.route(requestContext.route());
            }
        }
        if (notifyUrl != null) {
            client.asyncNotify(notifyUrl);
        }
        if (optionalArgs != null && !optionalArgs.isEmpty()) {
            client.batchOptional(optionalArgs);
        }
    }

    /**
     * 应用 WAP 支付客户端通用配置。
     *
     * @param client SDK 客户端
     * @param notifyUrl 通知地址
     * @param optionalArgs 可选参数
     * @param requestContext 调用上下文
     */
    private void applyShared(
            com.alipay.easysdk.payment.wap.Client client,
            String notifyUrl,
            Map<String, Object> optionalArgs,
            AlipayRequestContext requestContext) {
        if (requestContext != null) {
            if (requestContext.appAuthToken() != null) {
                client.agent(requestContext.appAuthToken());
            }
            if (requestContext.authToken() != null) {
                client.auth(requestContext.authToken());
            }
            if (requestContext.route() != null) {
                client.route(requestContext.route());
            }
        }
        if (notifyUrl != null) {
            client.asyncNotify(notifyUrl);
        }
        if (optionalArgs != null && !optionalArgs.isEmpty()) {
            client.batchOptional(optionalArgs);
        }
    }

    /**
     * 应用当面付客户端通用配置。
     *
     * @param client SDK 客户端
     * @param notifyUrl 通知地址
     * @param optionalArgs 可选参数
     * @param requestContext 调用上下文
     */
    private void applyShared(
            com.alipay.easysdk.payment.facetoface.Client client,
            String notifyUrl,
            Map<String, Object> optionalArgs,
            AlipayRequestContext requestContext) {
        if (requestContext != null) {
            if (requestContext.appAuthToken() != null) {
                client.agent(requestContext.appAuthToken());
            }
            if (requestContext.authToken() != null) {
                client.auth(requestContext.authToken());
            }
            if (requestContext.route() != null) {
                client.route(requestContext.route());
            }
        }
        if (notifyUrl != null) {
            client.asyncNotify(notifyUrl);
        }
        if (optionalArgs != null && !optionalArgs.isEmpty()) {
            client.batchOptional(optionalArgs);
        }
    }

    /**
     * 应用通用支付客户端通用配置。
     *
     * @param client SDK 客户端
     * @param notifyUrl 通知地址
     * @param optionalArgs 可选参数
     * @param requestContext 调用上下文
     */
    private void applyShared(
            com.alipay.easysdk.payment.common.Client client,
            String notifyUrl,
            Map<String, Object> optionalArgs,
            AlipayRequestContext requestContext) {
        if (requestContext != null) {
            if (requestContext.appAuthToken() != null) {
                client.agent(requestContext.appAuthToken());
            }
            if (requestContext.authToken() != null) {
                client.auth(requestContext.authToken());
            }
            if (requestContext.route() != null) {
                client.route(requestContext.route());
            }
        }
        if (notifyUrl != null) {
            client.asyncNotify(notifyUrl);
        }
        if (optionalArgs != null && !optionalArgs.isEmpty()) {
            client.batchOptional(optionalArgs);
        }
    }

    /**
     * 应用花呗分期客户端通用配置。
     *
     * @param client SDK 客户端
     * @param notifyUrl 通知地址
     * @param optionalArgs 可选参数
     * @param requestContext 调用上下文
     */
    private void applyShared(
            com.alipay.easysdk.payment.huabei.Client client,
            String notifyUrl,
            Map<String, Object> optionalArgs,
            AlipayRequestContext requestContext) {
        if (requestContext != null) {
            if (requestContext.appAuthToken() != null) {
                client.agent(requestContext.appAuthToken());
            }
            if (requestContext.authToken() != null) {
                client.auth(requestContext.authToken());
            }
            if (requestContext.route() != null) {
                client.route(requestContext.route());
            }
        }
        if (notifyUrl != null) {
            client.asyncNotify(notifyUrl);
        }
        if (optionalArgs != null && !optionalArgs.isEmpty()) {
            client.batchOptional(optionalArgs);
        }
    }
}
