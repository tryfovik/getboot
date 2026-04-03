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
package com.getboot.payment.infrastructure.alipay.trade.facetoface;

import com.alipay.easysdk.payment.facetoface.models.AlipayTradePayResponse;
import com.getboot.exception.api.exception.BusinessException;
import com.getboot.payment.api.alipay.trade.facetoface.AlipayFaceToFacePayRequest;
import com.getboot.payment.api.alipay.trade.facetoface.AlipayFaceToFacePayResponse;
import com.getboot.payment.api.alipay.trade.facetoface.AlipayFaceToFaceService;
import com.getboot.payment.api.properties.PaymentProperties;
import com.getboot.payment.spi.alipay.AlipayRequestCustomizer;
import com.getboot.payment.spi.alipay.AlipayRequestOptions;
import com.getboot.payment.support.PaymentInvoker;
import com.getboot.payment.support.alipay.AlipayGateway;
import com.getboot.payment.support.alipay.AlipayRequestSupport;
import com.getboot.payment.support.alipay.AlipayResponseSupport;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * 支付宝当面付服务实现。
 *
 * @author qiheng
 */
public class AlipayFaceToFaceServiceImpl implements AlipayFaceToFaceService {

    /**
     * 支付宝渠道配置。
     */
    private final PaymentProperties.Alipay properties;

    /**
     * 支付宝 SDK 网关。
     */
    private final AlipayGateway gateway;

    /**
     * 请求扩展器集合。
     */
    private final List<AlipayRequestCustomizer> requestCustomizers;

    /**
     * 构造当面付服务。
     *
     * @param paymentProperties 支付配置
     * @param gateway           SDK 网关
     */
    public AlipayFaceToFaceServiceImpl(PaymentProperties paymentProperties, AlipayGateway gateway) {
        this(paymentProperties, gateway, List.of());
    }

    /**
     * 构造当面付服务。
     *
     * @param paymentProperties 支付配置
     * @param gateway           SDK 网关
     * @param requestCustomizers 请求扩展器
     */
    public AlipayFaceToFaceServiceImpl(
            PaymentProperties paymentProperties,
            AlipayGateway gateway,
            List<AlipayRequestCustomizer> requestCustomizers) {
        this.properties = paymentProperties.getAlipay();
        this.gateway = gateway;
        this.requestCustomizers = requestCustomizers == null ? List.of() : List.copyOf(requestCustomizers);
    }

    /**
     * 发起支付宝当面付。
     *
     * @param request 当面付请求
     * @return 当面付响应
     */
    @Override
    public AlipayFaceToFacePayResponse pay(AlipayFaceToFacePayRequest request) {
        if (request == null) {
            throw new BusinessException("request must not be null");
        }
        requireText(request.getMerchantOrderNo(), "merchantOrderNo must not be blank");
        requireText(request.getSubject(), "subject must not be blank");
        requireText(request.getAuthCode(), "authCode must not be blank");
        requireAmount(request.getAmount(), "amount must not be null");

        AlipayRequestOptions options = buildOptions(request);
        AlipayTradePayResponse response = PaymentInvoker.invoke(
                () -> gateway.facePay(
                        request.getSubject(),
                        request.getMerchantOrderNo(),
                        toAmountText(request.getAmount()),
                        request.getAuthCode(),
                        options.getNotifyUrl(),
                        options.getOptionalArgs(),
                        AlipayRequestSupport.resolveContext(options)
                ),
                "Failed to pay Alipay face-to-face order"
        );
        AlipayResponseSupport.ensureSuccess(response, "Failed to pay Alipay face-to-face order");
        return AlipayFaceToFacePayResponse.builder()
                .merchantOrderNo(response.getOutTradeNo())
                .platformOrderNo(response.getTradeNo())
                .status("TRADE_SUCCESS")
                .paidAmount(AlipayResponseSupport.toBigDecimal(
                        AlipayResponseSupport.firstNonBlank(response.getBuyerPayAmount(), response.getTotalAmount())
                ))
                .currency(AlipayResponseSupport.firstNonBlank(
                        response.getPayCurrency(),
                        response.getTransCurrency(),
                        response.getSettleCurrency()
                ))
                .payerId(response.getBuyerUserId())
                .successTime(response.getGmtPayment())
                .metadata(AlipayResponseSupport.extractMetadata(response))
                .build();
    }

    /**
     * 构建当面付请求选项。
     *
     * @param request 当面付请求
     * @return 请求选项
     */
    private AlipayRequestOptions buildOptions(AlipayFaceToFacePayRequest request) {
        AlipayRequestOptions options = newRequestOptions(request.getMetadata());
        options.setNotifyUrl(resolveText(request.getNotifyUrl(), properties.getNotifyUrl()));
        options.putOptionalArg("body", request.getDescription());
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
        String serviceProviderId = AlipayRequestSupport.text(request.getMetadata(), AlipayRequestSupport.SERVICE_PROVIDER_ID);
        if (StringUtils.hasText(serviceProviderId)) {
            options.putOptionalArg("extend_params", java.util.Map.of("sys_service_provider_id", serviceProviderId));
        }
        for (AlipayRequestCustomizer customizer : requestCustomizers) {
            customizer.customizeFaceToFacePay(request, options);
        }
        return options;
    }

    /**
     * 根据元数据创建请求选项。
     *
     * @param metadata 元数据
     * @return 请求选项
     */
    private AlipayRequestOptions newRequestOptions(java.util.Map<String, String> metadata) {
        AlipayRequestOptions options = new AlipayRequestOptions();
        options.setAppAuthToken(AlipayRequestSupport.text(metadata, AlipayRequestSupport.APP_AUTH_TOKEN));
        options.setAuthToken(AlipayRequestSupport.text(metadata, AlipayRequestSupport.AUTH_TOKEN));
        options.setRoute(AlipayRequestSupport.text(metadata, AlipayRequestSupport.ROUTE));
        return options;
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
