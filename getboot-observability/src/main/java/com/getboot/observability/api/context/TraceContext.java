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
package com.getboot.observability.api.context;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Trace 上下文。
 *
 * <p>用于向扩展定制器暴露当前请求链路的 Trace 基础信息。</p>
 *
 * @param traceId 当前 TraceId
 * @param request 当前请求
 * @param response 当前响应
 * @author qiheng
 */
public record TraceContext(
        String traceId,
        HttpServletRequest request,
        HttpServletResponse response) {
}
