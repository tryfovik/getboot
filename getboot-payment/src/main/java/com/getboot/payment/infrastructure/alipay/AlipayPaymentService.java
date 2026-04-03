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
package com.getboot.payment.infrastructure.alipay;

import com.alipay.easysdk.payment.app.models.AlipayTradeAppPayResponse;
import com.alipay.easysdk.payment.common.models.AlipayTradeFastpayRefundQueryResponse;
import com.alipay.easysdk.payment.common.models.AlipayTradeQueryResponse;
import com.alipay.easysdk.payment.common.models.AlipayTradeRefundResponse;
import com.alipay.easysdk.payment.facetoface.models.AlipayTradePrecreateResponse;
import com.alipay.easysdk.payment.page.models.AlipayTradePagePayResponse;
import com.alipay.easysdk.payment.wap.models.AlipayTradeWapPayResponse;
import com.getboot.exception.api.exception.BusinessException;
import com.getboot.payment.api.model.PaymentChannel;
import com.getboot.payment.support.PaymentInvoker;
import com.getboot.payment.api.model.PaymentMode;
import com.getboot.payment.api.model.PaymentNotifyType;
import com.getboot.payment.api.properties.PaymentProperties;
import com.getboot.payment.api.request.PaymentCloseRequest;
import com.getboot.payment.api.request.PaymentCreateRequest;
import com.getboot.payment.api.request.PaymentNotifyRequest;
import com.getboot.payment.api.request.PaymentOrderQueryRequest;
import com.getboot.payment.api.request.PaymentRefundQueryRequest;
import com.getboot.payment.api.request.PaymentRefundRequest;
import com.getboot.payment.api.response.PaymentCloseResponse;
import com.getboot.payment.api.response.PaymentCreateResponse;
import com.getboot.payment.api.response.PaymentNotifyResponse;
import com.getboot.payment.api.response.PaymentOrderQueryResponse;
import com.getboot.payment.api.response.PaymentRefundQueryResponse;
import com.getboot.payment.api.response.PaymentRefundResponse;
import com.getboot.payment.api.service.PaymentService;
import com.getboot.payment.spi.alipay.AlipayRequestCustomizer;
import com.getboot.payment.spi.alipay.AlipayRequestOptions;
import com.getboot.payment.support.alipay.AlipayGateway;
import com.getboot.payment.support.alipay.AlipayNotifySupport;
import com.getboot.payment.support.alipay.AlipayRequestSupport;
import com.getboot.payment.support.alipay.AlipayResponseSupport;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 支付宝官方 Easy SDK 统一适配实现。
 *
 * <p>当前打通 App、电脑网站、手机网站、扫码预下单四类主流支付方式。</p>
 *
 * @author qiheng
 */
public class AlipayPaymentService implements PaymentService {

    /**
     * 支持的支付方式集合。
     */
    private static final Set<PaymentMode> SUPPORTED_MODES = Set.copyOf(EnumSet.of(
            PaymentMode.APP,
            PaymentMode.PAGE,
            PaymentMode.WAP,
            PaymentMode.NATIVE
    ));

    /**
     * 支付宝渠道配置。
     */
    private final PaymentProperties.Alipay properties;

    /**
     * 支付宝 SDK 网关。
     */
    private final AlipayGateway gateway;

    /**
     * 请求扩展器列表。
     */
    private final List<AlipayRequestCustomizer> requestCustomizers;

    /**
     * 构造统一支付宝支付服务。
     *
     * @param paymentProperties 支付配置
     * @param gateway           SDK 网关
     */
    public AlipayPaymentService(PaymentProperties paymentProperties, AlipayGateway gateway) {
        this(paymentProperties, gateway, List.of());
    }

