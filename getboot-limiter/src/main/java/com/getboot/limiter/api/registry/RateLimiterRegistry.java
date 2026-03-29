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
package com.getboot.limiter.api.registry;

import com.getboot.limiter.api.model.LimiterRule;

import java.util.concurrent.TimeUnit;

/**
 * 限流器注册表抽象。
 *
 * <p>对外暴露统一的限流规则注册与许可申请能力，屏蔽底层具体技术栈差异。</p>
 *
 * @author qiheng
 */
public interface RateLimiterRegistry {

    /**
     * 注册或更新命名限流器配置。
     *
     * @param limiterName 限流器名称
     * @param config 限流器配置
     */
    void configureRateLimiter(String limiterName, LimiterRule config);

    /**
     * 尝试获取一个许可。
     *
     * @param limiterName 限流器名称
     * @return 是否获取成功
     */
    boolean tryAcquire(String limiterName);

    /**
     * 尝试获取指定数量的许可。
     *
     * @param limiterName 限流器名称
     * @param permits 许可数量
     * @return 是否获取成功
     */
    boolean tryAcquire(String limiterName, long permits);

    /**
     * 在超时时间内尝试获取一个许可。
     *
     * @param limiterName 限流器名称
     * @param timeout 超时时间
     * @param timeUnit 时间单位
     * @return 是否获取成功
     */
    boolean tryAcquire(String limiterName, long timeout, TimeUnit timeUnit);

    /**
     * 在超时时间内尝试获取指定数量的许可。
     *
     * @param limiterName 限流器名称
     * @param permits 许可数量
     * @param timeout 超时时间
     * @param timeUnit 时间单位
     * @return 是否获取成功
     */
    boolean tryAcquire(String limiterName, long permits, long timeout, TimeUnit timeUnit);

    /**
     * 更新限流器配置。
     *
     * @param limiterName 限流器名称
     * @param newConfig 新配置
     */
    void updateRateLimiterConfig(String limiterName, LimiterRule newConfig);

    /**
     * 删除限流器。
     *
     * @param limiterName 限流器名称
     * @return 是否删除成功
     */
    boolean deleteRateLimiter(String limiterName);
}
