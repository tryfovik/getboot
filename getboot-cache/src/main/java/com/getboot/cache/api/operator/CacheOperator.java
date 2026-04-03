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
package com.getboot.cache.api.operator;

import java.time.Duration;
import java.util.Collection;

/**
 * 缓存操作门面。
 *
 * <p>对外暴露常见的 Key-Value、Hash 与过期时间操作，避免业务侧直接依赖具体缓存实现。</p>
 *
 * @author qiheng
 */
public interface CacheOperator {

    /**
     * 写入对象值。
     *
     * @param key 缓存键
     * @param value 缓存值
     */
    void set(String key, Object value);

    /**
     * 写入对象值并设置过期时间。
     *
     * @param key 缓存键
     * @param value 缓存值
     * @param ttl 过期时间
     */
    void set(String key, Object value, Duration ttl);

    /**
     * 读取对象值。
     *
     * @param key 缓存键
     * @param valueType 目标类型
     * @param <T> 返回值类型
     * @return 反序列化后的缓存值
     */
    <T> T get(String key, Class<T> valueType);

    /**
     * 写入字符串值。
     *
     * @param key 缓存键
     * @param value 字符串值
     */
    void setString(String key, String value);

    /**
     * 写入字符串值并设置过期时间。
     *
     * @param key 缓存键
     * @param value 字符串值
     * @param ttl 过期时间
     */
    void setString(String key, String value, Duration ttl);

    /**
     * 读取字符串值。
     *
     * @param key 缓存键
     * @return 字符串值
     */
    String getString(String key);

    /**
     * 执行自增操作。
     *
     * @param key 缓存键
     * @param delta 自增步长
     * @return 自增后的结果
     */
    Long increment(String key, long delta);

    /**
     * 删除单个缓存键。
     *
     * @param key 缓存键
     * @return 是否删除成功
     */
    Boolean delete(String key);

    /**
     * 批量删除缓存键。
     *
     * @param keys 缓存键集合
     * @return 删除数量
     */
    Long delete(Collection<String> keys);

    /**
     * 判断缓存键是否存在。
     *
     * @param key 缓存键
     * @return 是否存在
     */
    Boolean hasKey(String key);

    /**
     * 设置缓存键过期时间。
     *
     * @param key 缓存键
     * @param ttl 过期时间
     * @return 是否设置成功
     */
    Boolean expire(String key, Duration ttl);

    /**
     * 写入 Hash 字段值。
     *
     * @param key 缓存键
     * @param hashKey Hash 字段键
     * @param value 字段值
     */
    void putHash(String key, String hashKey, Object value);

    /**
     * 读取 Hash 字段值。
     *
     * @param key 缓存键
     * @param hashKey Hash 字段键
     * @param valueType 目标类型
     * @param <T> 返回值类型
     * @return 反序列化后的字段值
     */
    <T> T getHash(String key, String hashKey, Class<T> valueType);
}