    /**
     * 构造统一支付宝支付服务。
     *
     * @param paymentProperties 支付配置
     * @param gateway           SDK 网关
     * @param requestCustomizers 请求扩展器
     */
    public AlipayPaymentService(
            PaymentProperties paymentProperties,
            AlipayGateway gateway,
            List<AlipayRequestCustomizer> requestCustomizers) {
        this.properties = paymentProperties.getAlipay();
        this.gateway = gateway;
        this.requestCustomizers = requestCustomizers == null ? List.of() : List.copyOf(requestCustomizers);
    }

    /**
     * 返回当前服务支持的支付渠道。
     *
     * @return 支付渠道
     */
    @Override
    public PaymentChannel channel() {
        return PaymentChannel.ALIPAY;
    }

    /**
     * 返回当前服务支持的支付方式。
     *
     * @return 支付方式集合
     */
    @Override
    public Set<PaymentMode> supportedModes() {
        return SUPPORTED_MODES;
    }

    /**
     * 创建支付宝支付订单。
     *
     * @param request 下单请求
     * @return 下单响应
     */
    @Override
    public PaymentCreateResponse create(PaymentCreateRequest request) {
        requireChannel(request.getChannel());
        PaymentMode mode = requireSupportedMode(request.getMode());
        requireText(request.getMerchantOrderNo(), "merchantOrderNo must not be blank");
        requireText(request.getSubject(), "subject must not be blank");
        requireAmount(request.getAmount(), "amount must not be null");

        AlipayRequestOptions options = buildCreateOptions(request);
        return switch (mode) {
            case APP -> createAppOrder(request, options);
            case PAGE -> createPageOrder(request, options);
            case WAP -> createWapOrder(request, options);
            case NATIVE -> createNativeOrder(request, options);
            default -> throw unsupportedMode(mode);
        };
    }

    /**
     * 发起支付宝退款。
     *
     * @param request 退款请求
     * @return 退款响应
     */
    @Override
    public PaymentRefundResponse refund(PaymentRefundRequest request) {
        requireChannel(request.getChannel());
        requireText(request.getMerchantOrderNo(), "merchantOrderNo must not be blank");
        requireText(request.getRefundRequestNo(), "refundRequestNo must not be blank");
        requireAmount(request.getRefundAmount(), "refundAmount must not be null");

        AlipayRequestOptions options = buildRefundOptions(request);
        AlipayTradeRefundResponse response = PaymentInvoker.invoke(
                () -> gateway.refund(
                        request.getMerchantOrderNo(),
                        toAmountText(request.getRefundAmount()),
                        options.getNotifyUrl(),
                        options.getOptionalArgs(),
                        AlipayRequestSupport.resolveContext(options)
                ),
                "Failed to refund Alipay order"
        );
        AlipayResponseSupport.ensureSuccess(response, "Failed to refund Alipay order");
        return PaymentRefundResponse.builder()
                .channel(PaymentChannel.ALIPAY)
                .merchantOrderNo(response.getOutTradeNo())
                .refundRequestNo(request.getRefundRequestNo())
                .platformRefundNo(null)
                .status("SUCCESS")
                .metadata(AlipayResponseSupport.extractMetadata(response))
                .build();
    }

    /**
     * 查询支付宝订单状态。
     *
     * @param request 订单查询请求
     * @return 查询响应
     */
    @Override
    public PaymentOrderQueryResponse queryOrder(PaymentOrderQueryRequest request) {
        requireChannel(request.getChannel());
        requireText(request.getMerchantOrderNo(), "merchantOrderNo must not be blank");

        AlipayRequestOptions options = buildOrderQueryOptions(request);
        AlipayTradeQueryResponse response = PaymentInvoker.invoke(
                () -> gateway.query(
                        request.getMerchantOrderNo(),
                        options.getOptionalArgs(),
                        AlipayRequestSupport.resolveContext(options)
                ),
                "Failed to query Alipay order"
        );
        AlipayResponseSupport.ensureSuccess(response, "Failed to query Alipay order");
        return PaymentOrderQueryResponse.builder()
                .channel(PaymentChannel.ALIPAY)
                .mode(request.getMode())
                .merchantOrderNo(response.getOutTradeNo())
                .platformOrderNo(response.getTradeNo())
                .status(response.getTradeStatus())
                .paidAmount(AlipayResponseSupport.toBigDecimal(
                        AlipayResponseSupport.firstNonBlank(response.getBuyerPayAmount(), response.getTotalAmount())
                ))
                .currency(AlipayResponseSupport.firstNonBlank(
                        response.getPayCurrency(),
                        response.getTransCurrency(),
                        response.getSettleCurrency()
                ))
                .payerId(response.getBuyerUserId())
                .successTime(response.getSendPayDate())
                .metadata(AlipayResponseSupport.extractMetadata(response))
                .build();
    }

