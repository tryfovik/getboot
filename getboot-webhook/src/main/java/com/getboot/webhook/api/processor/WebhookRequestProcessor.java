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

    <T> T handleQuery(
            String appKey,
            String rateLimitKey,
            int rateLimit,
            String checksum,
            String time,
            HttpServletRequest rawRequest,
            Supplier<T> processor);
}
