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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.getboot.cache.infrastructure.redis.support.RedisCacheOperator;
import com.getboot.cache.spi.redis.GetbootRedisTemplateCustomizer;
import com.getboot.cache.spi.redis.RedisObjectMapperCustomizer;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.lang.Nullable;

/**
 * Redis 自动配置。
 *
 * <p>用于提供默认 JSON 序列化的 RedisTemplate 与开箱即用的 Redis 操作工具类。</p>
 *
 * @author qiheng
 */
@AutoConfiguration
@ConditionalOnClass({RedisTemplate.class, GenericJackson2JsonRedisSerializer.class})
public class RedisAutoConfiguration {

    /**
     * 注册默认的 Redis 值序列化器。
     *
     * <p>基于 Jackson JSON 序列化，避免业务对象默认走 JDK 序列化。</p>
     *
     * @param objectMapperProvider ObjectMapper 提供器
     * @return Redis 值序列化器
     */
    @Bean("getbootRedisValueSerializer")
    @ConditionalOnMissingBean(name = "getbootRedisValueSerializer")
    public RedisSerializer<Object> getbootRedisValueSerializer(
            ObjectProvider<ObjectMapper> objectMapperProvider,
            ObjectProvider<RedisObjectMapperCustomizer> objectMapperCustomizers) {
        ObjectMapper redisObjectMapper = createRedisObjectMapper(
                objectMapperProvider.getIfAvailable(),
                objectMapperCustomizers
        );
        return new GenericJackson2JsonRedisSerializer(redisObjectMapper);
    }

    /**
     * 注册 GetBoot 默认 RedisTemplate。
     *
     * <p>该模板默认采用字符串 Key 与 JSON Value 序列化，并标记为主模板供业务优先注入。</p>
     *
     * @param connectionFactory Redis 连接工厂
     * @param valueSerializer Redis 值序列化器
     * @return RedisTemplate 实例
     */
    @Bean("getbootRedisTemplate")
    @Primary
    @ConditionalOnBean(RedisConnectionFactory.class)
    @ConditionalOnMissingBean(name = "getbootRedisTemplate")
    public RedisTemplate<String, Object> getbootRedisTemplate(
            RedisConnectionFactory connectionFactory,
            @Qualifier("getbootRedisValueSerializer") RedisSerializer<Object> valueSerializer,
            ObjectProvider<GetbootRedisTemplateCustomizer> redisTemplateCustomizers) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        StringRedisSerializer keySerializer = StringRedisSerializer.UTF_8;
        redisTemplate.setConnectionFactory(connectionFactory);
        redisTemplate.setKeySerializer(keySerializer);
        redisTemplate.setHashKeySerializer(keySerializer);
        redisTemplate.setValueSerializer(valueSerializer);
        redisTemplate.setHashValueSerializer(valueSerializer);
        redisTemplateCustomizers.orderedStream().forEach(customizer -> customizer.customize(redisTemplate));
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }

    /**
     * 注册 Redis 操作工具类。
     *
     * @param redisTemplate GetBoot RedisTemplate
     * @param stringRedisTemplate 字符串 RedisTemplate
     * @param objectMapperProvider ObjectMapper 提供器
     * @return Redis 操作工具类
     */
    @Bean
    @ConditionalOnBean({StringRedisTemplate.class, RedisConnectionFactory.class})
    @ConditionalOnMissingBean
    public CacheOperator cacheOperator(
            @Qualifier("getbootRedisTemplate") RedisTemplate<String, Object> redisTemplate,
            StringRedisTemplate stringRedisTemplate,
            ObjectProvider<ObjectMapper> objectMapperProvider,
            ObjectProvider<RedisObjectMapperCustomizer> objectMapperCustomizers) {
        return new RedisCacheOperator(
                redisTemplate,
                stringRedisTemplate,
                createRedisObjectMapper(objectMapperProvider.getIfAvailable(), objectMapperCustomizers)
        );
    }

    /**
     * 创建 Redis 专用 ObjectMapper。
     *
     * @param sourceObjectMapper 源 ObjectMapper
     * @param objectMapperCustomizers ObjectMapper 定制器
     * @return Redis 专用 ObjectMapper
     */
    private ObjectMapper createRedisObjectMapper(
            @Nullable ObjectMapper sourceObjectMapper,
            ObjectProvider<RedisObjectMapperCustomizer> objectMapperCustomizers) {
        ObjectMapper objectMapper = sourceObjectMapper == null ? new ObjectMapper() : sourceObjectMapper.copy();
        objectMapper.findAndRegisterModules();
        GenericJackson2JsonRedisSerializer.registerNullValueSerializer(objectMapper, "@class");
        objectMapperCustomizers.orderedStream().forEach(customizer -> customizer.customize(objectMapper));
        return objectMapper;
    }
}
