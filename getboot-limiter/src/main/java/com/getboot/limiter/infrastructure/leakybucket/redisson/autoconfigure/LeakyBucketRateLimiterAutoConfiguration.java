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
package com.getboot.limiter.infrastructure.leakybucket.redisson.autoconfigure;

import com.getboot.limiter.api.properties.LeakyBucketRateLimiterProperties;
import com.getboot.limiter.infrastructure.leakybucket.redisson.support.LeakyBucketRedisSupport;
import com.getboot.limiter.infrastructure.leakybucket.redisson.support.RedissonLeakyBucketRateLimiterHandler;
import com.getboot.limiter.spi.RateLimiterAlgorithmHandler;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * 漏桶限流自动配置。
 *
 * <p>当前实现基于 Redis / Redisson 的状态存储与锁保护。</p>
 *
 * @author qiheng
 */
@AutoConfiguration
@ConditionalOnClass(RedissonClient.class)
@ConditionalOnBean(RedissonClient.class)
@ConditionalOnProperty(prefix = "getboot.limiter", name = {"enabled", "leaky-bucket.enabled"}, havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(LeakyBucketRateLimiterProperties.class)
public class LeakyBucketRateLimiterAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public LeakyBucketRedisSupport leakyBucketRedisSupport(RedissonClient redissonClient,
                                                           LeakyBucketRateLimiterProperties properties) {
        return new LeakyBucketRedisSupport(redissonClient, properties.getKeyPrefix());
    }

    @Bean
    @ConditionalOnMissingBean(name = "redissonLeakyBucketRateLimiterHandler")
    public RateLimiterAlgorithmHandler redissonLeakyBucketRateLimiterHandler(
            LeakyBucketRedisSupport leakyBucketRedisSupport,
            LeakyBucketRateLimiterProperties properties) {
        return new RedissonLeakyBucketRateLimiterHandler(leakyBucketRedisSupport, properties);
    }
}