    /**
     * 查询支付宝退款状态。
     *
     * @param request 退款查询请求
     * @return 查询响应
     */
    @Override
    public PaymentRefundQueryResponse queryRefund(PaymentRefundQueryRequest request) {
        requireChannel(request.getChannel());
        requireText(request.getMerchantOrderNo(), "merchantOrderNo must not be blank");
        requireText(request.getRefundRequestNo(), "refundRequestNo must not be blank");

        AlipayRequestOptions options = buildRefundQueryOptions(request);
        AlipayTradeFastpayRefundQueryResponse response = PaymentInvoker.invoke(
                () -> gateway.queryRefund(
                        request.getMerchantOrderNo(),
                        request.getRefundRequestNo(),
                        options.getOptionalArgs(),
                        AlipayRequestSupport.resolveContext(options)
                ),
                "Failed to query Alipay refund"
        );
        AlipayResponseSupport.ensureSuccess(response, "Failed to query Alipay refund");
        return PaymentRefundQueryResponse.builder()
                .channel(PaymentChannel.ALIPAY)
                .merchantOrderNo(response.getOutTradeNo())
                .refundRequestNo(response.getOutRequestNo())
                .platformRefundNo(null)
                .status(response.getRefundStatus())
                .refundAmount(AlipayResponseSupport.toBigDecimal(response.getRefundAmount()))
                .currency(null)
                .successTime(response.getGmtRefundPay())
                .metadata(AlipayResponseSupport.extractMetadata(response))
                .build();
    }

    /**
     * 关闭支付宝订单。
     *
     * @param request 关单请求
     * @return 关单响应
     */
    @Override
    public PaymentCloseResponse close(PaymentCloseRequest request) {
        requireChannel(request.getChannel());
        requireText(request.getMerchantOrderNo(), "merchantOrderNo must not be blank");

        AlipayRequestOptions options = buildCloseOptions(request);
        com.alipay.easysdk.payment.common.models.AlipayTradeCloseResponse response = PaymentInvoker.invoke(
                () -> gateway.close(
                        request.getMerchantOrderNo(),
                        options.getOptionalArgs(),
                        AlipayRequestSupport.resolveContext(options)
                ),
                "Failed to close Alipay order"
        );
        AlipayResponseSupport.ensureSuccess(response, "Failed to close Alipay order");
        return PaymentCloseResponse.builder()
                .channel(PaymentChannel.ALIPAY)
                .merchantOrderNo(response.getOutTradeNo())
                .closed(true)
                .build();
    }

    /**
     * 解析支付宝异步通知。
     *
     * @param request 通知请求
     * @return 通知解析结果
     */
    @Override
    public PaymentNotifyResponse parseNotify(PaymentNotifyRequest request) {
        requireChannel(request.getChannel());
        if (request.getNotifyType() == null) {
            throw new BusinessException("notifyType must not be null");
        }
        requireText(request.getBody(), "body must not be blank");

        Map<String, String> parameters = AlipayNotifySupport.parseFormBody(request.getBody());
        boolean verified = PaymentInvoker.invoke(() -> gateway.verifyNotify(parameters), "Failed to verify Alipay notify");
        if (!verified) {
            throw new BusinessException("Failed to verify Alipay notify");
        }
        return switch (request.getNotifyType()) {
            case PAYMENT -> buildPaymentNotifyResponse(parameters);
            case REFUND -> buildRefundNotifyResponse(parameters);
        };
    }

