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

import com.getboot.payment.api.properties.PaymentProperties;
import com.getboot.payment.api.registry.PaymentServiceRegistry;
import com.getboot.payment.api.service.PaymentService;
import com.getboot.payment.support.registry.DefaultPaymentServiceRegistry;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * 支付模块自动配置。
 *
 * @author qiheng
 */
@AutoConfiguration
@EnableConfigurationProperties(PaymentProperties.class)
@ConditionalOnProperty(prefix = "getboot.payment", name = "enabled", havingValue = "true", matchIfMissing = true)
public class PaymentAutoConfiguration {

    /**
     * 注册默认支付服务注册表。
     *
     * @param paymentServices 已注册的支付服务实现
     * @return 支付服务注册表
     */
    @Bean
    @ConditionalOnMissingBean
    public PaymentServiceRegistry paymentServiceRegistry(ObjectProvider<PaymentService> paymentServices) {
        return new DefaultPaymentServiceRegistry(paymentServices.orderedStream().toList());
    }
}
