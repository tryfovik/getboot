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
package com.getboot.payment.infrastructure.wechatpay;

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
import com.getboot.payment.spi.wechatpay.WechatPayRequestCustomizer;
import com.getboot.payment.spi.wechatpay.WechatPayRequestOptions;
import com.getboot.payment.support.wechatpay.WechatPayRequestSupport;
import com.getboot.payment.support.wechatpay.WechatPayResponseSupport;
import com.wechat.pay.java.core.notification.NotificationParser;
import com.wechat.pay.java.core.notification.RequestParam;
import com.wechat.pay.java.service.payments.app.AppServiceExtension;
import com.wechat.pay.java.service.payments.app.model.PrepayRequest;
import com.wechat.pay.java.service.payments.h5.H5Service;
import com.wechat.pay.java.service.payments.jsapi.JsapiService;
import com.wechat.pay.java.service.payments.jsapi.JsapiServiceExtension;
import com.wechat.pay.java.service.payments.model.Transaction;
import com.wechat.pay.java.service.payments.nativepay.NativePayService;
import com.wechat.pay.java.service.refund.RefundService;
import com.wechat.pay.java.service.refund.model.Refund;
import com.wechat.pay.java.service.refund.model.RefundNotification;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 微信支付官方 SDK 统一适配实现。
 *
 * <p>当前打通公众号/小程序、App、H5、Native 四类主流支付方式。</p>
 *
 * @author qiheng
 */
public class WechatPayPaymentService implements PaymentService {

    /**
     * 支持的支付方式集合。
     */
    private static final Set<PaymentMode> SUPPORTED_MODES = Set.copyOf(EnumSet.of(
            PaymentMode.JSAPI,
            PaymentMode.MINI_PROGRAM,
            PaymentMode.APP,
            PaymentMode.H5,
            PaymentMode.NATIVE
    ));

    /**
     * 通知头中的平台证书序列号键。
     */
    private static final String HEADER_SERIAL = "Wechatpay-Serial";

    /**
     * 通知头中的时间戳键。
     */
    private static final String HEADER_TIMESTAMP = "Wechatpay-Timestamp";

    /**
     * 通知头中的随机串键。
     */
    private static final String HEADER_NONCE = "Wechatpay-Nonce";

    /**
     * 通知头中的签名键。
     */
    private static final String HEADER_SIGNATURE = "Wechatpay-Signature";

    /**
     * 通知头中的签名类型键。
     */
    private static final String HEADER_SIGN_TYPE = "Wechatpay-Signtype";

    /**
     * 微信支付渠道配置。
     */
    private final PaymentProperties.WechatPay properties;

    /**
     * 通知解析器。
     */
    private final NotificationParser notificationParser;

    /**
     * JSAPI 服务。
     */
    private final JsapiService jsapiService;

    /**
     * JSAPI 前端拉起参数服务。
     */
    private final JsapiServiceExtension jsapiServiceExtension;

    /**
     * App 前端拉起参数服务。
     */
    private final AppServiceExtension appServiceExtension;

    /**
     * H5 支付服务。
     */
    private final H5Service h5Service;

    /**
     * Native 支付服务。
     */
    private final NativePayService nativePayService;

    /**
     * 退款服务。
     */
    private final RefundService refundService;

    /**
     * 请求扩展器集合。
     */
    private final List<WechatPayRequestCustomizer> requestCustomizers;

    /**
     * 构造统一微信支付服务。
     *
     * @param paymentProperties     支付配置
     * @param notificationParser    通知解析器
     * @param jsapiService          JSAPI 服务
     * @param jsapiServiceExtension JSAPI 扩展服务
     * @param appServiceExtension   App 扩展服务
     * @param h5Service             H5 支付服务
     * @param nativePayService      Native 支付服务
     * @param refundService         退款服务
     */
    public WechatPayPaymentService(
            PaymentProperties paymentProperties,
            NotificationParser notificationParser,
            JsapiService jsapiService,
            JsapiServiceExtension jsapiServiceExtension,
            AppServiceExtension appServiceExtension,
            H5Service h5Service,
            NativePayService nativePayService,
            RefundService refundService) {
        this(
                paymentProperties,
                notificationParser,
                jsapiService,
                jsapiServiceExtension,
                appServiceExtension,
                h5Service,
                nativePayService,
                refundService,
                List.of()
        );
    }