    /**
     * 创建支付宝 App 支付订单。
     *
     * @param request 下单请求
     * @param options 请求选项
     * @return 下单响应
     */
    private PaymentCreateResponse createAppOrder(
            PaymentCreateRequest request,
            AlipayRequestOptions options) {
        AlipayTradeAppPayResponse response = PaymentInvoker.invoke(
                () -> gateway.appPay(
                        request.getSubject(),
                        request.getMerchantOrderNo(),
                        toAmountText(request.getAmount()),
                        options.getNotifyUrl(),
                        options.getOptionalArgs(),
                        AlipayRequestSupport.resolveContext(options)
                ),
                "Failed to create Alipay app order"
        );
        PaymentCreateResponse createResponse = baseCreateResponse(request);
        createResponse.getPaymentData().put("orderString", response.getBody());
        return createResponse;
    }

    /**
     * 创建支付宝页面支付订单。
     *
     * @param request 下单请求
     * @param options 请求选项
     * @return 下单响应
     */
    private PaymentCreateResponse createPageOrder(
            PaymentCreateRequest request,
            AlipayRequestOptions options) {
        AlipayTradePagePayResponse response = PaymentInvoker.invoke(
                () -> gateway.pagePay(
                        request.getSubject(),
                        request.getMerchantOrderNo(),
                        toAmountText(request.getAmount()),
                        options.getReturnUrl(),
                        options.getNotifyUrl(),
                        options.getOptionalArgs(),
                        AlipayRequestSupport.resolveContext(options)
                ),
                "Failed to create Alipay page order"
        );
        PaymentCreateResponse createResponse = baseCreateResponse(request);
        createResponse.getPaymentData().put("form", response.getBody());
        return createResponse;
    }

    /**
     * 创建支付宝 WAP 支付订单。
     *
     * @param request 下单请求
     * @param options 请求选项
     * @return 下单响应
     */
    private PaymentCreateResponse createWapOrder(
            PaymentCreateRequest request,
            AlipayRequestOptions options) {
        requireText(options.getQuitUrl(), "quitUrl must not be blank for Alipay WAP payment");
        AlipayTradeWapPayResponse response = PaymentInvoker.invoke(
                () -> gateway.wapPay(
                        request.getSubject(),
                        request.getMerchantOrderNo(),
                        toAmountText(request.getAmount()),
                        options.getQuitUrl(),
                        options.getReturnUrl(),
                        options.getNotifyUrl(),
                        options.getOptionalArgs(),
                        AlipayRequestSupport.resolveContext(options)
                ),
                "Failed to create Alipay WAP order"
        );
        PaymentCreateResponse createResponse = baseCreateResponse(request);
        createResponse.getPaymentData().put("form", response.getBody());
        return createResponse;
    }

    /**
     * 创建支付宝扫码预下单。
     *
     * @param request 下单请求
     * @param options 请求选项
     * @return 下单响应
     */
    private PaymentCreateResponse createNativeOrder(
            PaymentCreateRequest request,
            AlipayRequestOptions options) {
        AlipayTradePrecreateResponse response = PaymentInvoker.invoke(
                () -> gateway.preCreate(
                        request.getSubject(),
                        request.getMerchantOrderNo(),
                        toAmountText(request.getAmount()),
                        options.getNotifyUrl(),
                        options.getOptionalArgs(),
                        AlipayRequestSupport.resolveContext(options)
                ),
                "Failed to create Alipay native order"
        );
        AlipayResponseSupport.ensureSuccess(response, "Failed to create Alipay native order");
        PaymentCreateResponse createResponse = baseCreateResponse(request);
        createResponse.setMerchantOrderNo(response.getOutTradeNo());
        createResponse.setQrCodeContent(response.getQrCode());
        createResponse.setMetadata(AlipayResponseSupport.extractMetadata(response));
        return createResponse;
    }

