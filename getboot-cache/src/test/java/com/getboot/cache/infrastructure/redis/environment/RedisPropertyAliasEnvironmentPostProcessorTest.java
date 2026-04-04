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
package com.getboot.cache.infrastructure.redis.environment;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link RedisPropertyAliasEnvironmentPostProcessor} 测试。
 *
 * @author qiheng
 */
class RedisPropertyAliasEnvironmentPostProcessorTest {

    /**
     * 验证 GetBoot Redis 前缀会桥接到 Spring Data Redis 原生前缀。
     */
    @Test
    void shouldAliasGetbootCacheRedisPropertiesToSpringDataRedis() {
        ConfigurableEnvironment environment = new StandardEnvironment();
        environment.getPropertySources().addFirst(new MapPropertySource("source", Map.of(
                "getboot.cache.redis.host", "127.0.0.1",
                "getboot.cache.redis.port", "6380",
                "getboot.cache.redis.database", "2"
        )));

        RedisPropertyAliasEnvironmentPostProcessor processor = new RedisPropertyAliasEnvironmentPostProcessor();
        processor.postProcessEnvironment(environment, new SpringApplication(Object.class));

        assertEquals("127.0.0.1", environment.getProperty("spring.data.redis.host"));
        assertEquals("6380", environment.getProperty("spring.data.redis.port"));
        assertEquals("2", environment.getProperty("spring.data.redis.database"));
    }

    /**
     * 验证已显式声明的 Spring Data Redis 原生配置不会被别名覆盖。
     */
    @Test
    void shouldNotOverrideExistingSpringDataRedisProperties() {
        ConfigurableEnvironment environment = new StandardEnvironment();
        environment.getPropertySources().addFirst(new MapPropertySource("source", Map.of(
                "getboot.cache.redis.port", "6380",
                "spring.data.redis.port", "6379"
        )));

        RedisPropertyAliasEnvironmentPostProcessor processor = new RedisPropertyAliasEnvironmentPostProcessor();
        processor.postProcessEnvironment(environment, new SpringApplication(Object.class));

        assertEquals("6379", environment.getProperty("spring.data.redis.port"));
    }
}
