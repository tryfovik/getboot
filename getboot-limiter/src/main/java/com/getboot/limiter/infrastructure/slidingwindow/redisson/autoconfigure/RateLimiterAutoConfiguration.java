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
package com.getboot.limiter.infrastructure.slidingwindow.redisson.autoconfigure;

import com.getboot.limiter.api.limiter.RateLimiter;
import com.getboot.limiter.api.properties.SlidingWindowRateLimiterProperties;
import com.getboot.limiter.api.registry.RateLimiterRegistry;
import com.getboot.limiter.infrastructure.slidingwindow.redisson.support.RedissonSlidingWindowRateLimiter;
import com.getboot.limiter.infrastructure.slidingwindow.redisson.support.RedissonSlidingWindowRateLimiterRegistry;
import com.getboot.limiter.infrastructure.slidingwindow.redisson.support.SlidingWindowRedisSupport;
import com.getboot.limiter.spi.RateLimiterRegistryCustomizer;
import com.getboot.limiter.support.aop.RateLimitAspect;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * 滑动窗口限流自动配置。
 *
 * <p>当前实现基于 Redis / Redisson，后续可在 limiter 模块中继续补充令牌桶、漏桶等其他算法。</p>
 *
 * @author qiheng
 */
@AutoConfiguration
@ConditionalOnClass(RedissonClient.class)
@ConditionalOnBean(RedissonClient.class)
@ConditionalOnProperty(prefix = "getboot.limiter", name = {"enabled", "sliding-window.enabled"}, havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(SlidingWindowRateLimiterProperties.class)
public class RateLimiterAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public SlidingWindowRedisSupport slidingWindowRedisSupport(RedissonClient redissonClient,
                                                               SlidingWindowRateLimiterProperties properties) {
        return new SlidingWindowRedisSupport(redissonClient, properties.getKeyPrefix());
    }

    @Bean
    @ConditionalOnMissingBean
    public RateLimiterRegistry redissonSlidingWindowRateLimiterRegistry(
            SlidingWindowRedisSupport slidingWindowRedisSupport,
            SlidingWindowRateLimiterProperties properties,
            ObjectProvider<RateLimiterRegistryCustomizer> registryCustomizers) {
        RedissonSlidingWindowRateLimiterRegistry registry =
                new RedissonSlidingWindowRateLimiterRegistry(slidingWindowRedisSupport, properties);
        registryCustomizers.orderedStream().forEach(customizer -> customizer.customize(registry));
        return registry;
    }

    @Bean
    @ConditionalOnMissingBean
    public RateLimiter redissonSlidingWindowRateLimiter(SlidingWindowRedisSupport slidingWindowRedisSupport) {
        return new RedissonSlidingWindowRateLimiter(slidingWindowRedisSupport);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(name = "org.aspectj.lang.ProceedingJoinPoint")
    public RateLimitAspect rateLimitAspect(RateLimiterRegistry rateLimiterRegistry) {
        return new RateLimitAspect(rateLimiterRegistry);
    }
}