    /**
     * 构建基础下单响应。
     *
     * @param request 下单请求
     * @return 基础响应
     */
    private PaymentCreateResponse baseCreateResponse(PaymentCreateRequest request) {
        return PaymentCreateResponse.builder()
                .channel(PaymentChannel.ALIPAY)
                .mode(request.getMode())
                .merchantOrderNo(request.getMerchantOrderNo())
                .build();
    }

    /**
     * 构建下单请求选项。
     *
     * @param request 下单请求
     * @return 请求选项
     */
    private AlipayRequestOptions buildCreateOptions(PaymentCreateRequest request) {
        AlipayRequestOptions options = newRequestOptions(request.getMetadata());
        options.setNotifyUrl(resolveText(request.getNotifyUrl(), properties.getNotifyUrl()));
        options.setReturnUrl(resolveText(request.getReturnUrl(), properties.getReturnUrl()));
        options.setQuitUrl(AlipayResponseSupport.firstNonBlank(
                AlipayRequestSupport.text(request.getMetadata(), AlipayRequestSupport.QUIT_URL),
                request.getReturnUrl(),
                properties.getReturnUrl()
        ));
        AlipayRequestSupport.putIfText(options.getOptionalArgs(), "body", request.getDescription());
        AlipayRequestSupport.putIfText(options.getOptionalArgs(), "buyer_id", request.getPayerId());
        AlipayRequestSupport.putIfText(options.getOptionalArgs(), "time_expire", request.getTimeExpire());
        AlipayRequestSupport.putIfText(
                options.getOptionalArgs(),
                "timeout_express",
                AlipayRequestSupport.text(request.getMetadata(), AlipayRequestSupport.TIMEOUT_EXPRESS)
        );
        AlipayRequestSupport.putIfText(
                options.getOptionalArgs(),
                "passback_params",
                AlipayRequestSupport.text(request.getMetadata(), AlipayRequestSupport.PASSBACK_PARAMS)
        );
        AlipayRequestSupport.putIfText(
                options.getOptionalArgs(),
                "seller_id",
                AlipayRequestSupport.text(request.getMetadata(), AlipayRequestSupport.SELLER_ID)
        );
        AlipayRequestSupport.putIfText(
                options.getOptionalArgs(),
                "store_id",
                AlipayRequestSupport.text(request.getMetadata(), AlipayRequestSupport.STORE_ID)
        );
        AlipayRequestSupport.putIfText(
                options.getOptionalArgs(),
                "operator_id",
                AlipayRequestSupport.text(request.getMetadata(), AlipayRequestSupport.OPERATOR_ID)
        );
        AlipayRequestSupport.putIfText(
                options.getOptionalArgs(),
                "terminal_id",
                AlipayRequestSupport.text(request.getMetadata(), AlipayRequestSupport.TERMINAL_ID)
        );
        AlipayRequestSupport.putIfText(
                options.getOptionalArgs(),
                "disable_pay_channels",
                AlipayRequestSupport.text(request.getMetadata(), AlipayRequestSupport.DISABLE_PAY_CHANNELS)
        );
        AlipayRequestSupport.putIfText(
                options.getOptionalArgs(),
                "enable_pay_channels",
                AlipayRequestSupport.text(request.getMetadata(), AlipayRequestSupport.ENABLE_PAY_CHANNELS)
        );
        String serviceProviderId = AlipayRequestSupport.text(request.getMetadata(), AlipayRequestSupport.SERVICE_PROVIDER_ID);
        if (StringUtils.hasText(serviceProviderId)) {
            options.putOptionalArg("extend_params", Map.of("sys_service_provider_id", serviceProviderId));
        }
        for (AlipayRequestCustomizer customizer : requestCustomizers) {
            customizer.customizeCreate(request, options);
        }
        return options;
    }