    /**
     * 构造统一微信支付服务。
     *
     * @param paymentProperties     支付配置
     * @param notificationParser    通知解析器
     * @param jsapiService          JSAPI 服务
     * @param jsapiServiceExtension JSAPI 扩展服务
     * @param appServiceExtension   App 扩展服务
     * @param h5Service             H5 支付服务
     * @param nativePayService      Native 支付服务
     * @param refundService         退款服务
     * @param requestCustomizers    请求扩展器
     */
    public WechatPayPaymentService(
            PaymentProperties paymentProperties,
            NotificationParser notificationParser,
            JsapiService jsapiService,
            JsapiServiceExtension jsapiServiceExtension,
            AppServiceExtension appServiceExtension,
            H5Service h5Service,
            NativePayService nativePayService,
            RefundService refundService,
            List<WechatPayRequestCustomizer> requestCustomizers) {
        this.properties = paymentProperties.getWechatpay();
        this.notificationParser = notificationParser;
        this.jsapiService = jsapiService;
        this.jsapiServiceExtension = jsapiServiceExtension;
        this.appServiceExtension = appServiceExtension;
        this.h5Service = h5Service;
        this.nativePayService = nativePayService;
        this.refundService = refundService;
        this.requestCustomizers = requestCustomizers == null ? List.of() : List.copyOf(requestCustomizers);
    }

