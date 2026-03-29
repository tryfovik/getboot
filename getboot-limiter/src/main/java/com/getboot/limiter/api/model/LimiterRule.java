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

import lombok.Data;

/**
 * 命名限流规则。
 *
 * <p>用于描述单个限流器的基础配额信息，供能力层注册表与具体算法实现共享。</p>
 *
 * @author qiheng
 */
@Data
public class LimiterRule {

    /**
     * 时间窗口内允许的最大请求数。
     */
    private long rate = 10;

    /**
     * 时间窗口大小。
     */
    private long interval = 1;

    /**
     * 时间窗口单位，例如 SECONDS、MINUTES。
     */
    private String intervalUnit = "SECONDS";
}
