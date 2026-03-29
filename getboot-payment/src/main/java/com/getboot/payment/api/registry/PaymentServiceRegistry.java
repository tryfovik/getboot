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
package com.getboot.payment.api.registry;

import com.getboot.payment.api.model.PaymentChannel;
import com.getboot.payment.api.service.PaymentService;

import java.util.Map;
import java.util.Optional;

/**
 * 支付服务注册表。
 *
 * @author qiheng
 */
public interface PaymentServiceRegistry {

    /**
     * 获取指定渠道的支付服务。
     *
     * @param channel 支付渠道
     * @return 支付服务
     */
    Optional<PaymentService> get(PaymentChannel channel);

    /**
     * 获取指定渠道的支付服务，不存在时抛异常。
     *
     * @param channel 支付渠道
     * @return 支付服务
     */
    PaymentService getRequired(PaymentChannel channel);

    /**
     * 获取当前已注册支付服务。
     *
     * @return 渠道与服务映射
     */
    Map<PaymentChannel, PaymentService> asMap();
}
