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
package com.getboot.payment.infrastructure.autoconfigure;

import com.alipay.easysdk.factory.Factory;
import com.alipay.easysdk.kernel.Config;
import com.alipay.easysdk.kernel.Context;
import com.getboot.payment.api.alipay.AlipayApiService;
import com.getboot.payment.api.alipay.settlement.AlipaySettlementService;
import com.getboot.payment.api.alipay.trade.facetoface.AlipayFaceToFaceService;
import com.getboot.payment.api.alipay.trade.huabei.AlipayHuabeiService;
import com.getboot.payment.api.alipay.trade.AlipayTradeService;
import com.getboot.payment.api.properties.PaymentProperties;
import com.getboot.payment.api.service.PaymentService;
import com.getboot.payment.infrastructure.alipay.AlipayApiServiceImpl;
import com.getboot.payment.infrastructure.alipay.AlipayPaymentService;
import com.getboot.payment.infrastructure.alipay.settlement.AlipaySettlementServiceImpl;
import com.getboot.payment.infrastructure.alipay.trade.facetoface.AlipayFaceToFaceServiceImpl;
import com.getboot.payment.infrastructure.alipay.trade.huabei.AlipayHuabeiServiceImpl;
import com.getboot.payment.infrastructure.alipay.trade.AlipayTradeServiceImpl;
import com.getboot.payment.spi.alipay.AlipayRequestCustomizer;
import com.getboot.payment.support.alipay.AlipayGateway;
import com.getboot.payment.support.alipay.DefaultAlipayGateway;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * 支付宝官方 Easy SDK 自动配置。
 *
 * @author qiheng
 */
@AutoConfiguration(after = PaymentAutoConfiguration.class)
@ConditionalOnClass(Config.class)
@ConditionalOnProperty(prefix = "getboot.payment.alipay", name = "enabled", havingValue = "true")
public class AlipayPaymentAutoConfiguration {

    /**
     * 支付宝默认生产网关域名。
     */
    private static final String DEFAULT_GATEWAY_HOST = "openapi.alipay.com";

    /**
     * 支付宝默认沙箱网关域名。
     */
    private static final String DEFAULT_SANDBOX_GATEWAY_HOST = "openapi-sandbox.dl.alipaydev.com";

    /**
     * 注册支付宝 SDK 配置。
     *
     * @param paymentProperties 支付配置
     * @return SDK 配置
     */
    @Bean
    @ConditionalOnMissingBean
    public Config alipayConfig(PaymentProperties paymentProperties) {
        PaymentProperties.Alipay properties = paymentProperties.getAlipay();
        validateAlipayProperties(properties);

        Config config = new Config();
        config.protocol = StringUtils.hasText(properties.getProtocol()) ? properties.getProtocol() : "https";
        config.gatewayHost = resolveGatewayHost(properties);
        config.signType = StringUtils.hasText(properties.getSignType()) ? properties.getSignType() : "RSA2";
        config.appId = properties.getAppId();
        config.merchantPrivateKey = properties.getMerchantPrivateKey();
        config.notifyUrl = properties.getNotifyUrl();

        if (useCertificateMode(properties)) {
            config.merchantCertPath = properties.getMerchantCertPath();
            config.alipayCertPath = properties.getAlipayCertPath();
            config.alipayRootCertPath = properties.getAlipayRootCertPath();
        } else {
            config.alipayPublicKey = properties.getAlipayPublicKey();
        }
        return config;
    }

    /**
     * 注册支付宝 SDK 上下文。
     *
     * @param config SDK 配置
     * @return SDK 上下文
     * @throws Exception 初始化异常
     */
    @Bean
    @ConditionalOnMissingBean
    public Context alipayContext(Config config) throws Exception {
        return new Context(config, Factory.SDK_VERSION);
    }

    /**
     * 注册支付宝 SDK 网关。
     *
     * @param context SDK 上下文
     * @return SDK 网关
     */
    @Bean
    @ConditionalOnMissingBean
    public AlipayGateway alipayGateway(Context context) {
        return new DefaultAlipayGateway(context);
    }

    /**
     * 注册支付宝统一支付服务。
     *
     * @param paymentProperties 支付配置
     * @param gateway           SDK 网关
     * @return 统一支付服务
     */
    @Bean
    @ConditionalOnMissingBean(name = "alipayPaymentService")
    public PaymentService alipayPaymentService(
            PaymentProperties paymentProperties,
            AlipayGateway gateway,
            ObjectProvider<AlipayRequestCustomizer> requestCustomizers) {
        return new AlipayPaymentService(paymentProperties, gateway, requestCustomizers.orderedStream().toList());
    }

