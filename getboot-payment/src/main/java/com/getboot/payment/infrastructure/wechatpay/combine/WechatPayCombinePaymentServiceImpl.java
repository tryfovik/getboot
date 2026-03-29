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
package com.getboot.payment.infrastructure.wechatpay.combine;

import com.getboot.exception.api.exception.BusinessException;
import com.getboot.payment.api.model.PaymentMode;
import com.getboot.payment.api.properties.PaymentProperties;
import com.getboot.payment.api.wechatpay.combine.WechatPayCombineCloseRequest;
import com.getboot.payment.api.wechatpay.combine.WechatPayCombineCloseResponse;
import com.getboot.payment.api.wechatpay.combine.WechatPayCombineCreateRequest;
import com.getboot.payment.api.wechatpay.combine.WechatPayCombineCreateResponse;
import com.getboot.payment.api.wechatpay.combine.WechatPayCombineNotifyRequest;
import com.getboot.payment.api.wechatpay.combine.WechatPayCombineNotifyResponse;
import com.getboot.payment.api.wechatpay.combine.WechatPayCombineOrderRequest;
import com.getboot.payment.api.wechatpay.combine.WechatPayCombineOrderResponse;
import com.getboot.payment.api.wechatpay.combine.WechatPayCombinePaymentService;
import com.getboot.payment.infrastructure.wechatpay.WechatPayAmounts;
import com.getboot.payment.spi.wechatpay.WechatPayRequestCustomizer;
import com.getboot.payment.spi.wechatpay.WechatPayRequestOptions;
import com.getboot.payment.support.PaymentInvoker;
import com.getboot.payment.support.wechatpay.WechatPayHttpGateway;
import com.getboot.payment.support.wechatpay.WechatPayRequestSupport;
import com.getboot.payment.support.wechatpay.WechatPayResponseSupport;
import com.wechat.pay.java.core.Config;
import com.wechat.pay.java.core.notification.NotificationParser;
import com.wechat.pay.java.core.notification.RequestParam;
import com.wechat.pay.java.core.util.NonceUtil;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 微信合单支付默认实现。
 *
 * @author qiheng
 */
public class WechatPayCombinePaymentServiceImpl implements WechatPayCombinePaymentService {

    private static final Set<PaymentMode> SUPPORTED_MODES = Set.copyOf(EnumSet.of(
            PaymentMode.JSAPI,
            PaymentMode.MINI_PROGRAM,
            PaymentMode.APP,
            PaymentMode.H5,
            PaymentMode.NATIVE
    ));

    private static final String HEADER_SERIAL = "Wechatpay-Serial";
    private static final String HEADER_TIMESTAMP = "Wechatpay-Timestamp";
    private static final String HEADER_NONCE = "Wechatpay-Nonce";
    private static final String HEADER_SIGNATURE = "Wechatpay-Signature";
    private static final String HEADER_SIGN_TYPE = "Wechatpay-Signtype";

    private final PaymentProperties.WechatPay properties;
    private final NotificationParser notificationParser;
    private final Config config;
    private final WechatPayHttpGateway httpGateway;
    private final List<WechatPayRequestCustomizer> requestCustomizers;

    /**
     * 构造微信合单支付服务。
     *
     * @param paymentProperties  支付配置
     * @param notificationParser 通知解析器
     * @param config             微信官方配置
     * @param httpGateway        微信 HTTP 网关
     */
    public WechatPayCombinePaymentServiceImpl(
            PaymentProperties paymentProperties,
            NotificationParser notificationParser,
            Config config,
            WechatPayHttpGateway httpGateway) {
        this(paymentProperties, notificationParser, config, httpGateway, List.of());
    }

    /**
     * 构造微信合单支付服务。
     *
     * @param paymentProperties  支付配置
     * @param notificationParser 通知解析器
     * @param config             微信官方配置
     * @param httpGateway        微信 HTTP 网关
     * @param requestCustomizers 请求扩展器
     */
    public WechatPayCombinePaymentServiceImpl(
            PaymentProperties paymentProperties,
            NotificationParser notificationParser,
            Config config,
            WechatPayHttpGateway httpGateway,
            List<WechatPayRequestCustomizer> requestCustomizers) {
        this.properties = paymentProperties.getWechatpay();
        this.notificationParser = notificationParser;
        this.config = config;
        this.httpGateway = httpGateway;
        this.requestCustomizers = requestCustomizers == null ? List.of() : List.copyOf(requestCustomizers);
    }

