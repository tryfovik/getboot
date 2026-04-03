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
package com.getboot.cache.infrastructure.redis.support;

import com.getboot.cache.api.operator.CacheOperator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.Assert;

import java.time.Duration;
import java.util.Collection;

/**
 * Redis 操作工具类。
 *
 * <p>封装常用的字符串、对象、哈希与过期时间操作，降低业务侧直接使用底层模板的成本。</p>
 *
 * @author qiheng
 */
public class RedisCacheOperator implements CacheOperator {

    /**
     * 对象 RedisTemplate。
     */
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 字符串 RedisTemplate。
     */
    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 对象转换器。
     */
    private final ObjectMapper objectMapper;

    /**
     * 创建 Redis 缓存操作器。
     *
     * @param redisTemplate 对象 RedisTemplate
     * @param stringRedisTemplate 字符串 RedisTemplate
     * @param objectMapper 对象转换器
     */
    public RedisCacheOperator(
            RedisTemplate<String, Object> redisTemplate,
            StringRedisTemplate stringRedisTemplate,
            ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.stringRedisTemplate = stringRedisTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * 写入对象值。
     *
     * @param key Redis Key
     * @param value 对象值
     */
    @Override
    public void set(String key, Object value) {
        assertKey(key);
        redisTemplate.opsForValue().set(key, value);
    }

    /**
     * 写入对象值并设置过期时间。
     *
     * @param key Redis Key
     * @param value 对象值
     * @param ttl 过期时间
     */
    @Override
    public void set(String key, Object value, Duration ttl) {
        assertKey(key);
        assertDuration(ttl);
        redisTemplate.opsForValue().set(key, value, ttl);
    }

    /**
     * 读取对象值。
     *
     * @param key Redis Key
     * @param valueType 目标类型
     * @param <T> 返回值类型
     * @return 反序列化后的对象
     */
    @Override
    public <T> T get(String key, Class<T> valueType) {
        assertKey(key);
        Assert.notNull(valueType, "Redis value type must not be null.");
        return convertValue(redisTemplate.opsForValue().get(key), valueType);
    }

    /**
     * 写入字符串值。
     *
     * @param key Redis Key
     * @param value 字符串值
     */
    @Override
    public void setString(String key, String value) {
        assertKey(key);
        stringRedisTemplate.opsForValue().set(key, value);
    }

    /**
     * 写入字符串值并设置过期时间。
     *
     * @param key Redis Key
     * @param value 字符串值
     * @param ttl 过期时间
     */
    @Override
    public void setString(String key, String value, Duration ttl) {
        assertKey(key);
        assertDuration(ttl);
        stringRedisTemplate.opsForValue().set(key, value, ttl);
    }

    /**
     * 读取字符串值。
     *
     * @param key Redis Key
     * @return 字符串值
     */
    @Override
    public String getString(String key) {
        assertKey(key);
        return stringRedisTemplate.opsForValue().get(key);
    }

    /**
     * 对字符串值执行自增。
     *
     * @param key Redis Key
     * @param delta 增量
     * @return 自增后的值
     */
    @Override
    public Long increment(String key, long delta) {
        assertKey(key);
        return stringRedisTemplate.opsForValue().increment(key, delta);
    }

    /**
     * 删除单个 Key。
     *
     * @param key Redis Key
     * @return 是否删除成功
     */
    @Override
    public Boolean delete(String key) {
        assertKey(key);
        return redisTemplate.delete(key);
    }

    /**
     * 批量删除 Key。
     *
     * @param keys Redis Key 列表
     * @return 删除数量
     */
    @Override
    public Long delete(Collection<String> keys) {
        Assert.notEmpty(keys, "Redis keys must not be empty.");
        return redisTemplate.delete(keys);
    }

    /**
     * 判断 Key 是否存在。
     *
     * @param key Redis Key
     * @return 是否存在
     */
    @Override
    public Boolean hasKey(String key) {
        assertKey(key);
        return redisTemplate.hasKey(key);
    }

    /**
     * 设置 Key 过期时间。
     *
     * @param key Redis Key
     * @param ttl 过期时间
     * @return 是否设置成功
     */
    @Override
    public Boolean expire(String key, Duration ttl) {
        assertKey(key);
        assertDuration(ttl);
        return redisTemplate.expire(key, ttl);
    }

    /**
     * 写入 Hash 字段值。
     *
     * @param key Redis Key
     * @param hashKey Hash 字段
     * @param value 字段值
     */
    @Override
    public void putHash(String key, String hashKey, Object value) {
        assertKey(key);
        assertKey(hashKey);
        redisTemplate.opsForHash().put(key, hashKey, value);
    }

    /**
     * 读取 Hash 字段值。
     *
     * @param key Redis Key
     * @param hashKey Hash 字段
     * @param valueType 目标类型
     * @param <T> 返回值类型
     * @return 反序列化后的对象
     */
    @Override
    public <T> T getHash(String key, String hashKey, Class<T> valueType) {
        assertKey(key);
        assertKey(hashKey);
        Assert.notNull(valueType, "Redis value type must not be null.");
        return convertValue(redisTemplate.opsForHash().get(key, hashKey), valueType);
    }

    /**
     * 校验缓存键不能为空白。
     *
     * @param key 缓存键
     */
    private void assertKey(String key) {
        Assert.hasText(key, "Redis key must not be blank.");
    }

    /**
     * 校验过期时间必须大于零。
     *
     * @param ttl 过期时间
     */
    private void assertDuration(Duration ttl) {
        Assert.notNull(ttl, "Redis ttl must not be null.");
        Assert.isTrue(!ttl.isNegative() && !ttl.isZero(), "Redis ttl must be greater than 0.");
    }

    /**
     * 将读取结果转换为目标类型。
     *
     * @param source 原始值
     * @param valueType 目标类型
     * @param <T> 返回值类型
     * @return 转换后的值
     */
    private <T> T convertValue(Object source, Class<T> valueType) {
        if (source == null) {
            return null;
        }
        if (valueType.isInstance(source)) {
            return valueType.cast(source);
        }
        return objectMapper.convertValue(source, valueType);
    }
}
