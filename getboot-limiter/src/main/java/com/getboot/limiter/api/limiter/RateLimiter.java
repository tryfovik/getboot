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
package com.getboot.limiter.api.limiter;

/**
 * 限流器抽象接口。
 *
 * <p>定义统一的限流许可申请能力，便于不同实现按相同方式接入。</p>
 *
 * @author qiheng
 */
public interface RateLimiter {

    /**
     * 判断一个key是否可以通过
     *
     * @param key 限流的key
     * @param limit 限流的数量
     * @param windowSize 窗口大小，单位为秒
     */
    boolean tryAcquire(String key, int limit, int windowSize);
}
