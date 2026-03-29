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
package com.getboot.payment.infrastructure.wechatpay.operation.payscore;

import com.getboot.payment.api.properties.PaymentProperties;
import com.getboot.payment.api.wechatpay.operation.payscore.WechatPayJsapiBusinessViewRequest;
import com.getboot.payment.api.wechatpay.operation.payscore.WechatPayPayScoreDetailViewRequest;
import com.getboot.payment.api.wechatpay.operation.payscore.WechatPayPayScoreQueryRequest;
import com.getboot.payment.api.wechatpay.operation.payscore.WechatPayPayScoreService;
import com.getboot.payment.support.wechatpay.WechatPayHttpGateway;
import com.getboot.payment.support.wechatpay.WechatPayV2Support;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 微信支付分能力默认实现。
 *
 * @author qiheng
 */
public class WechatPayPayScoreServiceImpl implements WechatPayPayScoreService {

    private static final String PAY_SCORE_ORDER_PATH = "/v3/payscore/serviceorder";
    private static final String REFUND_PATH = "/v3/refund/domestic/refunds";

    private final PaymentProperties paymentProperties;
    private final WechatPayHttpGateway httpGateway;

    /**
     * 构造支付分能力服务。
     *
     * @param paymentProperties 支付配置
     * @param httpGateway       微信 HTTP 网关
     */
    public WechatPayPayScoreServiceImpl(
            PaymentProperties paymentProperties,
            WechatPayHttpGateway httpGateway) {
        this.paymentProperties = paymentProperties;
        this.httpGateway = httpGateway;
    }

    @Override
    public Map<String, Object> createOrder(Object requestBody) {
        return postForMap(PAY_SCORE_ORDER_PATH, requestBody);
    }

    @Override
    public Map<String, Object> queryOrder(WechatPayPayScoreQueryRequest request) {
        Assert.notNull(request, "request must not be null");
        Assert.hasText(request.getServiceId(), "request.serviceId must not be blank");
        Assert.hasText(request.getAppId(), "request.appId must not be blank");

        boolean hasOutOrderNo = StringUtils.hasText(request.getOutOrderNo());
        boolean hasQueryId = StringUtils.hasText(request.getQueryId());
        Assert.isTrue(hasOutOrderNo ^ hasQueryId, "Exactly one of request.outOrderNo or request.queryId is required");

        Map<String, Object> args = new LinkedHashMap<>();
        args.put("service_id", request.getServiceId());
        args.put("appid", request.getAppId());
        if (hasOutOrderNo) {
            args.put("out_order_no", request.getOutOrderNo());
        } else {
            args.put("query_id", request.getQueryId());
        }
        return getForMap(PAY_SCORE_ORDER_PATH + "?" + buildQueryString(args));
    }

    @Override
    public void cancelOrder(String outOrderNo, Object requestBody) {
        Assert.hasText(outOrderNo, "outOrderNo must not be blank");
        httpGateway.postWithoutResponse(
                PAY_SCORE_ORDER_PATH + "/" + urlEncode(outOrderNo) + "/cancel",
                requestBody
        );
    }

    @Override
    public void completeOrder(String outOrderNo, Object requestBody) {
        Assert.hasText(outOrderNo, "outOrderNo must not be blank");
        httpGateway.postWithoutResponse(
                PAY_SCORE_ORDER_PATH + "/" + urlEncode(outOrderNo) + "/complete",
                requestBody
        );
    }

    @Override
    public void modifyOrderAmount(String outOrderNo, Object requestBody) {
        Assert.hasText(outOrderNo, "outOrderNo must not be blank");
        httpGateway.postWithoutResponse(
                PAY_SCORE_ORDER_PATH + "/" + urlEncode(outOrderNo) + "/modify",
                requestBody
        );
    }

    @Override
    public void syncOrder(String outOrderNo, Object requestBody) {
        Assert.hasText(outOrderNo, "outOrderNo must not be blank");
        httpGateway.postWithoutResponse(
                PAY_SCORE_ORDER_PATH + "/" + urlEncode(outOrderNo) + "/sync",
                requestBody
        );
    }

    @Override
    public Map<String, Object> createOrderRefund(Object requestBody) {
        return postForMap(REFUND_PATH, requestBody);
    }

    @Override
    public Map<String, Object> queryOrderRefund(String outRefundNo) {
        Assert.hasText(outRefundNo, "outRefundNo must not be blank");
        return getForMap(REFUND_PATH + "/" + urlEncode(outRefundNo));
    }

    @Override
    public WechatPayJsapiBusinessViewRequest buildJsapiConfirmOrderView(String packageValue) {
        Assert.hasText(packageValue, "packageValue must not be blank");
        Map<String, Object> args = new LinkedHashMap<>();
        args.put("package", packageValue);
        return WechatPayJsapiBusinessViewRequest.builder()
                .businessType("wxpayScoreUse")
                .queryString(buildQueryString(args))
                .build();
    }

    @Override
    public WechatPayJsapiBusinessViewRequest buildJsapiOrderDetailView(WechatPayPayScoreDetailViewRequest request) {
        Assert.notNull(request, "request must not be null");
        Assert.hasText(request.getServiceId(), "request.serviceId must not be blank");
        Assert.hasText(request.getOutOrderNo(), "request.outOrderNo must not be blank");

        String merchantId = StringUtils.hasText(request.getMerchantId())
                ? request.getMerchantId()
                : paymentProperties.getWechatpay().getMerchantId();
        Assert.hasText(merchantId, "getboot.payment.wechatpay.merchant-id must not be blank");

        WechatPayV2Support support = v2Support();
        String timestamp = StringUtils.hasText(request.getTimestamp())
                ? request.getTimestamp()
                : String.valueOf(Instant.now().getEpochSecond());
        String nonceStr = StringUtils.hasText(request.getNonceStr())
                ? request.getNonceStr()
                : support.generateNonceStr();

        Map<String, Object> signArgs = new LinkedHashMap<>();
        signArgs.put("mch_id", merchantId);
        signArgs.put("service_id", request.getServiceId());
        signArgs.put("out_order_no", request.getOutOrderNo());
        signArgs.put("timestamp", timestamp);
        signArgs.put("nonce_str", nonceStr);
        signArgs.put("sign_type", WechatPayV2Support.SIGN_TYPE_HMAC_SHA256);

        String sign = support.sign(signArgs);

        Map<String, Object> queryArgs = new LinkedHashMap<>(signArgs);
        queryArgs.put("sign", sign);
        return WechatPayJsapiBusinessViewRequest.builder()
                .businessType("wxpayScoreDetail")
                .queryString(support.buildQueryString(queryArgs))
                .build();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getForMap(String path) {
        return (Map<String, Object>) httpGateway.get(path, Map.class);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> postForMap(String path, Object requestBody) {
        return (Map<String, Object>) httpGateway.post(path, requestBody, Map.class);
    }

    private String buildQueryString(Map<String, Object> args) {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, Object> entry : args.entrySet()) {
            if (entry.getValue() == null) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append('&');
            }
            builder.append(entry.getKey())
                    .append('=')
                    .append(urlEncode(String.valueOf(entry.getValue())));
        }
        return builder.toString();
    }

    private String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
    }

    private WechatPayV2Support v2Support() {
        return new WechatPayV2Support(paymentProperties.getWechatpay().getApiV2Key());
    }
}
