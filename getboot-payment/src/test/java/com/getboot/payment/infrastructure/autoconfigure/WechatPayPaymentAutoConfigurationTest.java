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

import com.getboot.payment.api.model.PaymentChannel;
import com.getboot.payment.api.model.PaymentMode;
import com.getboot.payment.api.properties.PaymentProperties;
import com.getboot.payment.api.service.PaymentService;
import com.getboot.payment.api.wechatpay.WechatPayApiService;
import com.getboot.payment.api.wechatpay.combine.WechatPayCombinePaymentService;
import com.getboot.payment.api.wechatpay.extension.complaint.WechatPayComplaintService;
import com.getboot.payment.api.wechatpay.operation.WechatPayOperationService;
import com.getboot.payment.api.wechatpay.operation.businesscircle.WechatPayBusinessCircleService;
import com.getboot.payment.api.wechatpay.operation.coupon.WechatPayCouponService;
import com.getboot.payment.api.wechatpay.operation.payscore.WechatPayPayScoreService;
import com.getboot.payment.api.wechatpay.security.WechatPaySecurityService;
import com.getboot.payment.api.wechatpay.settlement.WechatPaySettlementService;
import com.getboot.payment.api.wechatpay.trade.WechatPayTradeService;
import com.getboot.payment.infrastructure.wechatpay.WechatPayApiServiceImpl;
import com.getboot.payment.infrastructure.wechatpay.WechatPayPaymentService;
import com.getboot.payment.infrastructure.wechatpay.combine.WechatPayCombinePaymentServiceImpl;
import com.getboot.payment.infrastructure.wechatpay.extension.complaint.WechatPayComplaintServiceImpl;
import com.getboot.payment.infrastructure.wechatpay.operation.WechatPayOperationServiceImpl;
import com.getboot.payment.infrastructure.wechatpay.operation.businesscircle.WechatPayBusinessCircleServiceImpl;
import com.getboot.payment.infrastructure.wechatpay.operation.coupon.WechatPayCouponServiceImpl;
import com.getboot.payment.infrastructure.wechatpay.operation.payscore.WechatPayPayScoreServiceImpl;
import com.getboot.payment.infrastructure.wechatpay.security.WechatPaySecurityServiceImpl;
import com.getboot.payment.infrastructure.wechatpay.settlement.WechatPaySettlementServiceImpl;
import com.getboot.payment.infrastructure.wechatpay.trade.WechatPayTradeServiceImpl;
import com.getboot.payment.spi.wechatpay.WechatPayRequestCustomizer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.support.StaticListableBeanFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 微信支付自动配置映射测试。
 *
 * @author qiheng
 */
class WechatPayPaymentAutoConfigurationTest {

    /**
     * 验证微信支付自动配置会映射到默认实现。
     */
    @Test
    void shouldMapWechatPayServiceBeansToDefaultImplementations() {
        WechatPayPaymentAutoConfiguration autoConfiguration = new WechatPayPaymentAutoConfiguration();
        PaymentProperties paymentProperties = new PaymentProperties();
        ObjectProvider<WechatPayRequestCustomizer> customizers =
                new StaticListableBeanFactory().getBeanProvider(WechatPayRequestCustomizer.class);

        PaymentService paymentService = autoConfiguration.wechatPayPaymentService(
                paymentProperties,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                customizers
        );
        assertInstanceOf(WechatPayPaymentService.class, paymentService);
        assertEquals(PaymentChannel.WECHAT_PAY, paymentService.channel());
        assertTrue(paymentService.supportedModes().contains(PaymentMode.JSAPI));

        WechatPayApiService apiService = autoConfiguration.wechatPayApiService(null);
        assertInstanceOf(WechatPayApiServiceImpl.class, apiService);

        WechatPayCombinePaymentService combinePaymentService = autoConfiguration.wechatPayCombinePaymentService(
                paymentProperties,
                null,
                null,
                null,
                customizers
        );
        assertInstanceOf(WechatPayCombinePaymentServiceImpl.class, combinePaymentService);
        assertTrue(combinePaymentService.supportedModes().contains(PaymentMode.NATIVE));

        WechatPayTradeService tradeService = autoConfiguration.wechatPayTradeService(null, null, null, customizers);
        assertInstanceOf(WechatPayTradeServiceImpl.class, tradeService);

        WechatPayCouponService couponService = autoConfiguration.wechatPayCouponService(paymentProperties);
        assertInstanceOf(WechatPayCouponServiceImpl.class, couponService);

        WechatPayPayScoreService payScoreService = autoConfiguration.wechatPayPayScoreService(paymentProperties, null);
        assertInstanceOf(WechatPayPayScoreServiceImpl.class, payScoreService);

        WechatPayBusinessCircleService businessCircleService = autoConfiguration.wechatPayBusinessCircleService(null);
        assertInstanceOf(WechatPayBusinessCircleServiceImpl.class, businessCircleService);

        WechatPayOperationService operationService = autoConfiguration.wechatPayOperationService(
                couponService,
                payScoreService,
                businessCircleService,
                null,
                null,
                null,
                null,
                null,
                null
        );
        assertInstanceOf(WechatPayOperationServiceImpl.class, operationService);

        WechatPaySettlementService settlementService = autoConfiguration.wechatPaySettlementService(null, null, null);
        assertInstanceOf(WechatPaySettlementServiceImpl.class, settlementService);

        WechatPaySecurityService securityService = autoConfiguration.wechatPaySecurityService(null, null);
        assertInstanceOf(WechatPaySecurityServiceImpl.class, securityService);

        WechatPayComplaintService complaintService = autoConfiguration.wechatPayComplaintService(null, null);
        assertInstanceOf(WechatPayComplaintServiceImpl.class, complaintService);
    }
}