    /**
     * 构建退款请求选项。
     *
     * @param request 退款请求
     * @return 请求选项
     */
    private AlipayRequestOptions buildRefundOptions(PaymentRefundRequest request) {
        AlipayRequestOptions options = newRequestOptions(request.getMetadata());
        options.setNotifyUrl(resolveText(request.getNotifyUrl(), properties.getNotifyUrl()));
        AlipayRequestSupport.putIfText(options.getOptionalArgs(), "out_request_no", request.getRefundRequestNo());
        AlipayRequestSupport.putIfText(options.getOptionalArgs(), "refund_reason", request.getReason());
        AlipayRequestSupport.putIfText(options.getOptionalArgs(), "trade_no", request.getPlatformOrderNo());
        AlipayRequestSupport.putIfText(
                options.getOptionalArgs(),
                "store_id",
                AlipayRequestSupport.text(request.getMetadata(), AlipayRequestSupport.STORE_ID)
        );
        AlipayRequestSupport.putIfText(
                options.getOptionalArgs(),
                "operator_id",
                AlipayRequestSupport.text(request.getMetadata(), AlipayRequestSupport.OPERATOR_ID)
        );
        AlipayRequestSupport.putIfText(
                options.getOptionalArgs(),
                "terminal_id",
                AlipayRequestSupport.text(request.getMetadata(), AlipayRequestSupport.TERMINAL_ID)
        );
        for (AlipayRequestCustomizer customizer : requestCustomizers) {
            customizer.customizeRefund(request, options);
        }
        return options;
    }

    /**
     * 构建订单查询请求选项。
     *
     * @param request 订单查询请求
     * @return 请求选项
     */
    private AlipayRequestOptions buildOrderQueryOptions(PaymentOrderQueryRequest request) {
        AlipayRequestOptions options = newRequestOptions(request.getMetadata());
        AlipayRequestSupport.putIfText(options.getOptionalArgs(), "trade_no", request.getPlatformOrderNo());
        for (AlipayRequestCustomizer customizer : requestCustomizers) {
            customizer.customizeQueryOrder(request, options);
        }
        return options;
    }

    /**
     * 构建退款查询请求选项。
     *
     * @param request 退款查询请求
     * @return 请求选项
     */
    private AlipayRequestOptions buildRefundQueryOptions(PaymentRefundQueryRequest request) {
        AlipayRequestOptions options = newRequestOptions(request.getMetadata());
        AlipayRequestSupport.putIfText(options.getOptionalArgs(), "trade_no", request.getPlatformOrderNo());
        for (AlipayRequestCustomizer customizer : requestCustomizers) {
            customizer.customizeQueryRefund(request, options);
        }
        return options;
    }

    /**
     * 构建关单请求选项。
     *
     * @param request 关单请求
     * @return 请求选项
     */
    private AlipayRequestOptions buildCloseOptions(PaymentCloseRequest request) {
        AlipayRequestOptions options = newRequestOptions(request.getMetadata());
        for (AlipayRequestCustomizer customizer : requestCustomizers) {
            customizer.customizeClose(request, options);
        }
        return options;
    }

    /**
     * 根据元数据创建请求选项。
     *
     * @param metadata 元数据
     * @return 请求选项
     */
    private AlipayRequestOptions newRequestOptions(Map<String, String> metadata) {
        AlipayRequestOptions options = new AlipayRequestOptions();
        options.setAppAuthToken(AlipayRequestSupport.text(metadata, AlipayRequestSupport.APP_AUTH_TOKEN));
        options.setAuthToken(AlipayRequestSupport.text(metadata, AlipayRequestSupport.AUTH_TOKEN));
        options.setRoute(AlipayRequestSupport.text(metadata, AlipayRequestSupport.ROUTE));
        return options;
    }

