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
package com.getboot.cache.spi.redis;

import org.springframework.data.redis.core.RedisTemplate;

/**
 * GetBoot RedisTemplate 定制器。
 *
 * <p>业务方可通过注册该类型 Bean，参与默认 RedisTemplate 的最终定制过程。</p>
 *
 * @author qiheng
 */
@FunctionalInterface
public interface GetbootRedisTemplateCustomizer {

    /**
     * 自定义 RedisTemplate。
     *
     * @param redisTemplate 默认 RedisTemplate
     */
    void customize(RedisTemplate<String, Object> redisTemplate);
}
