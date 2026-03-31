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
package com.getboot.limiter.infrastructure.tokenbucket.redisson.autoconfigure;

import com.getboot.limiter.api.properties.TokenBucketRateLimiterProperties;
import com.getboot.limiter.infrastructure.tokenbucket.redisson.support.RedissonTokenBucketRateLimiterHandler;
import com.getboot.limiter.infrastructure.tokenbucket.redisson.support.TokenBucketRedisSupport;
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
 * 令牌桶限流自动配置。
 *
 * <p>当前实现基于 Redis / Redisson 的 {@code RRateLimiter}。</p>
 *
 * @author qiheng
 */
@AutoConfiguration
@ConditionalOnClass(RedissonClient.class)
@ConditionalOnBean(RedissonClient.class)
@ConditionalOnProperty(prefix = "getboot.limiter", name = {"enabled", "token-bucket.enabled"}, havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(TokenBucketRateLimiterProperties.class)
public class TokenBucketRateLimiterAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public TokenBucketRedisSupport tokenBucketRedisSupport(RedissonClient redissonClient,
                                                           TokenBucketRateLimiterProperties properties) {
        return new TokenBucketRedisSupport(redissonClient, properties.getKeyPrefix());
    }

    @Bean
    @ConditionalOnMissingBean(name = "redissonTokenBucketRateLimiterHandler")
    public RateLimiterAlgorithmHandler redissonTokenBucketRateLimiterHandler(
            TokenBucketRedisSupport tokenBucketRedisSupport,
            TokenBucketRateLimiterProperties properties) {
        return new RedissonTokenBucketRateLimiterHandler(tokenBucketRedisSupport, properties);
    }
}