    /**
     * 构建支付通知响应。
     *
     * @param parameters 通知参数
     * @return 通知响应
     */
    private PaymentNotifyResponse buildPaymentNotifyResponse(Map<String, String> parameters) {
        String status = parameters.get("trade_status");
        return PaymentNotifyResponse.builder()
                .channel(PaymentChannel.ALIPAY)
                .notifyType(PaymentNotifyType.PAYMENT)
                .merchantOrderNo(parameters.get("out_trade_no"))
                .platformOrderNo(parameters.get("trade_no"))
                .status(status)
                .success("TRADE_SUCCESS".equals(status) || "TRADE_FINISHED".equals(status))
                .eventTime(AlipayResponseSupport.firstNonBlank(parameters.get("gmt_payment"), parameters.get("notify_time")))
                .metadata(AlipayNotifySupport.buildMetadata(parameters))
                .build();
    }

    /**
     * 构建退款通知响应。
     *
     * @param parameters 通知参数
     * @return 通知响应
     */
    private PaymentNotifyResponse buildRefundNotifyResponse(Map<String, String> parameters) {
        String status = AlipayResponseSupport.firstNonBlank(parameters.get("refund_status"), parameters.get("trade_status"));
        boolean success = "REFUND_SUCCESS".equalsIgnoreCase(status)
                || "SUCCESS".equalsIgnoreCase(status)
                || (!StringUtils.hasText(status) && StringUtils.hasText(parameters.get("gmt_refund")));
        return PaymentNotifyResponse.builder()
                .channel(PaymentChannel.ALIPAY)
                .notifyType(PaymentNotifyType.REFUND)
                .merchantOrderNo(parameters.get("out_trade_no"))
                .platformOrderNo(parameters.get("trade_no"))
                .refundRequestNo(AlipayResponseSupport.firstNonBlank(parameters.get("out_request_no"), parameters.get("out_biz_no")))
                .status(status)
                .success(success)
                .eventTime(AlipayResponseSupport.firstNonBlank(parameters.get("gmt_refund"), parameters.get("notify_time")))
                .metadata(AlipayNotifySupport.buildMetadata(parameters))
                .build();
    }

    /**
     * 解析优先值与回退值。
     *
     * @param candidate 优先值
     * @param fallback 回退值
     * @return 最终值
     */
    private String resolveText(String candidate, String fallback) {
        return StringUtils.hasText(candidate) ? candidate : fallback;
    }

    /**
     * 将金额格式化为支付宝要求的文本。
     *
     * @param amount 金额
     * @return 金额文本
     */
    private String toAmountText(BigDecimal amount) {
        return amount.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    /**
     * 校验支付方式已被支持。
     *
     * @param mode 支付方式
     * @return 支付方式
     */
    private PaymentMode requireSupportedMode(PaymentMode mode) {
        if (!supports(mode)) {
            throw unsupportedMode(mode);
        }
        return mode;
    }

    /**
     * 构造不支持支付方式异常。
     *
     * @param mode 支付方式
     * @return 业务异常
     */
    private BusinessException unsupportedMode(PaymentMode mode) {
        return new BusinessException("Unsupported Alipay payment mode: " + mode);
    }

    /**
     * 校验渠道是否为支付宝。
     *
     * @param channel 支付渠道
     */
    private void requireChannel(PaymentChannel channel) {
        if (channel != PaymentChannel.ALIPAY) {
            throw new BusinessException("channel must be ALIPAY");
        }
    }

    /**
     * 校验文本参数不为空。
     *
     * @param value 待校验值
     * @param message 校验失败消息
     */
    private void requireText(String value, String message) {
        if (!StringUtils.hasText(value)) {
            throw new BusinessException(message);
        }
    }

    /**
     * 校验金额参数不为空。
     *
     * @param value 待校验金额
     * @param message 校验失败消息
     */
    private void requireAmount(BigDecimal value, String message) {
        if (value == null) {
            throw new BusinessException(message);
        }
    }
}
