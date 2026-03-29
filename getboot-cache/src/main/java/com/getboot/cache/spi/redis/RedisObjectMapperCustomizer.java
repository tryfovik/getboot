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

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Redis ObjectMapper 定制器。
 *
 * <p>业务方可通过注册该类型 Bean，参与 GetBoot Redis 序列化 ObjectMapper 的构建过程。</p>
 *
 * @author qiheng
 */
@FunctionalInterface
public interface RedisObjectMapperCustomizer {

    /**
     * 自定义 Redis ObjectMapper。
     *
     * @param objectMapper Redis ObjectMapper
     */
    void customize(ObjectMapper objectMapper);
}
