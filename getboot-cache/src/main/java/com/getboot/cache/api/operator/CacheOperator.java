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

    void set(String key, Object value);

    void set(String key, Object value, Duration ttl);

    <T> T get(String key, Class<T> valueType);

    void setString(String key, String value);

    void setString(String key, String value, Duration ttl);

    String getString(String key);

    Long increment(String key, long delta);

    Boolean delete(String key);

    Long delete(Collection<String> keys);

    Boolean hasKey(String key);

    Boolean expire(String key, Duration ttl);

    void putHash(String key, String hashKey, Object value);

    <T> T getHash(String key, String hashKey, Class<T> valueType);
}
