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
package com.getboot.limiter.infrastructure.autoconfigure;

import com.getboot.limiter.infrastructure.slidingwindow.redisson.autoconfigure.RateLimiterAutoConfiguration;
import com.getboot.limiter.infrastructure.tokenbucket.redisson.autoconfigure.TokenBucketRateLimiterAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

/**
 * 限流模块统一自动配置入口。
 *
 * <p>对外只暴露模块级自动配置入口，内部再按算法或技术栈拆分具体实现。</p>
 *
 * @author qiheng
 */
@AutoConfiguration
@Import({
        RateLimiterCoreAutoConfiguration.class,
        RateLimiterAutoConfiguration.class,
        TokenBucketRateLimiterAutoConfiguration.class
})
public class LimiterAutoConfiguration {
}
