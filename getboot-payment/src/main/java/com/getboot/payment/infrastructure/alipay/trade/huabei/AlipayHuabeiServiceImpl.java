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
package com.getboot.payment.infrastructure.alipay.trade.huabei;

import com.alipay.easysdk.payment.huabei.models.HuabeiConfig;
import com.getboot.exception.api.exception.BusinessException;
import com.getboot.payment.api.alipay.trade.huabei.AlipayHuabeiCreateRequest;
import com.getboot.payment.api.alipay.trade.huabei.AlipayHuabeiCreateResponse;
import com.getboot.payment.api.alipay.trade.huabei.AlipayHuabeiService;
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
 * 支付宝花呗分期服务实现。
 *
 * @author qiheng
 */
public class AlipayHuabeiServiceImpl implements AlipayHuabeiService {

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
     * 构造花呗分期服务。
     *
     * @param paymentProperties 支付配置
     * @param gateway           SDK 网关
     */
    public AlipayHuabeiServiceImpl(PaymentProperties paymentProperties, AlipayGateway gateway) {
        this(paymentProperties, gateway, List.of());
    }

    /**
     * 构造花呗分期服务。
     *
     * @param paymentProperties 支付配置
     * @param gateway           SDK 网关
     * @param requestCustomizers 请求扩展器
     */
    public AlipayHuabeiServiceImpl(
            PaymentProperties paymentProperties,
            AlipayGateway gateway,
            List<AlipayRequestCustomizer> requestCustomizers) {
        this.properties = paymentProperties.getAlipay();
        this.gateway = gateway;
        this.requestCustomizers = requestCustomizers == null ? List.of() : List.copyOf(requestCustomizers);
    }

    /**
     * 创建支付宝花呗分期交易。
     *
     * @param request 花呗分期请求
     * @return 花呗分期响应
     */
    @Override
    public AlipayHuabeiCreateResponse create(AlipayHuabeiCreateRequest request) {
        if (request == null) {
            throw new BusinessException("request must not be null");
        }
        requireText(request.getMerchantOrderNo(), "merchantOrderNo must not be blank");
        requireText(request.getSubject(), "subject must not be blank");
        requireText(request.getPayerId(), "payerId must not be blank");
        requireText(request.getHbFqNum(), "hbFqNum must not be blank");
        requireText(request.getHbFqSellerPercent(), "hbFqSellerPercent must not be blank");
        requireAmount(request.getAmount(), "amount must not be null");

        AlipayRequestOptions options = buildOptions(request);
        HuabeiConfig config = new HuabeiConfig();
        config.setHbFqNum(request.getHbFqNum());
        config.setHbFqSellerPercent(request.getHbFqSellerPercent());

        com.alipay.easysdk.payment.huabei.models.AlipayTradeCreateResponse response = PaymentInvoker.invoke(
                () -> gateway.huabeiCreate(
                        request.getSubject(),
                        request.getMerchantOrderNo(),
                        toAmountText(request.getAmount()),
                        request.getPayerId(),
                        config,
                        options.getNotifyUrl(),
                        options.getOptionalArgs(),
                        AlipayRequestSupport.resolveContext(options)
                ),
                "Failed to create Alipay Huabei trade"
        );
        AlipayResponseSupport.ensureSuccess(response, "Failed to create Alipay Huabei trade");
        return AlipayHuabeiCreateResponse.builder()
                .merchantOrderNo(response.getOutTradeNo())
                .platformOrderNo(response.getTradeNo())
                .status("CREATED")
                .metadata(AlipayResponseSupport.extractMetadata(response))
                .build();
    }

    /**
     * 构建花呗分期请求选项。
     *
     * @param request 花呗分期请求
     * @return 请求选项
     */
    private AlipayRequestOptions buildOptions(AlipayHuabeiCreateRequest request) {
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
        for (AlipayRequestCustomizer customizer : requestCustomizers) {
            customizer.customizeHuabeiCreate(request, options);
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
