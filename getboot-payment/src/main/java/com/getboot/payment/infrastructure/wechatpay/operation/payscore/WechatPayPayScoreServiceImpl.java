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

    /**
     * 支付分服务订单基础路径。
     */
    private static final String PAY_SCORE_ORDER_PATH = "/v3/payscore/serviceorder";

    /**
     * 退款基础路径。
     */
    private static final String REFUND_PATH = "/v3/refund/domestic/refunds";

    /**
     * 支付配置。
     */
    private final PaymentProperties paymentProperties;

    /**
     * 微信 HTTP 网关。
     */
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

    /**
     * 创建支付分订单。
     *
     * @param requestBody 请求体
     * @return 创建结果
     */
    @Override
    public Map<String, Object> createOrder(Object requestBody) {
        return postForMap(PAY_SCORE_ORDER_PATH, requestBody);
    }

    /**
     * 查询支付分订单。
     *
     * @param request 查询请求
     * @return 查询结果
     */
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

    /**
     * 取消支付分订单。
     *
     * @param outOrderNo 商户订单号
     * @param requestBody 请求体
     */
    @Override
    public void cancelOrder(String outOrderNo, Object requestBody) {
        Assert.hasText(outOrderNo, "outOrderNo must not be blank");
        httpGateway.postWithoutResponse(
                PAY_SCORE_ORDER_PATH + "/" + urlEncode(outOrderNo) + "/cancel",
                requestBody
        );
    }

    /**
     * 完结支付分订单。
     *
     * @param outOrderNo 商户订单号
     * @param requestBody 请求体
     */
    @Override
    public void completeOrder(String outOrderNo, Object requestBody) {
        Assert.hasText(outOrderNo, "outOrderNo must not be blank");
        httpGateway.postWithoutResponse(
                PAY_SCORE_ORDER_PATH + "/" + urlEncode(outOrderNo) + "/complete",
                requestBody
        );
    }

    /**
     * 修改支付分订单金额。
     *
     * @param outOrderNo 商户订单号
     * @param requestBody 请求体
     */
    @Override
    public void modifyOrderAmount(String outOrderNo, Object requestBody) {
        Assert.hasText(outOrderNo, "outOrderNo must not be blank");
        httpGateway.postWithoutResponse(
                PAY_SCORE_ORDER_PATH + "/" + urlEncode(outOrderNo) + "/modify",
                requestBody
        );
    }

    /**
     * 同步支付分订单。
     *
     * @param outOrderNo 商户订单号
     * @param requestBody 请求体
     */
    @Override
    public void syncOrder(String outOrderNo, Object requestBody) {
        Assert.hasText(outOrderNo, "outOrderNo must not be blank");
        httpGateway.postWithoutResponse(
                PAY_SCORE_ORDER_PATH + "/" + urlEncode(outOrderNo) + "/sync",
                requestBody
        );
    }

    /**
     * 创建支付分订单退款。
     *
     * @param requestBody 请求体
     * @return 创建结果
     */
    @Override
    public Map<String, Object> createOrderRefund(Object requestBody) {
        return postForMap(REFUND_PATH, requestBody);
    }

    /**
     * 查询支付分订单退款。
     *
     * @param outRefundNo 商户退款单号
     * @return 查询结果
     */
    @Override
    public Map<String, Object> queryOrderRefund(String outRefundNo) {
        Assert.hasText(outRefundNo, "outRefundNo must not be blank");
        return getForMap(REFUND_PATH + "/" + urlEncode(outRefundNo));
    }

    /**
     * 构建 JSAPI 确认订单页面参数。
     *
     * @param packageValue 预支付包
     * @return 页面参数
     */
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

    /**
     * 构建 JSAPI 订单详情页参数。
     *
     * @param request 详情页请求
     * @return 页面参数
     */
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
    /**
     * 以 Map 形式发起 GET 请求。
     *
     * @param path 请求路径
     * @return 响应结果
     */
    private Map<String, Object> getForMap(String path) {
        return (Map<String, Object>) httpGateway.get(path, Map.class);
    }

    @SuppressWarnings("unchecked")
    /**
     * 以 Map 形式发起 POST 请求。
     *
     * @param path 请求路径
     * @param requestBody 请求体
     * @return 响应结果
     */
    private Map<String, Object> postForMap(String path, Object requestBody) {
        return (Map<String, Object>) httpGateway.post(path, requestBody, Map.class);
    }

    /**
     * 构建查询字符串。
     *
     * @param args 查询参数
     * @return 查询字符串
     */
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

    /**
     * 对参数执行 URL 编码。
     *
     * @param value 原始值
     * @return 编码后的值
     */
    private String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
    }

    /**
     * 创建 V2 签名支持工具。
     *
     * @return V2 签名支持工具
     */
    private WechatPayV2Support v2Support() {
        return new WechatPayV2Support(paymentProperties.getWechatpay().getApiV2Key());
    }
}
