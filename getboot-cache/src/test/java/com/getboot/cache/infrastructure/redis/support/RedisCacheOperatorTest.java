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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Collection;
import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link RedisCacheOperator} 测试。
 *
 * @author qiheng
 */
class RedisCacheOperatorTest {

    /**
     * 验证对象值与 Hash 值操作会委托到底层模板，并完成类型转换。
     */
    @Test
    @SuppressWarnings("unchecked")
    void shouldDelegateObjectAndHashOperationsAndConvertValues() {
        ValueOperations<String, Object> valueOperations = mock(ValueOperations.class);
        HashOperations<String, Object, Object> hashOperations = mock(HashOperations.class);
        StubRedisTemplate redisTemplate = new StubRedisTemplate(valueOperations, hashOperations);
        StubStringRedisTemplate stringRedisTemplate = new StubStringRedisTemplate(mock(ValueOperations.class));
        when(valueOperations.get("user:profile")).thenReturn(Map.of("name", "Alice", "age", 18));
        when(valueOperations.get("user:direct")).thenReturn(new UserCache("Bob", 20));
        when(valueOperations.get("user:missing")).thenReturn(null);
        when(hashOperations.get("user:hash", "profile")).thenReturn(Map.of("name", "Carol", "age", 22));

        RedisCacheOperator operator = new RedisCacheOperator(redisTemplate, stringRedisTemplate, new ObjectMapper());

        operator.set("user:profile", Map.of("name", "Alice"));
        operator.set("user:ttl", Map.of("name", "Bob"), Duration.ofMinutes(5));
        operator.putHash("user:hash", "profile", Map.of("name", "Carol"));

        UserCache profile = operator.get("user:profile", UserCache.class);
        UserCache direct = operator.get("user:direct", UserCache.class);
        UserCache hashProfile = operator.getHash("user:hash", "profile", UserCache.class);

        verify(valueOperations).set("user:profile", Map.of("name", "Alice"));
        verify(valueOperations).set("user:ttl", Map.of("name", "Bob"), Duration.ofMinutes(5));
        verify(hashOperations).put("user:hash", "profile", Map.of("name", "Carol"));
        assertEquals("Alice", profile.getName());
        assertEquals(18, profile.getAge());
        assertSame(direct, operator.get("user:direct", UserCache.class));
        assertEquals("Carol", hashProfile.getName());
        assertEquals(22, hashProfile.getAge());
        assertNull(operator.get("user:missing", UserCache.class));
    }

    /**
     * 验证字符串、删除、存在性与过期时间操作会委托到底层模板。
     */
    @Test
    @SuppressWarnings("unchecked")
    void shouldDelegateStringAndMetadataOperations() {
        StubRedisTemplate redisTemplate = new StubRedisTemplate(
                mock(ValueOperations.class),
                mock(HashOperations.class)
        );
        ValueOperations<String, String> stringValueOperations = mock(ValueOperations.class);
        StubStringRedisTemplate stringRedisTemplate = new StubStringRedisTemplate(stringValueOperations);
        when(stringValueOperations.get("counter")).thenReturn("2");
        when(stringValueOperations.increment("counter", 3L)).thenReturn(5L);
        redisTemplate.singleDeleteResult = true;
        redisTemplate.batchDeleteResult = 2L;
        redisTemplate.hasKeyResult = true;
        redisTemplate.expireResult = true;

        RedisCacheOperator operator = new RedisCacheOperator(redisTemplate, stringRedisTemplate, new ObjectMapper());

        operator.setString("counter", "2");
        operator.setString("counter", "2", Duration.ofMinutes(1));

        assertEquals("2", operator.getString("counter"));
        assertEquals(5L, operator.increment("counter", 3L));
        assertTrue(operator.delete("cache:1"));
        assertEquals(2L, operator.delete(List.of("cache:1", "cache:2")));
        assertTrue(operator.hasKey("cache:1"));
        assertTrue(operator.expire("cache:1", Duration.ofMinutes(1)));

        verify(stringValueOperations).set("counter", "2");
        verify(stringValueOperations).set("counter", "2", Duration.ofMinutes(1));
        assertEquals("cache:1", redisTemplate.deletedKey);
        assertEquals(List.of("cache:1", "cache:2"), redisTemplate.deletedKeys);
        assertEquals("cache:1", redisTemplate.checkedKey);
        assertEquals("cache:1", redisTemplate.expireKey);
        assertEquals(Duration.ofMinutes(1), redisTemplate.expireTtl);
    }

    /**
     * 验证非法参数会触发边界校验异常。
     */
    @Test
    void shouldRejectInvalidArguments() {
        RedisCacheOperator operator = new RedisCacheOperator(
                new StubRedisTemplate(mock(ValueOperations.class), mock(HashOperations.class)),
                new StubStringRedisTemplate(mock(ValueOperations.class)),
                new ObjectMapper()
        );

        assertAll(
                () -> assertThrows(IllegalArgumentException.class, () -> operator.set(" ", "value")),
                () -> assertThrows(IllegalArgumentException.class, () -> operator.set("key", "value", Duration.ZERO)),
                () -> assertThrows(IllegalArgumentException.class, () -> operator.setString("key", "value", null)),
                () -> assertThrows(IllegalArgumentException.class, () -> operator.delete(List.of())),
                () -> assertThrows(IllegalArgumentException.class, () -> operator.get("key", null)),
                () -> assertThrows(IllegalArgumentException.class, () -> operator.getHash("key", "field", null)),
                () -> assertThrows(IllegalArgumentException.class, () -> operator.putHash("key", " ", "value"))
        );
    }