    /**
     * 注册支付宝开放接口访问入口。
     *
     * @param gateway SDK 网关
     * @return 开放接口访问入口
     */
    @Bean
    @ConditionalOnMissingBean
    public AlipayApiService alipayApiService(AlipayGateway gateway) {
        return new AlipayApiServiceImpl(gateway);
    }

    /**
     * 注册支付宝交易增强服务。
     *
     * @param gateway SDK 网关
     * @return 交易增强服务
     */
    @Bean
    @ConditionalOnMissingBean
    public AlipayTradeService alipayTradeService(
            AlipayGateway gateway,
            ObjectProvider<AlipayRequestCustomizer> requestCustomizers) {
        return new AlipayTradeServiceImpl(gateway, requestCustomizers.orderedStream().toList());
    }

    /**
     * 注册支付宝结算能力服务。
     *
     * @param gateway            SDK 网关
     * @param requestCustomizers 请求扩展器
     * @return 结算能力服务
     */
    @Bean
    @ConditionalOnMissingBean
    public AlipaySettlementService alipaySettlementService(
            AlipayGateway gateway,
            ObjectProvider<AlipayRequestCustomizer> requestCustomizers) {
        return new AlipaySettlementServiceImpl(gateway, requestCustomizers.orderedStream().toList());
    }

    /**
     * 注册支付宝当面付服务。
     *
     * @param paymentProperties 支付配置
     * @param gateway           SDK 网关
     * @param requestCustomizers 请求扩展器
     * @return 当面付服务
     */
    @Bean
    @ConditionalOnMissingBean
    public AlipayFaceToFaceService alipayFaceToFaceService(
            PaymentProperties paymentProperties,
            AlipayGateway gateway,
            ObjectProvider<AlipayRequestCustomizer> requestCustomizers) {
        return new AlipayFaceToFaceServiceImpl(
                paymentProperties,
                gateway,
                requestCustomizers.orderedStream().toList()
        );
    }

    /**
     * 注册支付宝花呗分期服务。
     *
     * @param paymentProperties 支付配置
     * @param gateway           SDK 网关
     * @param requestCustomizers 请求扩展器
     * @return 花呗分期服务
     */
    @Bean
    @ConditionalOnMissingBean
    public AlipayHuabeiService alipayHuabeiService(
            PaymentProperties paymentProperties,
            AlipayGateway gateway,
            ObjectProvider<AlipayRequestCustomizer> requestCustomizers) {
        return new AlipayHuabeiServiceImpl(
                paymentProperties,
                gateway,
                requestCustomizers.orderedStream().toList()
        );
    }

    /**
     * 校验支付宝配置是否完整。
     *
     * @param properties 支付宝配置
     */
    private void validateAlipayProperties(PaymentProperties.Alipay properties) {
        Assert.hasText(properties.getAppId(), "getboot.payment.alipay.app-id must not be blank");
        Assert.hasText(
                properties.getMerchantPrivateKey(),
                "getboot.payment.alipay.merchant-private-key must not be blank"
        );
        if (useCertificateMode(properties)) {
            Assert.isTrue(
                    StringUtils.hasText(properties.getMerchantCertPath())
                            && StringUtils.hasText(properties.getAlipayCertPath())
                            && StringUtils.hasText(properties.getAlipayRootCertPath()),
                    "merchant-cert-path, alipay-cert-path and alipay-root-cert-path must be configured together"
            );
        } else {
            Assert.hasText(
                    properties.getAlipayPublicKey(),
                    "getboot.payment.alipay.alipay-public-key must not be blank when certificate mode is disabled"
            );
        }
    }

    /**
     * 判断是否使用证书模式。
     *
     * @param properties 支付宝配置
     * @return 是否使用证书模式
     */
    private boolean useCertificateMode(PaymentProperties.Alipay properties) {
        return StringUtils.hasText(properties.getMerchantCertPath())
                || StringUtils.hasText(properties.getAlipayCertPath())
                || StringUtils.hasText(properties.getAlipayRootCertPath());
    }

    /**
     * 解析支付宝网关域名。
     *
     * @param properties 支付宝配置
     * @return 网关域名
     */
    private String resolveGatewayHost(PaymentProperties.Alipay properties) {
        if (StringUtils.hasText(properties.getGatewayHost())) {
            return properties.getGatewayHost();
        }
        return properties.isSandbox() ? DEFAULT_SANDBOX_GATEWAY_HOST : DEFAULT_GATEWAY_HOST;
    }
}
