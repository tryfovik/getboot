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
package com.getboot.limiter.api.properties;

import com.getboot.limiter.api.model.LimiterRule;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * 滑动窗口限流配置。
 *
 * <p>当前对外保持 limiter 能力模块语义，具体算法能力先收敛到 sliding-window 子树。</p>
 *
 * @author qiheng
 */
@Data
@ConfigurationProperties(prefix = "getboot.limiter.sliding-window")
public class SlidingWindowRateLimiterProperties {

    /**
     * 是否启用滑动窗口限流实现。
     */
    private boolean enabled = true;

    /**
     * 默认等待超时时间，单位秒。
     */
    private long defaultTimeout = 5;

    /**
     * Redis 中滑动窗口 key 前缀。
     */
    private String keyPrefix = "rate_limiter";

    /**
     * 预定义限流器模板，key 为限流器名称。
     */
    private Map<String, LimiterRule> limiters = new HashMap<>();
}
