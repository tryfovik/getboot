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
package com.getboot.webhook.api.processor;

import jakarta.servlet.http.HttpServletRequest;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Webhook 请求处理器。
 *
 * <p>负责串联验签、限流、幂等校验与分布式锁控制，统一管理事件请求处理流程。</p>
 *
 * @author qiheng
 */
public interface WebhookRequestProcessor {

    /**
     * 处理需要幂等控制的 Webhook 请求。
     *
     * @param appKey 调用方应用标识
     * @param rateLimitKey 限流键
     * @param rateLimit 限流阈值
     * @param lockPrefix 幂等键前缀
     * @param checksum 请求签名
     * @param time 请求时间戳
     * @param rawRequest 原始请求对象
     * @param processor 业务处理逻辑
     * @param fingerprintGenerator 指纹生成器
     * @return 业务处理结果
     * @param <T> 返回值类型
     */
    <T> T handle(
            String appKey,
            String rateLimitKey,
            int rateLimit,
            String lockPrefix,
            String checksum,
            String time,
            HttpServletRequest rawRequest,
            Supplier<T> processor,
            Function<String, String> fingerprintGenerator);

    /**
     * 处理仅需验签与限流的查询型 Webhook 请求。
     *
     * @param appKey 调用方应用标识
     * @param rateLimitKey 限流键
     * @param rateLimit 限流阈值
     * @param checksum 请求签名
     * @param time 请求时间戳
     * @param rawRequest 原始请求对象
     * @param processor 业务处理逻辑
     * @return 业务处理结果
     * @param <T> 返回值类型
     */
    <T> T handleQuery(
            String appKey,
            String rateLimitKey,
            int rateLimit,
            String checksum,
            String time,
            HttpServletRequest rawRequest,
            Supplier<T> processor);
}
