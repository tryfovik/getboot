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
package com.getboot.limiter.api.model;

/**
 * 限流算法类型。
 *
 * <p>用于在统一注册表中路由到不同算法实现。</p>
 *
 * @author qiheng
 */
public enum LimiterAlgorithm {

    /**
     * 滑动窗口计数。
     */
    SLIDING_WINDOW,

    /**
     * 令牌桶。
     */
    TOKEN_BUCKET
}