    @Override
    public Set<PaymentMode> supportedModes() {
        return SUPPORTED_MODES;
    }

    @Override
    public WechatPayCombineCreateResponse create(WechatPayCombineCreateRequest request) {
        PaymentMode mode = requireSupportedMode(request.getMode());
        WechatPayRequestOptions options = customizeCombineCreate(request);
        validateCreateRequest(request, mode, options);

        CombineCreateResult response = PaymentInvoker.invoke(
                () -> httpGateway.post(resolveCreatePath(mode), buildCreateBody(request, options), CombineCreateResult.class),
                "Failed to create WeChat Pay combine order"
        );

        return switch (mode) {
            case JSAPI, MINI_PROGRAM -> buildJsapiCreateResponse(request, response, options);
            case APP -> buildAppCreateResponse(request, response, options);
            case H5 -> buildH5CreateResponse(request, response, options);
            case NATIVE -> buildNativeCreateResponse(request, response, options);
            default -> throw new BusinessException("Unsupported WeChat combine mode: " + mode);
        };
    }

    @Override
    public WechatPayCombineOrderResponse queryOrder(WechatPayCombineOrderRequest request) {
        requireText(request.getCombineMerchantOrderNo(), "combineMerchantOrderNo must not be blank");
        customizeCombineQueryOrder(request);
        String path = "/v3/combine-transactions/out-trade-no/"
                + encode(request.getCombineMerchantOrderNo())
                + "?combine_mchid="
                + encode(properties.getMerchantId());
        CombineQueryResult response = PaymentInvoker.invoke(
                () -> httpGateway.get(path, CombineQueryResult.class),
                "Failed to query WeChat Pay combine order"
        );
        return mapQueryResponse(response);
    }

    @Override
    public WechatPayCombineCloseResponse close(WechatPayCombineCloseRequest request) {
        requireText(request.getCombineMerchantOrderNo(), "combineMerchantOrderNo must not be blank");
        if (request.getSubOrders() == null || request.getSubOrders().isEmpty()) {
            throw new BusinessException("subOrders must not be empty");
        }
        WechatPayRequestOptions options = customizeCombineClose(request);
        String path = "/v3/combine-transactions/out-trade-no/"
                + encode(request.getCombineMerchantOrderNo())
                + "/close";
        PaymentInvoker.invokeVoid(
                () -> httpGateway.postWithoutResponse(path, buildCloseBody(request, options)),
                "Failed to close WeChat Pay combine order"
        );
        return WechatPayCombineCloseResponse.builder()
                .combineMerchantOrderNo(request.getCombineMerchantOrderNo())
                .closed(true)
                .build();
    }

    @Override
    public WechatPayCombineNotifyResponse parseNotify(WechatPayCombineNotifyRequest request) {
        requireText(request.getBody(), "body must not be blank");
        RequestParam requestParam = buildRequestParam(request.getHeaders(), request.getBody());
        CombineNotifyResult response = PaymentInvoker.invoke(
                () -> notificationParser.parse(requestParam, CombineNotifyResult.class),
                "Failed to parse WeChat Pay combine notification"
        );
        return mapNotifyResponse(response);
    }

    private String resolveCreatePath(PaymentMode mode) {
        return switch (mode) {
            case JSAPI, MINI_PROGRAM -> "/v3/combine-transactions/jsapi";
            case APP -> "/v3/combine-transactions/app";
            case H5 -> "/v3/combine-transactions/h5";
            case NATIVE -> "/v3/combine-transactions/native";
            default -> throw new BusinessException("Unsupported WeChat combine mode: " + mode);
        };
    }

