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

import com.getboot.limiter.api.registry.RateLimiterRegistry;

/**
 * 限流器注册表定制器。
 *
 * <p>业务方可通过注册该类型 Bean，在 GetBoot 初始化完成后动态补充限流规则。</p>
 *
 * @author qiheng
 */
@FunctionalInterface
public interface RateLimiterRegistryCustomizer {

    /**
     * 自定义限流器注册表。
     *
     * @param registry 限流器注册表
     */
    void customize(RateLimiterRegistry registry);
}
