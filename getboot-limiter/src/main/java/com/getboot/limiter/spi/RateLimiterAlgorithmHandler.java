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
package com.getboot.limiter.spi;

import com.getboot.limiter.api.model.LimiterAlgorithm;
import com.getboot.limiter.api.model.LimiterRule;

import java.util.Map;

/**
 * 限流算法处理器。
 *
 * <p>用于把统一注册表中的规则路由到具体算法实现，并暴露算法级默认规则与预定义规则。</p>
 *
 * @author qiheng
 */
public interface RateLimiterAlgorithmHandler {

    LimiterAlgorithm algorithm();

    Map<String, LimiterRule> predefinedRules();

    LimiterRule defaultRule();

    long defaultTimeout();

    void validateRule(LimiterRule rule);

    boolean tryAcquire(String limiterName, LimiterRule rule, long permits);

    boolean delete(String limiterName);
}
