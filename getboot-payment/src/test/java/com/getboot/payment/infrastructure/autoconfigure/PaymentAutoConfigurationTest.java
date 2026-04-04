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
import com.getboot.payment.api.registry.PaymentServiceRegistry;
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
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 支付模块总入口自动配置测试。
 *
 * @author qiheng
 */
class PaymentAutoConfigurationTest {

    /**
     * 测试用上下文运行器。
     */
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(PaymentAutoConfiguration.class));

    /**
     * 验证默认自动配置会基于已发现的支付服务注册注册表。
     */
    @Test
    void shouldRegisterPaymentServiceRegistryFromDiscoveredServices() {
        contextRunner
                .withBean("alipayPaymentService", PaymentService.class, () -> new StubPaymentService(PaymentChannel.ALIPAY))
                .withBean("wechatPayPaymentService", PaymentService.class, () -> new StubPaymentService(
                        PaymentChannel.WECHAT_PAY))
                .run(context -> {
                    assertTrue(context.containsBean("paymentServiceRegistry"));

                    PaymentServiceRegistry registry = context.getBean(PaymentServiceRegistry.class);
                    assertEquals(Set.of(PaymentChannel.ALIPAY, PaymentChannel.WECHAT_PAY), registry.asMap().keySet());
                    assertEquals(PaymentChannel.ALIPAY, registry.getRequired(PaymentChannel.ALIPAY).channel());
                    assertEquals(PaymentChannel.WECHAT_PAY, registry.getRequired(PaymentChannel.WECHAT_PAY).channel());
                });
    }

    /**
     * 验证关闭支付模块后不会注册默认注册表。
     */
    @Test
    void shouldSkipRegistryWhenPaymentModuleDisabled() {
        contextRunner
                .withPropertyValues("getboot.payment.enabled=false")
                .run(context -> assertFalse(context.containsBean("paymentServiceRegistry")));
    }

    /**
     * 验证业务方自定义注册表存在时，自动配置会正常让位。
     */
    @Test
    void shouldBackOffWhenCustomRegistryProvided() {
        PaymentServiceRegistry customRegistry = new StaticPaymentServiceRegistry(Map.of());

        contextRunner
                .withBean(PaymentServiceRegistry.class, () -> customRegistry)
                .run(context -> assertSame(customRegistry, context.getBean(PaymentServiceRegistry.class)));
    }

    /**
     * 测试用固定支付服务注册表。
     *
     * @param services 渠道服务映射
     */
    private record StaticPaymentServiceRegistry(Map<PaymentChannel, PaymentService> services)
            implements PaymentServiceRegistry {

        /**
         * 返回指定渠道的支付服务。
         *
         * @param channel 支付渠道
         * @return 支付服务
         */
        @Override
        public Optional<PaymentService> get(PaymentChannel channel) {
            return Optional.ofNullable(services.get(channel));
        }

        /**
         * 返回指定渠道的支付服务，不存在时抛异常。
         *
         * @param channel 支付渠道
         * @return 支付服务
         */
        @Override
        public PaymentService getRequired(PaymentChannel channel) {
            return get(channel).orElseThrow();
        }

        /**
         * 返回当前已注册的渠道服务。
         *
         * @return 渠道服务映射
         */
        @Override
        public Map<PaymentChannel, PaymentService> asMap() {
            return services;
        }
    }

    /**
     * 测试用支付服务桩。
     *
     * @param channel 支付渠道
     */
    private record StubPaymentService(PaymentChannel channel) implements PaymentService {

        /**
         * 返回测试支持的支付方式。
         *
         * @return 支付方式集合
         */
        @Override
        public Set<PaymentMode> supportedModes() {
            return Set.of(PaymentMode.APP);
        }

        /**
         * 当前测试不校验下单实现。
         *
         * @param request 下单请求
         * @return 始终为空
         */
        @Override
        public PaymentCreateResponse create(PaymentCreateRequest request) {
            return null;
        }

        /**
         * 当前测试不校验退款实现。
         *
         * @param request 退款请求
         * @return 始终为空
         */
        @Override
        public PaymentRefundResponse refund(PaymentRefundRequest request) {
            return null;
        }

        /**
         * 当前测试不校验查单实现。
         *
         * @param request 查单请求
         * @return 始终为空
         */
        @Override
        public PaymentOrderQueryResponse queryOrder(PaymentOrderQueryRequest request) {
            return null;
        }

        /**
         * 当前测试不校验查退款实现。
         *
         * @param request 查退款请求
         * @return 始终为空
         */
        @Override
        public PaymentRefundQueryResponse queryRefund(PaymentRefundQueryRequest request) {
            return null;
        }

        /**
         * 当前测试不校验关单实现。
         *
         * @param request 关单请求
         * @return 始终为空
         */
        @Override
        public PaymentCloseResponse close(PaymentCloseRequest request) {
            return null;
        }

        /**
         * 当前测试不校验通知解析实现。
         *
         * @param request 通知请求
         * @return 始终为空
         */
        @Override
        public PaymentNotifyResponse parseNotify(PaymentNotifyRequest request) {
            return null;
        }
    }
}
