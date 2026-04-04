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

import com.getboot.limiter.api.limiter.RateLimiter;
import com.getboot.limiter.api.properties.LeakyBucketRateLimiterProperties;
import com.getboot.limiter.api.properties.SlidingWindowRateLimiterProperties;
import com.getboot.limiter.api.properties.TokenBucketRateLimiterProperties;
import com.getboot.limiter.api.registry.RateLimiterRegistry;
import com.getboot.limiter.infrastructure.leakybucket.redisson.support.LeakyBucketRedisSupport;
import com.getboot.limiter.infrastructure.slidingwindow.redisson.support.RedissonSlidingWindowRateLimiter;
import com.getboot.limiter.infrastructure.slidingwindow.redisson.support.SlidingWindowRedisSupport;
import com.getboot.limiter.infrastructure.tokenbucket.redisson.support.TokenBucketRedisSupport;
import com.getboot.limiter.spi.RateLimiterAlgorithmHandler;
import com.getboot.limiter.support.aop.RateLimitAspect;
import com.getboot.limiter.support.resolver.RateLimitOperationResolver;
import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.lang.reflect.Proxy;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * limiter 自动配置测试。
 */
class LimiterAutoConfigurationTest {

    /**
     * 测试用上下文运行器。
     */
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(LimiterAutoConfiguration.class))
            .withBean(RedissonClient.class, LimiterAutoConfigurationTest::redissonClient);

    /**
     * 验证启用 limiter 时会注册三类算法实现和核心 Bean。
     */
    @Test
    void shouldRegisterLimiterBeansWhenEnabled() {
        contextRunner
                .withPropertyValues(
                        "getboot.limiter.enabled=true",
                        "getboot.limiter.sliding-window.key-prefix=sw-prefix",
                        "getboot.limiter.token-bucket.key-prefix=tb-prefix",
                        "getboot.limiter.leaky-bucket.key-prefix=lb-prefix"
                )
                .run(context -> {
                    assertInstanceOf(SlidingWindowRedisSupport.class, context.getBean(SlidingWindowRedisSupport.class));
                    assertInstanceOf(TokenBucketRedisSupport.class, context.getBean(TokenBucketRedisSupport.class));
                    assertInstanceOf(LeakyBucketRedisSupport.class, context.getBean(LeakyBucketRedisSupport.class));
                    assertInstanceOf(RedissonSlidingWindowRateLimiter.class, context.getBean(RateLimiter.class));
                    assertInstanceOf(RateLimiterRegistry.class, context.getBean(RateLimiterRegistry.class));
                    assertInstanceOf(RateLimitOperationResolver.class, context.getBean(RateLimitOperationResolver.class));
                    assertInstanceOf(RateLimitAspect.class, context.getBean(RateLimitAspect.class));
                    assertEquals(3, context.getBeansOfType(RateLimiterAlgorithmHandler.class).size());

                    SlidingWindowRateLimiterProperties slidingWindowProperties =
                            context.getBean(SlidingWindowRateLimiterProperties.class);
                    TokenBucketRateLimiterProperties tokenBucketProperties =
                            context.getBean(TokenBucketRateLimiterProperties.class);
                    LeakyBucketRateLimiterProperties leakyBucketProperties =
                            context.getBean(LeakyBucketRateLimiterProperties.class);
                    assertEquals("sw-prefix", slidingWindowProperties.getKeyPrefix());
                    assertEquals("tb-prefix", tokenBucketProperties.getKeyPrefix());
                    assertEquals("lb-prefix", leakyBucketProperties.getKeyPrefix());
                });
    }

    /**
     * 验证禁用 limiter 时跳过全部相关 Bean。
     */
    @Test
    void shouldSkipAllLimiterBeansWhenDisabled() {
        contextRunner
                .withPropertyValues("getboot.limiter.enabled=false")
                .run(context -> {
                    assertFalse(context.containsBean("slidingWindowRedisSupport"));
                    assertFalse(context.containsBean("redissonSlidingWindowRateLimiterHandler"));
                    assertFalse(context.containsBean("tokenBucketRedisSupport"));
                    assertFalse(context.containsBean("redissonTokenBucketRateLimiterHandler"));
                    assertFalse(context.containsBean("leakyBucketRedisSupport"));
                    assertFalse(context.containsBean("redissonLeakyBucketRateLimiterHandler"));
                    assertFalse(context.containsBean("rateLimiterRegistry"));
                    assertFalse(context.containsBean("rateLimitOperationResolver"));
                    assertFalse(context.containsBean("rateLimitAspect"));
                });
    }

    /**
     * 验证关闭令牌桶和漏桶后，仍会保留滑动窗口和核心 Bean。
     */
    @Test
    void shouldKeepSlidingWindowAndCoreBeansWhenOtherAlgorithmsDisabled() {
        contextRunner
                .withPropertyValues(
                        "getboot.limiter.enabled=true",
                        "getboot.limiter.token-bucket.enabled=false",
                        "getboot.limiter.leaky-bucket.enabled=false"
                )
                .run(context -> {
                    assertTrue(context.containsBean("slidingWindowRedisSupport"));
                    assertTrue(context.containsBean("redissonSlidingWindowRateLimiterHandler"));
                    assertTrue(context.containsBean("redissonSlidingWindowRateLimiter"));
                    assertFalse(context.containsBean("tokenBucketRedisSupport"));
                    assertFalse(context.containsBean("redissonTokenBucketRateLimiterHandler"));
                    assertFalse(context.containsBean("leakyBucketRedisSupport"));
                    assertFalse(context.containsBean("redissonLeakyBucketRateLimiterHandler"));
                    assertTrue(context.containsBean("rateLimiterRegistry"));
                    assertTrue(context.containsBean("rateLimitAspect"));
                });
    }

    /**
     * 创建测试用 Redisson 客户端代理。
     *
     * @return Redisson 客户端
     */
    private static RedissonClient redissonClient() {
        return (RedissonClient) Proxy.newProxyInstance(
                RedissonClient.class.getClassLoader(),
                new Class[]{RedissonClient.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "equals" -> proxy == args[0];
                    case "toString" -> "RedissonClientProxy";
                    default -> throw new UnsupportedOperationException(method.getName());
                }
        );
    }
}