    /**
     * 返回当前服务支持的支付渠道。
     *
     * @return 支付渠道
     */
    @Override
    public PaymentChannel channel() {
        return PaymentChannel.WECHAT_PAY;
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
     * 创建微信支付订单。
     *
     * @param request 下单请求
     * @return 下单响应
     */
    @Override
    public PaymentCreateResponse create(PaymentCreateRequest request) {
        requireChannel(request.getChannel());
        PaymentMode mode = requireSupportedMode(request.getMode());
        WechatPayRequestOptions options = customizeCreate(request);
        validateCreateRequest(request, mode, options);
        return switch (mode) {
            case JSAPI, MINI_PROGRAM -> createJsapiOrder(request, options);
            case APP -> createAppOrder(request, options);
            case H5 -> createH5Order(request, options);
            case NATIVE -> createNativeOrder(request, options);
            default -> throw unsupportedMode(mode);
        };
    }

    /**
     * 发起微信支付退款。
     *
     * @param request 退款请求
     * @return 退款响应
     */
    @Override
    public PaymentRefundResponse refund(PaymentRefundRequest request) {
        requireChannel(request.getChannel());
        requireText(request.getRefundRequestNo(), "refundRequestNo must not be blank");
        requireText(request.getMerchantOrderNo(), "merchantOrderNo must not be blank");
        requireAmount(request.getTotalAmount(), "totalAmount must not be null");
        requireAmount(request.getRefundAmount(), "refundAmount must not be null");
        WechatPayRequestOptions options = customizeRefund(request);

        com.wechat.pay.java.service.refund.model.CreateRequest refundRequest =
                new com.wechat.pay.java.service.refund.model.CreateRequest();
        refundRequest.setOutTradeNo(request.getMerchantOrderNo());
        refundRequest.setOutRefundNo(request.getRefundRequestNo());
        refundRequest.setReason(request.getReason());
        refundRequest.setNotifyUrl(resolveNotifyUrl(request.getNotifyUrl(), options));

        if (StringUtils.hasText(request.getPlatformOrderNo())) {
            refundRequest.setTransactionId(request.getPlatformOrderNo());
        }

        com.wechat.pay.java.service.refund.model.AmountReq amount =
                new com.wechat.pay.java.service.refund.model.AmountReq();
        amount.setTotal(WechatPayAmounts.toFenLong(request.getTotalAmount()));
        amount.setRefund(WechatPayAmounts.toFenLong(request.getRefundAmount()));
        amount.setCurrency(request.getCurrency());
        refundRequest.setAmount(amount);

        Refund refund = PaymentInvoker.invoke(() -> refundService.create(refundRequest), "Failed to create WeChat Pay refund");
        return PaymentRefundResponse.builder()
                .channel(PaymentChannel.WECHAT_PAY)
                .merchantOrderNo(refund.getOutTradeNo())
                .refundRequestNo(refund.getOutRefundNo())
                .platformRefundNo(refund.getRefundId())
                .status(WechatPayResponseSupport.enumName(refund.getStatus()))
                .metadata(WechatPayResponseSupport.buildRefundMetadata(refund))
                .build();
    }

    /**
     * 查询微信支付订单。
     *
     * @param request 订单查询请求
     * @return 查询响应
     */
    @Override
    public PaymentOrderQueryResponse queryOrder(PaymentOrderQueryRequest request) {
        requireChannel(request.getChannel());
        Transaction transaction = queryTransaction(request, customizeQueryOrder(request));
        return PaymentOrderQueryResponse.builder()
                .channel(PaymentChannel.WECHAT_PAY)
                .mode(resolveMode(request.getMode(), transaction))
                .merchantOrderNo(transaction.getOutTradeNo())
                .platformOrderNo(transaction.getTransactionId())
                .status(WechatPayResponseSupport.enumName(transaction.getTradeState()))
                .paidAmount(WechatPayResponseSupport.amountFromTransaction(transaction))
                .currency(WechatPayResponseSupport.currencyFromTransaction(transaction))
                .payerId(transaction.getPayer() == null ? null : transaction.getPayer().getOpenid())
                .successTime(transaction.getSuccessTime())
                .metadata(WechatPayResponseSupport.buildTransactionMetadata(transaction))
                .build();
    }

    /**
     * 查询微信支付退款。
     *
     * @param request 退款查询请求
     * @return 查询响应
     */
    @Override
    public PaymentRefundQueryResponse queryRefund(PaymentRefundQueryRequest request) {
        requireChannel(request.getChannel());
        requireText(request.getRefundRequestNo(), "refundRequestNo must not be blank");
        customizeQueryRefund(request);

        com.wechat.pay.java.service.refund.model.QueryByOutRefundNoRequest queryRequest =
                new com.wechat.pay.java.service.refund.model.QueryByOutRefundNoRequest();
        queryRequest.setOutRefundNo(request.getRefundRequestNo());
        Refund refund = PaymentInvoker.invoke(
                () -> refundService.queryByOutRefundNo(queryRequest),
                "Failed to query WeChat Pay refund"
        );
        return PaymentRefundQueryResponse.builder()
                .channel(PaymentChannel.WECHAT_PAY)
                .merchantOrderNo(refund.getOutTradeNo())
                .refundRequestNo(refund.getOutRefundNo())
                .platformRefundNo(refund.getRefundId())
                .status(WechatPayResponseSupport.enumName(refund.getStatus()))
                .refundAmount(WechatPayResponseSupport.amountFromFen(
                        refund.getAmount() == null ? null : refund.getAmount().getRefund()
                ))
                .currency(refund.getAmount() == null ? null : refund.getAmount().getCurrency())
                .successTime(refund.getSuccessTime())
                .metadata(WechatPayResponseSupport.buildRefundMetadata(refund))
                .build();
    }

    /**
     * 关闭微信支付订单。
     *
     * @param request 关单请求
     * @return 关单响应
     */
    @Override
    public PaymentCloseResponse close(PaymentCloseRequest request) {
        requireChannel(request.getChannel());
        requireText(request.getMerchantOrderNo(), "merchantOrderNo must not be blank");
        customizeClose(request);

        com.wechat.pay.java.service.payments.jsapi.model.CloseOrderRequest closeRequest =
                new com.wechat.pay.java.service.payments.jsapi.model.CloseOrderRequest();
        closeRequest.setMchid(properties.getMerchantId());
        closeRequest.setOutTradeNo(request.getMerchantOrderNo());
        PaymentInvoker.invokeVoid(() -> jsapiService.closeOrder(closeRequest), "Failed to close WeChat Pay order");
        return PaymentCloseResponse.builder()
                .channel(PaymentChannel.WECHAT_PAY)
                .merchantOrderNo(request.getMerchantOrderNo())
                .closed(true)
                .build();
    }

    /**
     * 解析微信支付异步通知。
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

        RequestParam requestParam = buildRequestParam(request.getHeaders(), request.getBody());
        return switch (request.getNotifyType()) {
            case PAYMENT -> parsePaymentNotify(requestParam);
            case REFUND -> parseRefundNotify(requestParam);
        };
    }

    /**
     * 创建 JSAPI/小程序支付订单。
     *
     * @param request 下单请求
     * @param options 请求选项
     * @return 下单响应
     */
    private PaymentCreateResponse createJsapiOrder(PaymentCreateRequest request, WechatPayRequestOptions options) {
        com.wechat.pay.java.service.payments.jsapi.model.PrepayRequest prepayRequest =
                new com.wechat.pay.java.service.payments.jsapi.model.PrepayRequest();
        fillCommonCreateRequest(prepayRequest, request, options);

        com.wechat.pay.java.service.payments.jsapi.model.Payer payer =
                new com.wechat.pay.java.service.payments.jsapi.model.Payer();
        payer.setOpenid(resolvePayerId(request, options));
        prepayRequest.setPayer(payer);

        com.wechat.pay.java.service.payments.jsapi.model.PrepayWithRequestPaymentResponse response = PaymentInvoker.invoke(
                () -> jsapiServiceExtension.prepayWithRequestPayment(prepayRequest),
                "Failed to create WeChat Pay JSAPI order"
        );
        Map<String, String> paymentData = new LinkedHashMap<>();
        paymentData.put("appId", response.getAppId());
        paymentData.put("timeStamp", response.getTimeStamp());
        paymentData.put("nonceStr", response.getNonceStr());
        paymentData.put("package", response.getPackageVal());
        paymentData.put("signType", response.getSignType());
        paymentData.put("paySign", response.getPaySign());
        return PaymentCreateResponse.builder()
                .channel(PaymentChannel.WECHAT_PAY)
                .mode(request.getMode())
                .merchantOrderNo(request.getMerchantOrderNo())
                .prepayId(extractPrepayId(response.getPackageVal()))
                .paymentData(paymentData)
                .metadata(WechatPayResponseSupport.buildBaseMetadata(
                        resolveAppId(request, options),
                        properties.getMerchantId()
                ))
                .build();
    }

    /**
     * 创建 App 支付订单。
     *
     * @param request 下单请求
     * @param options 请求选项
     * @return 下单响应
     */
    private PaymentCreateResponse createAppOrder(PaymentCreateRequest request, WechatPayRequestOptions options) {
        PrepayRequest prepayRequest = new PrepayRequest();
        fillCommonCreateRequest(prepayRequest, request, options);

        com.wechat.pay.java.service.payments.app.model.PrepayWithRequestPaymentResponse response = PaymentInvoker.invoke(
                () -> appServiceExtension.prepayWithRequestPayment(prepayRequest),
                "Failed to create WeChat Pay APP order"
        );
        Map<String, String> paymentData = new LinkedHashMap<>();
        paymentData.put("appId", response.getAppid());
        paymentData.put("partnerId", response.getPartnerId());
        paymentData.put("prepayId", response.getPrepayId());
        paymentData.put("package", response.getPackageVal());
        paymentData.put("nonceStr", response.getNonceStr());
        paymentData.put("timestamp", response.getTimestamp());
        paymentData.put("sign", response.getSign());
        return PaymentCreateResponse.builder()
                .channel(PaymentChannel.WECHAT_PAY)
                .mode(request.getMode())
                .merchantOrderNo(request.getMerchantOrderNo())
                .prepayId(response.getPrepayId())
                .paymentData(paymentData)
                .metadata(WechatPayResponseSupport.buildBaseMetadata(
                        resolveAppId(request, options),
                        properties.getMerchantId()
                ))
                .build();
    }

    /**
     * 创建 H5 支付订单。
     *
     * @param request 下单请求
     * @param options 请求选项
     * @return 下单响应
     */
    private PaymentCreateResponse createH5Order(PaymentCreateRequest request, WechatPayRequestOptions options) {
        com.wechat.pay.java.service.payments.h5.model.PrepayRequest prepayRequest =
                new com.wechat.pay.java.service.payments.h5.model.PrepayRequest();
        fillCommonCreateRequest(prepayRequest, request, options);
        prepayRequest.setSceneInfo(buildH5SceneInfo(request, options));

        com.wechat.pay.java.service.payments.h5.model.PrepayResponse response = PaymentInvoker.invoke(
                () -> h5Service.prepay(prepayRequest),
                "Failed to create WeChat Pay H5 order"
        );
        return PaymentCreateResponse.builder()
                .channel(PaymentChannel.WECHAT_PAY)
                .mode(request.getMode())
                .merchantOrderNo(request.getMerchantOrderNo())
                .payUrl(response.getH5Url())
                .metadata(WechatPayResponseSupport.buildBaseMetadata(
                        resolveAppId(request, options),
                        properties.getMerchantId()
                ))
                .build();
    }

    /**
     * 创建 Native 支付订单。
     *
     * @param request 下单请求
     * @param options 请求选项
     * @return 下单响应
     */
    private PaymentCreateResponse createNativeOrder(PaymentCreateRequest request, WechatPayRequestOptions options) {
        com.wechat.pay.java.service.payments.nativepay.model.PrepayRequest prepayRequest =
                new com.wechat.pay.java.service.payments.nativepay.model.PrepayRequest();
        fillCommonCreateRequest(prepayRequest, request, options);

        com.wechat.pay.java.service.payments.nativepay.model.PrepayResponse response = PaymentInvoker.invoke(
                () -> nativePayService.prepay(prepayRequest),
                "Failed to create WeChat Pay Native order"
        );
        return PaymentCreateResponse.builder()
                .channel(PaymentChannel.WECHAT_PAY)
                .mode(request.getMode())
                .merchantOrderNo(request.getMerchantOrderNo())
                .payUrl(response.getCodeUrl())
                .qrCodeContent(response.getCodeUrl())
                .metadata(WechatPayResponseSupport.buildBaseMetadata(
                        resolveAppId(request, options),
                        properties.getMerchantId()
                ))
                .build();
    }

    /**
     * 填充 JSAPI 预下单公共参数。
     *
     * @param prepayRequest 预下单请求
     * @param request 下单请求
     * @param options 请求选项
     */
    private void fillCommonCreateRequest(
            com.wechat.pay.java.service.payments.jsapi.model.PrepayRequest prepayRequest,
            PaymentCreateRequest request,
            WechatPayRequestOptions options) {
        prepayRequest.setAppid(resolveAppId(request, options));
        prepayRequest.setMchid(properties.getMerchantId());
        prepayRequest.setDescription(resolveDescription(request));
        prepayRequest.setOutTradeNo(request.getMerchantOrderNo());
        prepayRequest.setTimeExpire(request.getTimeExpire());
        prepayRequest.setAttach(resolveAttach(request, options));
        prepayRequest.setNotifyUrl(resolveNotifyUrl(request.getNotifyUrl(), options));
        prepayRequest.setGoodsTag(resolveGoodsTag(request, options));

        com.wechat.pay.java.service.payments.jsapi.model.Amount amount =
                new com.wechat.pay.java.service.payments.jsapi.model.Amount();
        amount.setTotal(WechatPayAmounts.toFen(request.getAmount()));
        amount.setCurrency(request.getCurrency());
        prepayRequest.setAmount(amount);
    }

    /**
     * 填充 App 预下单公共参数。
     *
     * @param prepayRequest 预下单请求
     * @param request 下单请求
     * @param options 请求选项
     */
    private void fillCommonCreateRequest(
            PrepayRequest prepayRequest,
            PaymentCreateRequest request,
            WechatPayRequestOptions options) {
        prepayRequest.setAppid(resolveAppId(request, options));
        prepayRequest.setMchid(properties.getMerchantId());
        prepayRequest.setDescription(resolveDescription(request));
        prepayRequest.setOutTradeNo(request.getMerchantOrderNo());
        prepayRequest.setTimeExpire(request.getTimeExpire());
        prepayRequest.setAttach(resolveAttach(request, options));
        prepayRequest.setNotifyUrl(resolveNotifyUrl(request.getNotifyUrl(), options));
        prepayRequest.setGoodsTag(resolveGoodsTag(request, options));

        com.wechat.pay.java.service.payments.app.model.Amount amount =
                new com.wechat.pay.java.service.payments.app.model.Amount();
        amount.setTotal(WechatPayAmounts.toFen(request.getAmount()));
        amount.setCurrency(request.getCurrency());
        prepayRequest.setAmount(amount);
    }

    /**
     * 填充 H5 预下单公共参数。
     *
     * @param prepayRequest 预下单请求
     * @param request 下单请求
     * @param options 请求选项
     */
    private void fillCommonCreateRequest(
            com.wechat.pay.java.service.payments.h5.model.PrepayRequest prepayRequest,
            PaymentCreateRequest request,
            WechatPayRequestOptions options) {
        prepayRequest.setAppid(resolveAppId(request, options));
        prepayRequest.setMchid(properties.getMerchantId());
        prepayRequest.setDescription(resolveDescription(request));
        prepayRequest.setOutTradeNo(request.getMerchantOrderNo());
        prepayRequest.setTimeExpire(request.getTimeExpire());
        prepayRequest.setAttach(resolveAttach(request, options));
        prepayRequest.setNotifyUrl(resolveNotifyUrl(request.getNotifyUrl(), options));
        prepayRequest.setGoodsTag(resolveGoodsTag(request, options));

        com.wechat.pay.java.service.payments.h5.model.Amount amount =
                new com.wechat.pay.java.service.payments.h5.model.Amount();
        amount.setTotal(WechatPayAmounts.toFen(request.getAmount()));
        amount.setCurrency(request.getCurrency());
        prepayRequest.setAmount(amount);
    }

    /**
     * 填充 Native 预下单公共参数。
     *
     * @param prepayRequest 预下单请求
     * @param request 下单请求
     * @param options 请求选项
     */
    private void fillCommonCreateRequest(
            com.wechat.pay.java.service.payments.nativepay.model.PrepayRequest prepayRequest,
            PaymentCreateRequest request,
            WechatPayRequestOptions options) {
        prepayRequest.setAppid(resolveAppId(request, options));
        prepayRequest.setMchid(properties.getMerchantId());
        prepayRequest.setDescription(resolveDescription(request));
        prepayRequest.setOutTradeNo(request.getMerchantOrderNo());
        prepayRequest.setTimeExpire(request.getTimeExpire());
        prepayRequest.setAttach(resolveAttach(request, options));
        prepayRequest.setNotifyUrl(resolveNotifyUrl(request.getNotifyUrl(), options));
        prepayRequest.setGoodsTag(resolveGoodsTag(request, options));

        com.wechat.pay.java.service.payments.nativepay.model.Amount amount =
                new com.wechat.pay.java.service.payments.nativepay.model.Amount();
        amount.setTotal(WechatPayAmounts.toFen(request.getAmount()));
        amount.setCurrency(request.getCurrency());
        prepayRequest.setAmount(amount);
    }

    /**
     * 构建 H5 支付场景信息。
     *
     * @param request 下单请求
     * @param options 请求选项
     * @return 场景信息
     */
    private com.wechat.pay.java.service.payments.h5.model.SceneInfo buildH5SceneInfo(
            PaymentCreateRequest request,
            WechatPayRequestOptions options) {
        com.wechat.pay.java.service.payments.h5.model.SceneInfo sceneInfo =
                new com.wechat.pay.java.service.payments.h5.model.SceneInfo();
        sceneInfo.setPayerClientIp(resolveClientIp(request, options));
        sceneInfo.setDeviceId(WechatPayRequestSupport.text(request.getMetadata(), "deviceId"));

        com.wechat.pay.java.service.payments.h5.model.H5Info h5Info =
                new com.wechat.pay.java.service.payments.h5.model.H5Info();
        h5Info.setType(resolveText(WechatPayRequestSupport.text(request.getMetadata(), "h5Type"), "Wap"));
        h5Info.setAppName(WechatPayRequestSupport.text(request.getMetadata(), "appName"));
        h5Info.setAppUrl(WechatPayRequestSupport.text(request.getMetadata(), "appUrl"));
        h5Info.setBundleId(WechatPayRequestSupport.text(request.getMetadata(), "bundleId"));
        h5Info.setPackageName(WechatPayRequestSupport.text(request.getMetadata(), "packageName"));
        sceneInfo.setH5Info(h5Info);
        return sceneInfo;
    }

    /**
     * 查询微信支付交易。
     *
     * @param request 订单查询请求
     * @param options 请求选项
     * @return 交易对象
     */
    private Transaction queryTransaction(PaymentOrderQueryRequest request, WechatPayRequestOptions options) {
        if (StringUtils.hasText(request.getPlatformOrderNo())) {
            com.wechat.pay.java.service.payments.jsapi.model.QueryOrderByIdRequest queryRequest =
                    new com.wechat.pay.java.service.payments.jsapi.model.QueryOrderByIdRequest();
            queryRequest.setMchid(properties.getMerchantId());
            queryRequest.setTransactionId(request.getPlatformOrderNo());
            return PaymentInvoker.invoke(() -> jsapiService.queryOrderById(queryRequest), "Failed to query WeChat Pay order");
        }
        requireText(request.getMerchantOrderNo(), "merchantOrderNo or platformOrderNo must not be blank");
        com.wechat.pay.java.service.payments.jsapi.model.QueryOrderByOutTradeNoRequest queryRequest =
                new com.wechat.pay.java.service.payments.jsapi.model.QueryOrderByOutTradeNoRequest();
        queryRequest.setMchid(properties.getMerchantId());
        queryRequest.setOutTradeNo(request.getMerchantOrderNo());
        return PaymentInvoker.invoke(
                () -> jsapiService.queryOrderByOutTradeNo(queryRequest),
                "Failed to query WeChat Pay order"
        );
    }

    /**
     * 解析支付通知。
     *
     * @param requestParam 通知参数
     * @return 通知响应
     */
    private PaymentNotifyResponse parsePaymentNotify(RequestParam requestParam) {
        Transaction transaction = PaymentInvoker.invoke(
                () -> notificationParser.parse(requestParam, Transaction.class),
                "Failed to parse WeChat Pay payment notification"
        );
        Map<String, String> metadata = WechatPayResponseSupport.buildTransactionMetadata(transaction);
        return PaymentNotifyResponse.builder()
                .channel(PaymentChannel.WECHAT_PAY)
                .notifyType(PaymentNotifyType.PAYMENT)
                .merchantOrderNo(transaction.getOutTradeNo())
                .platformOrderNo(transaction.getTransactionId())
                .status(WechatPayResponseSupport.enumName(transaction.getTradeState()))
                .success(Transaction.TradeStateEnum.SUCCESS.equals(transaction.getTradeState()))
                .eventTime(transaction.getSuccessTime())
                .metadata(metadata)
                .build();
    }

    /**
     * 解析退款通知。
     *
     * @param requestParam 通知参数
     * @return 通知响应
     */
    private PaymentNotifyResponse parseRefundNotify(RequestParam requestParam) {
        RefundNotification refundNotification = PaymentInvoker.invoke(
                () -> notificationParser.parse(requestParam, RefundNotification.class),
                "Failed to parse WeChat Pay refund notification"
        );
        Map<String, String> metadata = WechatPayResponseSupport.buildRefundNotificationMetadata(refundNotification);
        String refundStatus = WechatPayResponseSupport.enumName(refundNotification.getRefundStatus());
        return PaymentNotifyResponse.builder()
                .channel(PaymentChannel.WECHAT_PAY)
                .notifyType(PaymentNotifyType.REFUND)
                .merchantOrderNo(refundNotification.getOutTradeNo())
                .platformOrderNo(refundNotification.getTransactionId())
                .refundRequestNo(refundNotification.getOutRefundNo())
                .status(refundStatus)
                .success("SUCCESS".equals(refundStatus))
                .eventTime(refundNotification.getSuccessTime())
                .metadata(metadata)
                .build();
    }

    /**
     * 构建通知解析参数。
     *
     * @param headers 通知请求头
     * @param body 通知请求体
     * @return 解析参数
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
     * 校验下单请求。
     *
     * @param request 下单请求
     * @param mode 支付方式
     * @param options 请求选项
     */
    private void validateCreateRequest(
            PaymentCreateRequest request,
            PaymentMode mode,
            WechatPayRequestOptions options) {
        requireText(request.getMerchantOrderNo(), "merchantOrderNo must not be blank");
        requireAmount(request.getAmount(), "amount must not be null");
        requireText(resolveDescription(request), "description and subject must not both be blank");
        requireText(request.getCurrency(), "currency must not be blank");

        if (EnumSet.of(PaymentMode.JSAPI, PaymentMode.MINI_PROGRAM).contains(mode)) {
            requireText(resolvePayerId(request, options), "payerId must not be blank for WeChat JSAPI/Mini Program");
        }
        if (PaymentMode.H5.equals(mode)) {
            requireText(resolveClientIp(request, options), "clientIp must not be blank for WeChat H5");
        }
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
        return new BusinessException("Unsupported WeChat Pay mode: " + mode);
    }

    /**
     * 校验渠道是否为微信支付。
     *
     * @param channel 支付渠道
     */
    private void requireChannel(PaymentChannel channel) {
        if (!PaymentChannel.WECHAT_PAY.equals(channel)) {
            throw new BusinessException("Unsupported channel for WeChat Pay service: " + channel);
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

    /**
     * 解析下单使用的 AppId。
     *
     * @param request 下单请求
     * @param options 请求选项
     * @return AppId
     */
    private String resolveAppId(PaymentCreateRequest request, WechatPayRequestOptions options) {
        return resolveText(options.getAppId(), resolveText(request.getAppId(), properties.getAppId()));
    }

    /**
     * 解析订单描述。
     *
     * @param request 下单请求
     * @return 订单描述
     */
    private String resolveDescription(PaymentCreateRequest request) {
        return resolveText(request.getDescription(), request.getSubject());
    }

    /**
     * 解析通知地址。
     *
     * @param requestNotifyUrl 请求中的通知地址
     * @param options 请求选项
     * @return 通知地址
     */
    private String resolveNotifyUrl(String requestNotifyUrl, WechatPayRequestOptions options) {
        return resolveText(options.getNotifyUrl(), resolveText(requestNotifyUrl, properties.getNotifyUrl()));
    }

    /**
     * 解析附加数据。
     *
     * @param request 下单请求
     * @param options 请求选项
     * @return 附加数据
     */
    private String resolveAttach(PaymentCreateRequest request, WechatPayRequestOptions options) {
        return resolveText(options.getAttach(), WechatPayRequestSupport.text(request.getMetadata(), "attach"));
    }

    /**
     * 解析商品标记。
     *
     * @param request 下单请求
     * @param options 请求选项
     * @return 商品标记
     */
    private String resolveGoodsTag(PaymentCreateRequest request, WechatPayRequestOptions options) {
        return resolveText(options.getGoodsTag(), WechatPayRequestSupport.text(request.getMetadata(), "goodsTag"));
    }

    /**
     * 解析付款人标识。
     *
     * @param request 下单请求
     * @param options 请求选项
     * @return 付款人标识
     */
    private String resolvePayerId(PaymentCreateRequest request, WechatPayRequestOptions options) {
        return resolveText(options.getPayerId(), request.getPayerId());
    }

    /**
     * 解析客户端 IP。
     *
     * @param request 下单请求
     * @param options 请求选项
     * @return 客户端 IP
     */
    private String resolveClientIp(PaymentCreateRequest request, WechatPayRequestOptions options) {
        return resolveText(options.getClientIp(), request.getClientIp());
    }

    /**
     * 解析优先值与回退值。
     *
     * @param preferred 优先值
     * @param fallback 回退值
     * @return 最终值
     */
    private String resolveText(String preferred, String fallback) {
        return StringUtils.hasText(preferred) ? preferred : fallback;
    }

    /**
     * 解析订单支付方式。
     *
     * @param mode 显式支付方式
     * @param transaction 交易对象
     * @return 支付方式
     */
    private PaymentMode resolveMode(PaymentMode mode, Transaction transaction) {
        if (mode != null) {
            return mode;
        }
        if (transaction == null || transaction.getTradeType() == null) {
            return null;
        }
        return switch (transaction.getTradeType()) {
            case JSAPI -> PaymentMode.JSAPI;
            case APP -> PaymentMode.APP;
            case NATIVE -> PaymentMode.NATIVE;
            case MWEB -> PaymentMode.H5;
            default -> null;
        };
    }

    /**
     * 从预支付包中提取预支付标识。
     *
     * @param packageValue 预支付包
     * @return 预支付标识
     */
    private String extractPrepayId(String packageValue) {
        if (!StringUtils.hasText(packageValue)) {
            return null;
        }
        int index = packageValue.indexOf("prepay_id=");
        return index >= 0 ? packageValue.substring(index + "prepay_id=".length()) : packageValue;
    }

    /**
     * 执行下单请求定制。
     *
     * @param request 下单请求
     * @return 请求选项
     */
    private WechatPayRequestOptions customizeCreate(PaymentCreateRequest request) {
        WechatPayRequestOptions options = new WechatPayRequestOptions();
        for (WechatPayRequestCustomizer requestCustomizer : requestCustomizers) {
            requestCustomizer.customizeCreate(request, options);
        }
        return options;
    }

    /**
     * 执行退款请求定制。
     *
     * @param request 退款请求
     * @return 请求选项
     */
    private WechatPayRequestOptions customizeRefund(PaymentRefundRequest request) {
        WechatPayRequestOptions options = new WechatPayRequestOptions();
        for (WechatPayRequestCustomizer requestCustomizer : requestCustomizers) {
            requestCustomizer.customizeRefund(request, options);
        }
        return options;
    }

    /**
     * 执行订单查询请求定制。
     *
     * @param request 订单查询请求
     * @return 请求选项
     */
    private WechatPayRequestOptions customizeQueryOrder(PaymentOrderQueryRequest request) {
        WechatPayRequestOptions options = new WechatPayRequestOptions();
        for (WechatPayRequestCustomizer requestCustomizer : requestCustomizers) {
            requestCustomizer.customizeQueryOrder(request, options);
        }
        return options;
    }

    /**
     * 执行退款查询请求定制。
     *
     * @param request 退款查询请求
     */
    private void customizeQueryRefund(PaymentRefundQueryRequest request) {
        WechatPayRequestOptions options = new WechatPayRequestOptions();
        for (WechatPayRequestCustomizer requestCustomizer : requestCustomizers) {
            requestCustomizer.customizeQueryRefund(request, options);
        }
    }

    /**
     * 执行关单请求定制。
     *
     * @param request 关单请求
     */
    private void customizeClose(PaymentCloseRequest request) {
        WechatPayRequestOptions options = new WechatPayRequestOptions();
        for (WechatPayRequestCustomizer requestCustomizer : requestCustomizers) {
            requestCustomizer.customizeClose(request, options);
        }
    }
}
