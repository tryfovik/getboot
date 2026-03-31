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
 * 令牌桶限流配置。
 *
 * <p>当前实现基于 Redis / Redisson 的 {@code RRateLimiter} 能力。</p>
 *
 * @author qiheng
 */
@Data
@ConfigurationProperties(prefix = "getboot.limiter.token-bucket")
public class TokenBucketRateLimiterProperties {

    /**
     * 是否启用令牌桶限流实现。
     */
    private boolean enabled = true;

    /**
     * 默认等待超时时间，单位秒。
     */
    private long defaultTimeout = 5;

    /**
     * Redis 中令牌桶 key 前缀。
     */
    private String keyPrefix = "rate_limiter_token_bucket";

    /**
     * 预定义限流器模板，key 为限流器名称。
     */
    private Map<String, LimiterRule> limiters = new HashMap<>();
}
