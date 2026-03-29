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

    @Override
    public AlipayTradeQueryResponse query(
            String outTradeNo,
            Map<String, Object> optionalArgs,
            AlipayRequestContext requestContext) throws Exception {
        com.alipay.easysdk.payment.common.Client client =
                configure(new com.alipay.easysdk.payment.common.Client(newKernelClient()), null, optionalArgs, requestContext);
        return client.query(outTradeNo);
    }

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

    @Override
    public AlipayTradeCloseResponse close(
            String outTradeNo,
            Map<String, Object> optionalArgs,
            AlipayRequestContext requestContext) throws Exception {
        com.alipay.easysdk.payment.common.Client client =
                configure(new com.alipay.easysdk.payment.common.Client(newKernelClient()), null, optionalArgs, requestContext);
        return client.close(outTradeNo);
    }

    @Override
    public AlipayTradeCancelResponse cancel(
            String outTradeNo,
            Map<String, Object> optionalArgs,
            AlipayRequestContext requestContext) throws Exception {
        com.alipay.easysdk.payment.common.Client client =
                configure(new com.alipay.easysdk.payment.common.Client(newKernelClient()), null, optionalArgs, requestContext);
        return client.cancel(outTradeNo);
    }

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

    @Override
    public boolean verifyNotify(Map<String, String> parameters) throws Exception {
        return new com.alipay.easysdk.payment.common.Client(newKernelClient()).verifyNotify(parameters);
    }

    @Override
    public AlipayOpenApiGenericResponse execute(
            String method,
            Map<String, String> textParams,
            Map<String, Object> bizParams) throws Exception {
        return execute(method, textParams, bizParams, null);
    }

    @Override
    public AlipayOpenApiGenericResponse execute(
            String method,
            Map<String, String> textParams,
            Map<String, Object> bizParams,
            AlipayRequestContext requestContext) throws Exception {
        return configure(new com.alipay.easysdk.util.generic.Client(newKernelClient()), requestContext)
                .execute(method, textParams, bizParams);
    }

    private Client newKernelClient() {
        return new Client(context);
    }

    private com.alipay.easysdk.payment.app.Client configure(
            com.alipay.easysdk.payment.app.Client client,
            String notifyUrl,
            Map<String, Object> optionalArgs,
            AlipayRequestContext requestContext) {
        applyShared(client, notifyUrl, optionalArgs, requestContext);
        return client;
    }

    private com.alipay.easysdk.payment.page.Client configure(
            com.alipay.easysdk.payment.page.Client client,
            String notifyUrl,
            Map<String, Object> optionalArgs,
            AlipayRequestContext requestContext) {
        applyShared(client, notifyUrl, optionalArgs, requestContext);
        return client;
    }

    private com.alipay.easysdk.payment.wap.Client configure(
            com.alipay.easysdk.payment.wap.Client client,
            String notifyUrl,
            Map<String, Object> optionalArgs,
            AlipayRequestContext requestContext) {
        applyShared(client, notifyUrl, optionalArgs, requestContext);
        return client;
    }

    private com.alipay.easysdk.payment.facetoface.Client configure(
            com.alipay.easysdk.payment.facetoface.Client client,
            String notifyUrl,
            Map<String, Object> optionalArgs,
            AlipayRequestContext requestContext) {
        applyShared(client, notifyUrl, optionalArgs, requestContext);
        return client;
    }

    private com.alipay.easysdk.payment.common.Client configure(
            com.alipay.easysdk.payment.common.Client client,
            String notifyUrl,
            Map<String, Object> optionalArgs,
            AlipayRequestContext requestContext) {
        applyShared(client, notifyUrl, optionalArgs, requestContext);
        return client;
    }

    private com.alipay.easysdk.payment.huabei.Client configure(
            com.alipay.easysdk.payment.huabei.Client client,
            String notifyUrl,
            Map<String, Object> optionalArgs,
            AlipayRequestContext requestContext) {
        applyShared(client, notifyUrl, optionalArgs, requestContext);
        return client;
    }

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
