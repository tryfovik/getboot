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
package com.getboot.observability.spi;

import jakarta.servlet.http.HttpServletRequest;

/**
 * TraceId 解析器。
 *
 * <p>业务方可通过注册该类型 Bean，优先从链路系统或自定义上下文中解析主 TraceId。</p>
 *
 * @author qiheng
 */
@FunctionalInterface
public interface TraceIdResolver {

    /**
     * 解析 TraceId。
     *
     * @param request 当前请求
     * @return 解析得到的 TraceId，未命中时返回 {@code null}
     */
    String resolve(HttpServletRequest request);
}
