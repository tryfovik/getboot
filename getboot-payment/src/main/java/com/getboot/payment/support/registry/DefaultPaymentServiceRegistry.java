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
package com.getboot.payment.support.registry;

import com.getboot.payment.api.model.PaymentChannel;
import com.getboot.payment.api.registry.PaymentServiceRegistry;
import com.getboot.payment.api.service.PaymentService;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 默认支付服务注册表实现。
 *
 * @author qiheng
 */
public class DefaultPaymentServiceRegistry implements PaymentServiceRegistry {

    /**
     * 已注册的渠道服务映射。
     */
    private final Map<PaymentChannel, PaymentService> services;

    /**
     * 基于当前 Spring 容器中的支付服务构建注册表。
     *
     * @param paymentServices 支付服务列表
     */
    public DefaultPaymentServiceRegistry(List<PaymentService> paymentServices) {
        Map<PaymentChannel, PaymentService> serviceMap = new EnumMap<>(PaymentChannel.class);
        for (PaymentService paymentService : paymentServices) {
            PaymentChannel channel = paymentService.channel();
            PaymentService previous = serviceMap.putIfAbsent(channel, paymentService);
            if (previous != null) {
                throw new IllegalStateException("Multiple PaymentService beans found for channel: " + channel);
            }
        }
        this.services = Collections.unmodifiableMap(serviceMap);
    }

    @Override
    public Optional<PaymentService> get(PaymentChannel channel) {
        return Optional.ofNullable(services.get(channel));
    }

    @Override
    public PaymentService getRequired(PaymentChannel channel) {
        return get(channel)
                .orElseThrow(() -> new IllegalStateException("No PaymentService bean configured for channel: " + channel));
    }

    @Override
    public Map<PaymentChannel, PaymentService> asMap() {
        return services;
    }
}
