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
package com.getboot.cache.infrastructure.redis.autoconfigure;

import com.getboot.cache.api.operator.CacheOperator;
import com.getboot.cache.infrastructure.redis.support.RedisCacheOperator;
import com.getboot.cache.spi.redis.GetbootRedisTemplateCustomizer;
import com.getboot.cache.spi.redis.RedisObjectMapperCustomizer;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * Redis 自动配置测试。
 *
 * @author qiheng
 */
class RedisAutoConfigurationTest {

    /**
     * 测试用应用上下文运行器。
     */
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(RedisAutoConfiguration.class));

    /**
     * 验证存在 Redis 连接时会注册默认序列化器、模板与缓存门面。
     */
    @Test
    void shouldRegisterDefaultRedisBeansWhenConnectionFactoryPresent() {
        RedisConnectionFactory connectionFactory = mock(RedisConnectionFactory.class);
        contextRunner
                .withBean(RedisConnectionFactory.class, () -> connectionFactory)
                .withBean(StringRedisTemplate.class, () -> new TestStringRedisTemplate(connectionFactory))
                .withBean(
                        GetbootRedisTemplateCustomizer.class,
                        () -> redisTemplate -> redisTemplate.setExposeConnection(true)
                )
                .withBean(
                        RedisObjectMapperCustomizer.class,
                        () -> objectMapper -> objectMapper.setPropertyNamingStrategy(
                                com.fasterxml.jackson.databind.PropertyNamingStrategies.SNAKE_CASE
                        )
                )
                .run(context -> {
                    assertTrue(context.containsBean("getbootRedisValueSerializer"));
                    assertTrue(context.containsBean("getbootRedisTemplate"));
                    assertTrue(context.containsBean("cacheOperator"));

                    RedisSerializer<Object> serializer =
                            context.getBean("getbootRedisValueSerializer", RedisSerializer.class);
                    String payload = new String(
                            serializer.serialize(new DemoPayload("trace-001")),
                            StandardCharsets.UTF_8
                    );
                    assertTrue(payload.contains("trace_id"));

                    RedisTemplate<String, Object> redisTemplate =
                            context.getBean("getbootRedisTemplate", RedisTemplate.class);
                    assertSame(StringRedisSerializer.UTF_8, redisTemplate.getKeySerializer());
                    assertSame(StringRedisSerializer.UTF_8, redisTemplate.getHashKeySerializer());
                    assertInstanceOf(GenericJackson2JsonRedisSerializer.class, redisTemplate.getValueSerializer());
                    assertInstanceOf(GenericJackson2JsonRedisSerializer.class, redisTemplate.getHashValueSerializer());
                    assertTrue(redisTemplate.isExposeConnection());
                    assertInstanceOf(RedisCacheOperator.class, context.getBean(CacheOperator.class));
                });
    }

    /**
     * 验证缺少 StringRedisTemplate 时不会注册缓存门面。
     */
    @Test
    void shouldSkipCacheOperatorWhenStringRedisTemplateIsMissing() {
        contextRunner
                .withBean(RedisConnectionFactory.class, () -> mock(RedisConnectionFactory.class))
                .run(context -> {
                    assertTrue(context.containsBean("getbootRedisValueSerializer"));
                    assertTrue(context.containsBean("getbootRedisTemplate"));
                    assertFalse(context.containsBean("cacheOperator"));
                });
    }

    /**
     * 测试用负载对象。
     */
    private static final class DemoPayload {

        /**
         * 链路标识。
         */
        private final String traceId;

        /**
         * 创建测试负载。
         *
         * @param traceId 链路标识
         */
        private DemoPayload(String traceId) {
            this.traceId = traceId;
        }

        /**
         * 返回链路标识。
         *
         * @return 链路标识
         */
        public String getTraceId() {
            return traceId;
        }
    }

    /**
     * 测试用字符串 RedisTemplate，避免上下文启动时依赖真实连接初始化。
     */
    private static final class TestStringRedisTemplate extends StringRedisTemplate {

        /**
         * 创建测试用字符串模板。
         *
         * @param connectionFactory Redis 连接工厂
         */
        private TestStringRedisTemplate(RedisConnectionFactory connectionFactory) {
            setConnectionFactory(connectionFactory);
        }

        /**
         * 跳过真实初始化，避免测试依赖外部 Redis。
         */
        @Override
        public void afterPropertiesSet() {
        }
    }
}
