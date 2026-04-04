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

import com.alipay.easysdk.kernel.Config;
import com.getboot.payment.api.alipay.AlipayApiService;
import com.getboot.payment.api.alipay.settlement.AlipaySettlementService;
import com.getboot.payment.api.alipay.trade.AlipayTradeService;
import com.getboot.payment.api.alipay.trade.facetoface.AlipayFaceToFaceService;
import com.getboot.payment.api.alipay.trade.huabei.AlipayHuabeiService;
import com.getboot.payment.api.model.PaymentChannel;
import com.getboot.payment.api.model.PaymentMode;
import com.getboot.payment.api.properties.PaymentProperties;
import com.getboot.payment.api.service.PaymentService;
import com.getboot.payment.infrastructure.alipay.AlipayApiServiceImpl;
import com.getboot.payment.infrastructure.alipay.AlipayPaymentService;
import com.getboot.payment.infrastructure.alipay.settlement.AlipaySettlementServiceImpl;
import com.getboot.payment.infrastructure.alipay.trade.AlipayTradeServiceImpl;
import com.getboot.payment.infrastructure.alipay.trade.facetoface.AlipayFaceToFaceServiceImpl;
import com.getboot.payment.infrastructure.alipay.trade.huabei.AlipayHuabeiServiceImpl;
import com.getboot.payment.spi.alipay.AlipayRequestCustomizer;
import com.getboot.payment.support.alipay.AlipayGateway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.support.StaticListableBeanFactory;

import java.lang.reflect.Proxy;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 支付宝自动配置映射测试。
 *
 * @author qiheng
 */
class AlipayPaymentAutoConfigurationTest {

    /**
     * 验证公钥模式配置会使用默认协议、签名算法和生产网关。
     */
    @Test
    void shouldCreateDefaultConfigForPublicKeyMode() {
        Config config = new AlipayPaymentAutoConfiguration().alipayConfig(paymentProperties());

        assertEquals("https", config.protocol);
        assertEquals("openapi.alipay.com", config.gatewayHost);
        assertEquals("RSA2", config.signType);
        assertEquals("2026000000000001", config.appId);
        assertEquals("merchant-private-key", config.merchantPrivateKey);
        assertEquals("alipay-public-key", config.alipayPublicKey);
    }

    /**
     * 验证证书模式会回填证书路径并按沙箱环境切换默认网关。
     */
    @Test
    void shouldCreateCertificateModeConfigWithSandboxGateway() {
        PaymentProperties paymentProperties = paymentProperties();
        paymentProperties.getAlipay().setSandbox(true);
        paymentProperties.getAlipay().setAlipayPublicKey(null);
        paymentProperties.getAlipay().setMerchantCertPath("classpath:payment/alipay/appCertPublicKey.crt");
        paymentProperties.getAlipay().setAlipayCertPath("classpath:payment/alipay/alipayCertPublicKey_RSA2.crt");
        paymentProperties.getAlipay().setAlipayRootCertPath("classpath:payment/alipay/alipayRootCert.crt");

        Config config = new AlipayPaymentAutoConfiguration().alipayConfig(paymentProperties);

        assertEquals("openapi-sandbox.dl.alipaydev.com", config.gatewayHost);
        assertEquals("classpath:payment/alipay/appCertPublicKey.crt", config.merchantCertPath);
        assertEquals("classpath:payment/alipay/alipayCertPublicKey_RSA2.crt", config.alipayCertPath);
        assertEquals("classpath:payment/alipay/alipayRootCert.crt", config.alipayRootCertPath);
    }

    /**
     * 验证证书模式只配置部分证书路径时会拒绝启动。
     */
    @Test
    void shouldRejectIncompleteCertificateModeConfiguration() {
        PaymentProperties paymentProperties = paymentProperties();
        paymentProperties.getAlipay().setAlipayPublicKey(null);
        paymentProperties.getAlipay().setMerchantCertPath("classpath:payment/alipay/appCertPublicKey.crt");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new AlipayPaymentAutoConfiguration().alipayConfig(paymentProperties)
        );

        assertEquals(
                "merchant-cert-path, alipay-cert-path and alipay-root-cert-path must be configured together",
                exception.getMessage()
        );
    }

    /**
     * 验证支付宝自动配置会映射到默认实现。
     */
    @Test
    void shouldMapAlipayServiceBeansToDefaultImplementations() {
        AlipayPaymentAutoConfiguration autoConfiguration = new AlipayPaymentAutoConfiguration();
        PaymentProperties paymentProperties = paymentProperties();
        ObjectProvider<AlipayRequestCustomizer> customizers =
                new StaticListableBeanFactory().getBeanProvider(AlipayRequestCustomizer.class);
        AlipayGateway gateway = noopGateway();

        PaymentService paymentService = autoConfiguration.alipayPaymentService(paymentProperties, gateway, customizers);
        assertInstanceOf(AlipayPaymentService.class, paymentService);
        assertEquals(PaymentChannel.ALIPAY, paymentService.channel());
        assertTrue(paymentService.supportedModes().containsAll(Set.of(
                PaymentMode.APP,
                PaymentMode.PAGE,
                PaymentMode.WAP,
                PaymentMode.NATIVE
        )));

        AlipayApiService apiService = autoConfiguration.alipayApiService(gateway);
        assertInstanceOf(AlipayApiServiceImpl.class, apiService);

        AlipayTradeService tradeService = autoConfiguration.alipayTradeService(gateway, customizers);
        assertInstanceOf(AlipayTradeServiceImpl.class, tradeService);

        AlipaySettlementService settlementService = autoConfiguration.alipaySettlementService(gateway, customizers);
        assertInstanceOf(AlipaySettlementServiceImpl.class, settlementService);

        AlipayFaceToFaceService faceToFaceService = autoConfiguration.alipayFaceToFaceService(
                paymentProperties,
                gateway,
                customizers
        );
        assertInstanceOf(AlipayFaceToFaceServiceImpl.class, faceToFaceService);

        AlipayHuabeiService huabeiService = autoConfiguration.alipayHuabeiService(paymentProperties, gateway, customizers);
        assertInstanceOf(AlipayHuabeiServiceImpl.class, huabeiService);
    }

    /**
     * 创建测试用支付配置。
     *
     * @return 支付配置
     */
    private static PaymentProperties paymentProperties() {
        PaymentProperties paymentProperties = new PaymentProperties();
        PaymentProperties.Alipay alipay = paymentProperties.getAlipay();
        alipay.setEnabled(true);
        alipay.setAppId("2026000000000001");
        alipay.setMerchantPrivateKey("merchant-private-key");
        alipay.setAlipayPublicKey("alipay-public-key");
        alipay.setNotifyUrl("https://demo.example.com/alipay/notify");
        return paymentProperties;
    }

    /**
     * 创建不执行任何 SDK 调用的测试网关。
     *
     * @return 支付宝网关
     */
    private static AlipayGateway noopGateway() {
        return (AlipayGateway) Proxy.newProxyInstance(
                AlipayGateway.class.getClassLoader(),
                new Class<?>[]{AlipayGateway.class},
                (proxy, method, args) -> boolean.class.equals(method.getReturnType()) ? false : null
        );
    }
}
