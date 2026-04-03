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
package com.getboot.webhook.infrastructure.servlet.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;

/**
 * 请求体缓存过滤器。
 *
 * <p>用于在请求进入业务链路前包装请求对象，确保请求体可重复读取。</p>
 *
 * @author qiheng
 */
public class CachingRequestBodyFilter implements Filter {

    /**
     * 使用支持重复读取的请求包装器继续传递过滤链。
     *
     * @param request 原始请求
     * @param response 原始响应
     * @param chain 过滤器链
     * @throws IOException 读取请求体失败时抛出
     * @throws ServletException 过滤链执行失败时抛出
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        CachedBodyHttpServletRequest wrappedRequest = new CachedBodyHttpServletRequest(httpRequest);
        chain.doFilter(wrappedRequest, response);
    }
}
