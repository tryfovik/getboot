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
package com.getboot.idempotency.infrastructure.autoconfigure;

import com.getboot.idempotency.api.properties.IdempotencyProperties;
import com.getboot.idempotency.infrastructure.redis.support.RedisIdempotencyStore;
import com.getboot.idempotency.spi.IdempotencyDuplicateRequestHandler;
import com.getboot.idempotency.spi.IdempotencyKeyResolver;
import com.getboot.idempotency.spi.IdempotencyStore;
import com.getboot.idempotency.support.DefaultIdempotencyDuplicateRequestHandler;
import com.getboot.idempotency.support.SpelIdempotencyKeyResolver;
import com.getboot.idempotency.support.aop.IdempotencyAspect;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.data.redis.core.RedisTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 幂等自动配置测试。
 *
 * @author qiheng
 */
class IdempotencyAutoConfigurationTest {

    /**
     * 测试用上下文运行器。
     */
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(IdempotencyAutoConfiguration.class));

    /**
     * 验证默认情况下会注册幂等核心 Bean、Redis 存储与切面。
     */
    @Test
    void shouldRegisterCoreBeansAndRedisStoreByDefault() {
        contextRunner
                .withPropertyValues(
                        "getboot.idempotency.default-ttl-seconds=600",
                        "getboot.idempotency.redis.key-prefix=demo:idempotency"
                )
                .withBean("getbootRedisTemplate", RedisTemplate.class, TestRedisTemplate::new)
                .run(context -> {
                    assertInstanceOf(SpelIdempotencyKeyResolver.class, context.getBean(IdempotencyKeyResolver.class));
                    assertInstanceOf(
                            DefaultIdempotencyDuplicateRequestHandler.class,
                            context.getBean(IdempotencyDuplicateRequestHandler.class)
                    );
                    assertInstanceOf(RedisIdempotencyStore.class, context.getBean(IdempotencyStore.class));
                    assertInstanceOf(IdempotencyAspect.class, context.getBean(IdempotencyAspect.class));

                    IdempotencyProperties properties = context.getBean(IdempotencyProperties.class);
                    assertEquals(600, properties.getDefaultTtlSeconds());
                    assertEquals("demo:idempotency", properties.resolveKeyPrefix());
                });
    }

    /**
     * 验证关闭幂等模块时不会注册任何幂等相关 Bean。
     */
    @Test
    void shouldSkipAllBeansWhenIdempotencyDisabled() {
        contextRunner
                .withPropertyValues("getboot.idempotency.enabled=false")
                .withBean("getbootRedisTemplate", RedisTemplate.class, TestRedisTemplate::new)
                .run(context -> {
                    assertFalse(context.containsBean("idempotencyKeyResolver"));
                    assertFalse(context.containsBean("idempotencyDuplicateRequestHandler"));
                    assertFalse(context.containsBean("idempotencyStore"));
                    assertFalse(context.containsBean("idempotencyAspect"));
                });
    }

    /**
     * 验证切换到非 Redis 类型时不会注册 Redis 存储和切面。
     */
    @Test
    void shouldSkipRedisStoreWhenStoreTypeIsNotRedis() {
        contextRunner
                .withPropertyValues("getboot.idempotency.type=custom")
                .withBean("getbootRedisTemplate", RedisTemplate.class, TestRedisTemplate::new)
                .run(context -> {
                    assertTrue(context.containsBean("idempotencyKeyResolver"));
                    assertTrue(context.containsBean("idempotencyDuplicateRequestHandler"));
                    assertFalse(context.containsBean("idempotencyStore"));
                    assertFalse(context.containsBean("idempotencyAspect"));
                });
    }

    /**
     * 轻量级测试用 RedisTemplate，避免初始化时依赖真实连接。
     */
    private static final class TestRedisTemplate extends RedisTemplate<String, Object> {

        /**
         * 跳过真实初始化，避免测试依赖外部 Redis。
         */
        @Override
        public void afterPropertiesSet() {
        }
    }
}