    private Map<String, Object> buildCreateBody(
            WechatPayCombineCreateRequest request,
            WechatPayRequestOptions options) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("combine_appid", resolveAppId(request.getAppId(), options));
        body.put("combine_mchid", properties.getMerchantId());
        body.put("combine_out_trade_no", request.getCombineMerchantOrderNo());
        body.put("time_expire", request.getTimeExpire());
        body.put("notify_url", resolveNotifyUrl(request.getNotifyUrl(), options));
        body.put("sub_orders", buildSubOrders(request));
        if (PaymentMode.H5.equals(request.getMode())) {
            body.put("scene_info", buildH5SceneInfo(request, options));
        }
        if (StringUtils.hasText(resolvePayerId(request, options))) {
            Map<String, Object> payerInfo = new LinkedHashMap<>();
            payerInfo.put("openid", resolvePayerId(request, options));
            body.put("combine_payer_info", payerInfo);
        }
        body.putAll(options.getExtraBody());
        return body;
    }

    private List<Map<String, Object>> buildSubOrders(WechatPayCombineCreateRequest request) {
        List<Map<String, Object>> subOrders = new ArrayList<>();
        for (WechatPayCombineCreateRequest.SubOrder subOrder : request.getSubOrders()) {
            requireText(subOrder.getMerchantOrderNo(), "subOrder.merchantOrderNo must not be blank");
            requireAmount(subOrder.getAmount(), "subOrder.amount must not be null");
            String description = resolveText(subOrder.getDescription(), resolveDescription(request));
            requireText(description, "subOrder.description and request description must not both be blank");

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("mchid", resolveText(subOrder.getMerchantId(), properties.getMerchantId()));
            item.put("out_trade_no", subOrder.getMerchantOrderNo());
            item.put("description", description);
            item.put("attach", WechatPayRequestSupport.text(subOrder.getMetadata(), "attach"));
            item.put("goods_tag", WechatPayRequestSupport.text(subOrder.getMetadata(), "goodsTag"));

            Map<String, Object> amount = new LinkedHashMap<>();
            amount.put("total_amount", WechatPayAmounts.toFenLong(subOrder.getAmount()));
            amount.put("currency", resolveText(subOrder.getCurrency(), "CNY"));
            item.put("amount", amount);
            subOrders.add(item);
        }
        return subOrders;
    }

    private Map<String, Object> buildH5SceneInfo(
            WechatPayCombineCreateRequest request,
            WechatPayRequestOptions options) {
        requireText(resolveClientIp(request, options), "clientIp must not be blank for WeChat combine H5");
        Map<String, Object> sceneInfo = new LinkedHashMap<>();
        sceneInfo.put("payer_client_ip", resolveClientIp(request, options));

        Map<String, Object> h5Info = new LinkedHashMap<>();
        h5Info.put("type", resolveText(WechatPayRequestSupport.text(request.getMetadata(), "h5Type"), "Wap"));
        h5Info.put("app_name", WechatPayRequestSupport.text(request.getMetadata(), "appName"));
        h5Info.put("app_url", WechatPayRequestSupport.text(request.getMetadata(), "appUrl"));
        h5Info.put("bundle_id", WechatPayRequestSupport.text(request.getMetadata(), "bundleId"));
        h5Info.put("package_name", WechatPayRequestSupport.text(request.getMetadata(), "packageName"));
        sceneInfo.put("h5_info", h5Info);
        return sceneInfo;
    }

    private Map<String, Object> buildCloseBody(
            WechatPayCombineCloseRequest request,
            WechatPayRequestOptions options) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("combine_appid", resolveAppId(request.getAppId(), options));
        List<Map<String, Object>> subOrders = new ArrayList<>();
        for (WechatPayCombineCloseRequest.SubOrder subOrder : request.getSubOrders()) {
            requireText(subOrder.getMerchantOrderNo(), "subOrder.merchantOrderNo must not be blank");
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("mchid", resolveText(subOrder.getMerchantId(), properties.getMerchantId()));
            item.put("out_trade_no", subOrder.getMerchantOrderNo());
            subOrders.add(item);
        }
        body.put("sub_orders", subOrders);
        body.putAll(options.getExtraBody());
        return body;
    }

    private WechatPayCombineCreateResponse buildJsapiCreateResponse(
            WechatPayCombineCreateRequest request,
            CombineCreateResult response,
            WechatPayRequestOptions options) {
        String appId = resolveAppId(request.getAppId(), options);
        String timeStamp = String.valueOf(Instant.now().getEpochSecond());
        String nonceStr = NonceUtil.createNonce(32);
        String packageValue = "prepay_id=" + response.prepay_id;
        String message = appId + "\n" + timeStamp + "\n" + nonceStr + "\n" + packageValue + "\n";
        String paySign = config.createSigner().sign(message).getSign();

        Map<String, String> paymentData = new LinkedHashMap<>();
        paymentData.put("appId", appId);
        paymentData.put("timeStamp", timeStamp);
        paymentData.put("nonceStr", nonceStr);
        paymentData.put("package", packageValue);
        paymentData.put("signType", "RSA");
        paymentData.put("paySign", paySign);

        return WechatPayCombineCreateResponse.builder()
                .mode(request.getMode())
                .combineMerchantOrderNo(request.getCombineMerchantOrderNo())
                .prepayId(response.prepay_id)
                .paymentData(paymentData)
                .metadata(WechatPayResponseSupport.buildCombineBaseMetadata(
                        resolveAppId(request.getAppId(), options),
                        properties.getMerchantId(),
                        request.getSubOrders().size()
                ))
                .build();
    }

    private WechatPayCombineCreateResponse buildAppCreateResponse(
            WechatPayCombineCreateRequest request,
            CombineCreateResult response,
            WechatPayRequestOptions options) {
        String appId = resolveAppId(request.getAppId(), options);
        String timeStamp = String.valueOf(Instant.now().getEpochSecond());
        String nonceStr = NonceUtil.createNonce(32);
        String message = appId + "\n" + timeStamp + "\n" + nonceStr + "\n" + response.prepay_id + "\n";
        String paySign = config.createSigner().sign(message).getSign();

        Map<String, String> paymentData = new LinkedHashMap<>();
        paymentData.put("appId", appId);
        paymentData.put("partnerId", properties.getMerchantId());
        paymentData.put("prepayId", response.prepay_id);
        paymentData.put("package", "Sign=WXPay");
        paymentData.put("nonceStr", nonceStr);
        paymentData.put("timestamp", timeStamp);
        paymentData.put("sign", paySign);

        return WechatPayCombineCreateResponse.builder()
                .mode(request.getMode())
                .combineMerchantOrderNo(request.getCombineMerchantOrderNo())
                .prepayId(response.prepay_id)
                .paymentData(paymentData)
                .metadata(WechatPayResponseSupport.buildCombineBaseMetadata(
                        resolveAppId(request.getAppId(), options),
                        properties.getMerchantId(),
                        request.getSubOrders().size()
                ))
                .build();
    }

    private WechatPayCombineCreateResponse buildH5CreateResponse(
            WechatPayCombineCreateRequest request,
            CombineCreateResult response,
            WechatPayRequestOptions options) {
        return WechatPayCombineCreateResponse.builder()
                .mode(request.getMode())
                .combineMerchantOrderNo(request.getCombineMerchantOrderNo())
                .prepayId(response.prepay_id)
                .payUrl(response.h5_url)
                .metadata(WechatPayResponseSupport.buildCombineBaseMetadata(
                        resolveAppId(request.getAppId(), options),
                        properties.getMerchantId(),
                        request.getSubOrders().size()
                ))
                .build();
    }

    private WechatPayCombineCreateResponse buildNativeCreateResponse(
            WechatPayCombineCreateRequest request,
            CombineCreateResult response,
            WechatPayRequestOptions options) {
        return WechatPayCombineCreateResponse.builder()
                .mode(request.getMode())
                .combineMerchantOrderNo(request.getCombineMerchantOrderNo())
                .payUrl(response.code_url)
                .qrCodeContent(response.code_url)
                .metadata(WechatPayResponseSupport.buildCombineBaseMetadata(
                        resolveAppId(request.getAppId(), options),
                        properties.getMerchantId(),
                        request.getSubOrders().size()
                ))
                .build();
    }

    private WechatPayCombineOrderResponse mapQueryResponse(CombineQueryResult response) {
        String state = WechatPayResponseSupport.firstNonBlank(
                response.combine_state,
                response.combine_trade_state,
                response.state
        );
        Map<String, String> metadata = WechatPayResponseSupport.buildCombineMetadata(
                state,
                response.transaction_id,
                response.amount == null ? null : response.amount.settlement_rate
        );

        List<WechatPayCombineOrderResponse.SubOrder> subOrders = new ArrayList<>();
        if (response.sub_orders != null) {
            for (CombineSubOrder item : response.sub_orders) {
                Map<String, String> subMetadata = WechatPayResponseSupport.buildCombineSubOrderMetadata(
                        item.attach,
                        item.bank_type,
                        item.amount == null ? null : item.amount.settlement_rate
                );
                subOrders.add(WechatPayCombineOrderResponse.SubOrder.builder()
                        .merchantId(item.mchid)
                        .merchantOrderNo(item.out_trade_no)
                        .transactionId(item.transaction_id)
                        .tradeType(item.trade_type)
                        .tradeState(WechatPayResponseSupport.firstNonBlank(item.trade_state, item.state))
                        .totalAmount(WechatPayResponseSupport.amountFromFen(item.amount == null ? null : item.amount.total_amount))
                        .payerAmount(WechatPayResponseSupport.amountFromFen(item.amount == null ? null : item.amount.payer_amount))
                        .currency(item.amount == null
                                ? null
                                : WechatPayResponseSupport.firstNonBlank(item.amount.currency, item.amount.payer_currency))
                        .successTime(item.success_time)
                        .metadata(subMetadata)
                        .build());
            }
        }

        return WechatPayCombineOrderResponse.builder()
                .combineAppId(response.combine_appid)
                .combineMerchantId(response.combine_mchid)
                .combineMerchantOrderNo(response.combine_out_trade_no)
                .status(state)
                .payerId(response.combine_payer_info == null ? null : response.combine_payer_info.openid)
                .transactionId(response.transaction_id)
                .successTime(response.success_time)
                .subOrders(subOrders)
                .metadata(metadata)
                .build();
    }

    private WechatPayCombineNotifyResponse mapNotifyResponse(CombineNotifyResult response) {
        List<WechatPayCombineOrderResponse.SubOrder> subOrders = new ArrayList<>();
        if (response.sub_orders != null) {
            for (CombineSubOrder item : response.sub_orders) {
                Map<String, String> subMetadata = WechatPayResponseSupport.buildCombineSubOrderMetadata(
                        item.attach,
                        item.bank_type,
                        item.amount == null ? null : item.amount.settlement_rate
                );
                subOrders.add(WechatPayCombineOrderResponse.SubOrder.builder()
                        .merchantId(item.mchid)
                        .merchantOrderNo(item.out_trade_no)
                        .transactionId(item.transaction_id)
                        .tradeType(item.trade_type)
                        .tradeState(WechatPayResponseSupport.firstNonBlank(item.trade_state, item.state))
                        .totalAmount(WechatPayResponseSupport.amountFromFen(item.amount == null ? null : item.amount.total_amount))
                        .payerAmount(WechatPayResponseSupport.amountFromFen(item.amount == null ? null : item.amount.payer_amount))
                        .currency(item.amount == null
                                ? null
                                : WechatPayResponseSupport.firstNonBlank(item.amount.currency, item.amount.payer_currency))
                        .successTime(item.success_time)
                        .metadata(subMetadata)
                        .build());
            }
        }

        String state = WechatPayResponseSupport.firstNonBlank(response.combine_state, response.state);
        Map<String, String> metadata = WechatPayResponseSupport.buildCombineMetadata(
                state,
                response.transaction_id,
                response.amount == null ? null : response.amount.settlement_rate
        );

        return WechatPayCombineNotifyResponse.builder()
                .combineAppId(response.combine_appid)
                .combineMerchantId(response.combine_mchid)
                .combineMerchantOrderNo(response.combine_out_trade_no)
                .transactionId(response.transaction_id)
                .success("SUCCESS".equalsIgnoreCase(state))
                .successTime(response.success_time)
                .payerId(response.combine_payer_info == null ? null : response.combine_payer_info.openid)
                .subOrders(subOrders)
                .metadata(metadata)
                .build();
    }

    private RequestParam buildRequestParam(Map<String, String> headers, String body) {
        String serialNumber = WechatPayRequestSupport.requiredHeader(headers, HEADER_SERIAL);
        String timestamp = WechatPayRequestSupport.requiredHeader(headers, HEADER_TIMESTAMP);
        String nonce = WechatPayRequestSupport.requiredHeader(headers, HEADER_NONCE);
        String signature = WechatPayRequestSupport.requiredHeader(headers, HEADER_SIGNATURE);
        RequestParam.Builder builder = new RequestParam.Builder()
                .serialNumber(serialNumber)
                .timestamp(timestamp)
                .nonce(nonce)
                .signature(signature)
                .body(body);
        String signType = WechatPayRequestSupport.header(headers, HEADER_SIGN_TYPE);
        if (StringUtils.hasText(signType)) {
            builder.signType(signType);
        }
        return builder.build();
    }

    private void validateCreateRequest(
            WechatPayCombineCreateRequest request,
            PaymentMode mode,
            WechatPayRequestOptions options) {
        requireText(request.getCombineMerchantOrderNo(), "combineMerchantOrderNo must not be blank");
        if (request.getSubOrders() == null || request.getSubOrders().size() < 2) {
            throw new BusinessException("subOrders must contain at least 2 items");
        }
        if (PaymentMode.H5.equals(mode)) {
            requireText(resolveClientIp(request, options), "clientIp must not be blank for WeChat combine H5");
        }
        if (EnumSet.of(PaymentMode.JSAPI, PaymentMode.MINI_PROGRAM).contains(mode)) {
            requireText(
                    resolvePayerId(request, options),
                    "payerId must not be blank for WeChat combine JSAPI/Mini Program"
            );
        }
    }

    private PaymentMode requireSupportedMode(PaymentMode mode) {
        if (mode == null || !SUPPORTED_MODES.contains(mode)) {
            throw new BusinessException("Unsupported WeChat combine mode: " + mode);
        }
        return mode;
    }

    private void requireText(String value, String message) {
        if (!StringUtils.hasText(value)) {
            throw new BusinessException(message);
        }
    }

    private void requireAmount(BigDecimal value, String message) {
        if (value == null) {
            throw new BusinessException(message);
        }
    }

    private String resolveAppId(String preferred, WechatPayRequestOptions options) {
        return resolveText(options.getAppId(), resolveText(preferred, properties.getAppId()));
    }

    private String resolveDescription(WechatPayCombineCreateRequest request) {
        return resolveText(request.getDescription(), request.getSubject());
    }

    private String resolveNotifyUrl(String preferred, WechatPayRequestOptions options) {
        return resolveText(options.getNotifyUrl(), resolveText(preferred, properties.getNotifyUrl()));
    }

    private String resolvePayerId(WechatPayCombineCreateRequest request, WechatPayRequestOptions options) {
        return resolveText(options.getPayerId(), request.getPayerId());
    }

    private String resolveClientIp(WechatPayCombineCreateRequest request, WechatPayRequestOptions options) {
        return resolveText(options.getClientIp(), request.getClientIp());
    }

    private String resolveText(String preferred, String fallback) {
        return StringUtils.hasText(preferred) ? preferred : fallback;
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private WechatPayRequestOptions customizeCombineCreate(WechatPayCombineCreateRequest request) {
        WechatPayRequestOptions options = new WechatPayRequestOptions();
        for (WechatPayRequestCustomizer requestCustomizer : requestCustomizers) {
            requestCustomizer.customizeCombineCreate(request, options);
        }
        return options;
    }

    private void customizeCombineQueryOrder(WechatPayCombineOrderRequest request) {
        WechatPayRequestOptions options = new WechatPayRequestOptions();
        for (WechatPayRequestCustomizer requestCustomizer : requestCustomizers) {
            requestCustomizer.customizeCombineQueryOrder(request, options);
        }
    }

    private WechatPayRequestOptions customizeCombineClose(WechatPayCombineCloseRequest request) {
        WechatPayRequestOptions options = new WechatPayRequestOptions();
        for (WechatPayRequestCustomizer requestCustomizer : requestCustomizers) {
            requestCustomizer.customizeCombineClose(request, options);
        }
        return options;
    }

    private static final class CombineCreateResult {
        private String prepay_id;
        private String h5_url;
        private String code_url;
    }

    private static final class CombineQueryResult {
        private String combine_appid;
        private String combine_mchid;
        private String combine_out_trade_no;
        private String combine_state;
        private String combine_trade_state;
        private String state;
        private String success_time;
        private String transaction_id;
        private CombinePayerInfo combine_payer_info;
        private CombineAmount amount;
        private List<CombineSubOrder> sub_orders;
    }

    private static final class CombineNotifyResult {
        private String combine_appid;
        private String combine_mchid;
        private String combine_out_trade_no;
        private String combine_state;
        private String state;
        private String success_time;
        private String transaction_id;
        private CombinePayerInfo combine_payer_info;
        private CombineAmount amount;
        private List<CombineSubOrder> sub_orders;
    }

    private static final class CombinePayerInfo {
        private String openid;
    }

    private static final class CombineSubOrder {
        private String mchid;
        private String out_trade_no;
        private String transaction_id;
        private String trade_type;
        private String trade_state;
        private String state;
        private String attach;
        private String bank_type;
        private String success_time;
        private CombineAmount amount;
    }

    private static final class CombineAmount {
        private Long total_amount;
        private String currency;
        private Long payer_amount;
        private String payer_currency;
        private Long settlement_rate;
    }

}