    /**
     * 测试用缓存对象。
     */
    private static final class UserCache {

        /**
         * 用户名。
         */
        private String name;

        /**
         * 年龄。
         */
        private int age;

        /**
         * 默认构造函数，供 Jackson 转换使用。
         */
        private UserCache() {
        }

        /**
         * 创建测试缓存对象。
         *
         * @param name 用户名
         * @param age 年龄
         */
        private UserCache(String name, int age) {
            this.name = name;
            this.age = age;
        }

        /**
         * 返回用户名。
         *
         * @return 用户名
         */
        public String getName() {
            return name;
        }

        /**
         * 设置用户名。
         *
         * @param name 用户名
         */
        public void setName(String name) {
            this.name = name;
        }

        /**
         * 返回年龄。
         *
         * @return 年龄
         */
        public int getAge() {
            return age;
        }

        /**
         * 设置年龄。
         *
         * @param age 年龄
         */
        public void setAge(int age) {
            this.age = age;
        }
    }

    /**
     * 测试用对象 RedisTemplate。
     */
    private static final class StubRedisTemplate extends RedisTemplate<String, Object> {

        /**
         * Value 操作接口。
         */
        private final ValueOperations<String, Object> valueOperations;

        /**
         * Hash 操作接口。
         */
        private final HashOperations<String, Object, Object> hashOperations;

        /**
         * 单键删除返回值。
         */
        private Boolean singleDeleteResult;

        /**
         * 批量删除返回值。
         */
        private Long batchDeleteResult;

        /**
         * 存在性检查返回值。
         */
        private Boolean hasKeyResult;

        /**
         * 过期设置返回值。
         */
        private Boolean expireResult;

        /**
         * 最近一次单键删除参数。
         */
        private String deletedKey;

        /**
         * 最近一次批量删除参数。
         */
        private Collection<String> deletedKeys;

        /**
         * 最近一次存在性检查参数。
         */
        private String checkedKey;

        /**
         * 最近一次过期设置键。
         */
        private String expireKey;

        /**
         * 最近一次过期设置时长。
         */
        private Duration expireTtl;

        /**
         * 创建测试模板。
         *
         * @param valueOperations Value 操作接口
         * @param hashOperations Hash 操作接口
         */
        private StubRedisTemplate(
                ValueOperations<String, Object> valueOperations,
                HashOperations<String, Object, Object> hashOperations
        ) {
            this.valueOperations = valueOperations;
            this.hashOperations = hashOperations;
        }

        /**
         * 返回 Value 操作接口。
         *
         * @return Value 操作接口
         */
        @Override
        public ValueOperations<String, Object> opsForValue() {
            return valueOperations;
        }

        /**
         * 返回 Hash 操作接口。
         *
         * @return Hash 操作接口
         */
        @Override
        public HashOperations<String, Object, Object> opsForHash() {
            return hashOperations;
        }

        /**
         * 记录单键删除请求。
         *
         * @param key 缓存键
         * @return 预置结果
         */
        @Override
        public Boolean delete(String key) {
            this.deletedKey = key;
            return singleDeleteResult;
        }

        /**
         * 记录批量删除请求。
         *
         * @param keys 缓存键集合
         * @return 预置结果
         */
        @Override
        public Long delete(Collection<String> keys) {
            this.deletedKeys = keys;
            return batchDeleteResult;
        }

        /**
         * 记录存在性检查请求。
         *
         * @param key 缓存键
         * @return 预置结果
         */
        @Override
        public Boolean hasKey(String key) {
            this.checkedKey = key;
            return hasKeyResult;
        }

        /**
         * 记录过期时间设置请求。
         *
         * @param key 缓存键
         * @param timeout 过期时间
         * @return 预置结果
         */
        @Override
        public Boolean expire(String key, Duration timeout) {
            this.expireKey = key;
            this.expireTtl = timeout;
            return expireResult;
        }
    }

    /**
     * 测试用字符串 RedisTemplate。
     */
    private static final class StubStringRedisTemplate extends StringRedisTemplate {

        /**
         * Value 操作接口。
         */
        private final ValueOperations<String, String> valueOperations;

        /**
         * 创建测试模板。
         *
         * @param valueOperations Value 操作接口
         */
        private StubStringRedisTemplate(ValueOperations<String, String> valueOperations) {
            this.valueOperations = valueOperations;
        }

        /**
         * 返回 Value 操作接口。
         *
         * @return Value 操作接口
         */
        @Override
        public ValueOperations<String, String> opsForValue() {
            return valueOperations;
        }
    }
}
