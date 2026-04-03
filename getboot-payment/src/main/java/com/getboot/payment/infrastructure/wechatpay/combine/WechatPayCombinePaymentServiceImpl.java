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

    /**
     * 支持的合单支付模式集合。
     */
    private static final Set<PaymentMode> SUPPORTED_MODES = Set.copyOf(EnumSet.of(
            PaymentMode.JSAPI,
            PaymentMode.MINI_PROGRAM,
            PaymentMode.APP,
            PaymentMode.H5,
            PaymentMode.NATIVE
    ));

    /**
     * 通知证书序列号请求头。
     */
    private static final String HEADER_SERIAL = "Wechatpay-Serial";

    /**
     * 通知时间戳请求头。
     */
    private static final String HEADER_TIMESTAMP = "Wechatpay-Timestamp";

    /**
     * 通知随机串请求头。
     */
    private static final String HEADER_NONCE = "Wechatpay-Nonce";

    /**
     * 通知签名请求头。
     */
    private static final String HEADER_SIGNATURE = "Wechatpay-Signature";

    /**
     * 通知签名算法请求头。
     */
    private static final String HEADER_SIGN_TYPE = "Wechatpay-Signtype";

    /**
     * 微信支付配置。
     */
    private final PaymentProperties.WechatPay properties;

    /**
     * 微信通知解析器。
     */
    private final NotificationParser notificationParser;

    /**
     * 微信官方配置对象。
     */
    private final Config config;

    /**
     * 微信支付 HTTP 网关。
     */
    private final WechatPayHttpGateway httpGateway;

    /**
     * 请求扩展器列表。
     */
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

    /**
     * 返回当前实现支持的支付模式。
     *
     * @return 支持的支付模式集合
     */
    @Override
    public Set<PaymentMode> supportedModes() {
        return SUPPORTED_MODES;
    }

    /**
     * 创建微信合单支付订单。
     *
     * @param request 合单创建请求
     * @return 合单创建结果
     */
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

    /**
     * 查询微信合单支付订单。
     *
     * @param request 合单查询请求
     * @return 合单查询结果
     */
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

    /**
     * 关闭微信合单支付订单。
     *
     * @param request 合单关单请求
     * @return 合单关单结果
     */
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

    /**
     * 解析微信合单支付通知。
     *
     * @param request 合单通知请求
     * @return 合单通知结果
     */
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

    /**
     * 根据支付模式解析下单路径。
     *
     * @param mode 支付模式
     * @return 下单接口路径
     */
    private String resolveCreatePath(PaymentMode mode) {
        return switch (mode) {
            case JSAPI, MINI_PROGRAM -> "/v3/combine-transactions/jsapi";
            case APP -> "/v3/combine-transactions/app";
            case H5 -> "/v3/combine-transactions/h5";
            case NATIVE -> "/v3/combine-transactions/native";
            default -> throw new BusinessException("Unsupported WeChat combine mode: " + mode);
        };
    }

    /**
     * 构建合单下单请求体。
     *
     * @param request 合单下单请求
     * @param options 请求扩展参数
     * @return 下单请求体
     */
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

    /**
     * 构建子订单列表。
     *
     * @param request 合单下单请求
     * @return 子订单请求体列表
     */
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

    /**
     * 构建 H5 合单支付场景信息。
     *
     * @param request 合单下单请求
     * @param options 请求扩展参数
     * @return H5 场景信息
     */
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

    /**
     * 构建合单关单请求体。
     *
     * @param request 合单关单请求
     * @param options 请求扩展参数
     * @return 关单请求体
     */
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

    /**
     * 构建 JSAPI 或小程序模式的下单响应。
     *
     * @param request 合单下单请求
     * @param response 微信原始响应
     * @param options 请求扩展参数
     * @return 统一后的创建响应
     */
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

    /**
     * 构建 APP 模式的下单响应。
     *
     * @param request 合单下单请求
     * @param response 微信原始响应
     * @param options 请求扩展参数
     * @return 统一后的创建响应
     */
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

    /**
     * 构建 H5 模式的下单响应。
     *
     * @param request 合单下单请求
     * @param response 微信原始响应
     * @param options 请求扩展参数
     * @return 统一后的创建响应
     */
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

    /**
     * 构建 Native 模式的下单响应。
     *
     * @param request 合单下单请求
     * @param response 微信原始响应
     * @param options 请求扩展参数
     * @return 统一后的创建响应
     */
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

    /**
     * 将微信查询响应映射为统一响应对象。
     *
     * @param response 微信查询响应
     * @return 统一查询响应
     */
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

    /**
     * 将微信通知响应映射为统一通知对象。
     *
     * @param response 微信通知响应
     * @return 统一通知响应
     */
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

    /**
     * 构建微信通知解析参数。
     *
     * @param headers 通知请求头
     * @param body 通知请求体
     * @return 微信 SDK 请求参数
     */
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

    /**
     * 校验合单下单请求。
     *
     * @param request 合单下单请求
     * @param mode 支付模式
     * @param options 请求扩展参数
     */
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

    /**
     * 校验并返回支持的支付模式。
     *
     * @param mode 支付模式
     * @return 已校验的支付模式
     */
    private PaymentMode requireSupportedMode(PaymentMode mode) {
        if (mode == null || !SUPPORTED_MODES.contains(mode)) {
            throw new BusinessException("Unsupported WeChat combine mode: " + mode);
        }
        return mode;
    }

    /**
     * 校验文本参数非空。
     *
     * @param value 文本值
     * @param message 失败提示
     */
    private void requireText(String value, String message) {
        if (!StringUtils.hasText(value)) {
            throw new BusinessException(message);
        }
    }

    /**
     * 校验金额参数非空。
     *
     * @param value 金额
     * @param message 失败提示
     */
    private void requireAmount(BigDecimal value, String message) {
        if (value == null) {
            throw new BusinessException(message);
        }
    }

    /**
     * 解析有效的应用 ID。
     *
     * @param preferred 请求优先值
     * @param options 请求扩展参数
     * @return 最终应用 ID
     */
    private String resolveAppId(String preferred, WechatPayRequestOptions options) {
        return resolveText(options.getAppId(), resolveText(preferred, properties.getAppId()));
    }

    /**
     * 解析有效的订单描述。
     *
     * @param request 合单下单请求
     * @return 最终订单描述
     */
    private String resolveDescription(WechatPayCombineCreateRequest request) {
        return resolveText(request.getDescription(), request.getSubject());
    }

    /**
     * 解析有效的通知地址。
     *
     * @param preferred 请求优先值
     * @param options 请求扩展参数
     * @return 最终通知地址
     */
    private String resolveNotifyUrl(String preferred, WechatPayRequestOptions options) {
        return resolveText(options.getNotifyUrl(), resolveText(preferred, properties.getNotifyUrl()));
    }

    /**
     * 解析有效的付款人标识。
     *
     * @param request 合单下单请求
     * @param options 请求扩展参数
     * @return 最终付款人标识
     */
    private String resolvePayerId(WechatPayCombineCreateRequest request, WechatPayRequestOptions options) {
        return resolveText(options.getPayerId(), request.getPayerId());
    }

    /**
     * 解析有效的客户端 IP。
     *
     * @param request 合单下单请求
     * @param options 请求扩展参数
     * @return 最终客户端 IP
     */
    private String resolveClientIp(WechatPayCombineCreateRequest request, WechatPayRequestOptions options) {
        return resolveText(options.getClientIp(), request.getClientIp());
    }

    /**
     * 返回优先值或回退值。
     *
     * @param preferred 优先值
     * @param fallback 回退值
     * @return 最终值
     */
    private String resolveText(String preferred, String fallback) {
        return StringUtils.hasText(preferred) ? preferred : fallback;
    }

    /**
     * 对路径参数执行 URL 编码。
     *
     * @param value 原始值
     * @return 编码后的值
     */
    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    /**
     * 应用合单下单请求扩展器。
     *
     * @param request 合单下单请求
     * @return 扩展后的请求参数
     */
    private WechatPayRequestOptions customizeCombineCreate(WechatPayCombineCreateRequest request) {
        WechatPayRequestOptions options = new WechatPayRequestOptions();
        for (WechatPayRequestCustomizer requestCustomizer : requestCustomizers) {
            requestCustomizer.customizeCombineCreate(request, options);
        }
        return options;
    }

    /**
     * 应用合单查单请求扩展器。
     *
     * @param request 合单查单请求
     */
    private void customizeCombineQueryOrder(WechatPayCombineOrderRequest request) {
        WechatPayRequestOptions options = new WechatPayRequestOptions();
        for (WechatPayRequestCustomizer requestCustomizer : requestCustomizers) {
            requestCustomizer.customizeCombineQueryOrder(request, options);
        }
    }

    /**
     * 应用合单关单请求扩展器。
     *
     * @param request 合单关单请求
     * @return 扩展后的请求参数
     */
    private WechatPayRequestOptions customizeCombineClose(WechatPayCombineCloseRequest request) {
        WechatPayRequestOptions options = new WechatPayRequestOptions();
        for (WechatPayRequestCustomizer requestCustomizer : requestCustomizers) {
            requestCustomizer.customizeCombineClose(request, options);
        }
        return options;
    }

    /**
     * 微信合单下单原始响应。
     */
    private static final class CombineCreateResult {

        /**
         * 预支付交易会话标识。
         */
        private String prepay_id;

        /**
         * H5 支付地址。
         */
        private String h5_url;

        /**
         * Native 支付二维码地址。
         */
        private String code_url;
    }

    /**
     * 微信合单查单原始响应。
     */
    private static final class CombineQueryResult {

        /**
         * 合单应用 ID。
         */
        private String combine_appid;

        /**
         * 合单商户号。
         */
        private String combine_mchid;

        /**
         * 合单商户订单号。
         */
        private String combine_out_trade_no;

        /**
         * 合单状态。
         */
        private String combine_state;

        /**
         * 合单交易状态。
         */
        private String combine_trade_state;

        /**
         * 通用状态字段。
         */
        private String state;

        /**
         * 支付成功时间。
         */
        private String success_time;

        /**
         * 微信交易单号。
         */
        private String transaction_id;

        /**
         * 合单付款人信息。
         */
        private CombinePayerInfo combine_payer_info;

        /**
         * 合单金额信息。
         */
        private CombineAmount amount;

        /**
         * 子订单列表。
         */
        private List<CombineSubOrder> sub_orders;
    }

    /**
     * 微信合单通知原始响应。
     */
    private static final class CombineNotifyResult {

        /**
         * 合单应用 ID。
         */
        private String combine_appid;

        /**
         * 合单商户号。
         */
        private String combine_mchid;

        /**
         * 合单商户订单号。
         */
        private String combine_out_trade_no;

        /**
         * 合单状态。
         */
        private String combine_state;

        /**
         * 通用状态字段。
         */
        private String state;

        /**
         * 支付成功时间。
         */
        private String success_time;

        /**
         * 微信交易单号。
         */
        private String transaction_id;

        /**
         * 合单付款人信息。
         */
        private CombinePayerInfo combine_payer_info;

        /**
         * 合单金额信息。
         */
        private CombineAmount amount;

        /**
         * 子订单列表。
         */
        private List<CombineSubOrder> sub_orders;
    }

    /**
     * 微信合单付款人信息。
     */
    private static final class CombinePayerInfo {

        /**
         * 用户 OpenID。
         */
        private String openid;
    }

    /**
     * 微信合单子订单信息。
     */
    private static final class CombineSubOrder {

        /**
         * 子订单商户号。
         */
        private String mchid;

        /**
         * 子订单商户订单号。
         */
        private String out_trade_no;

        /**
         * 子订单微信交易单号。
         */
        private String transaction_id;

        /**
         * 交易类型。
         */
        private String trade_type;

        /**
         * 交易状态。
         */
        private String trade_state;

        /**
         * 通用状态字段。
         */
        private String state;

        /**
         * 附加数据。
         */
        private String attach;

        /**
         * 付款银行类型。
         */
        private String bank_type;

        /**
         * 支付成功时间。
         */
        private String success_time;

        /**
         * 子订单金额信息。
         */
        private CombineAmount amount;
    }

    /**
     * 微信合单金额信息。
     */
    private static final class CombineAmount {

        /**
         * 总金额，单位分。
         */
        private Long total_amount;

        /**
         * 币种。
         */
        private String currency;

        /**
         * 用户实付金额，单位分。
         */
        private Long payer_amount;

        /**
         * 用户支付币种。
         */
        private String payer_currency;

        /**
         * 清算汇率。
         */
        private Long settlement_rate;
    }

}
