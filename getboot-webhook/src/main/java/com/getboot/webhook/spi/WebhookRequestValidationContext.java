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
package com.getboot.webhook.spi;

/**
 * Webhook 请求校验上下文。
 *
 * <p>用于向扩展校验器暴露当前请求的基础校验上下文信息。</p>
 *
 * @param checksum 请求签名
 * @param appKey 调用方应用标识
 * @param time 请求时间戳
 * @param requestBody 原始请求体
 * @param timestampValidated 是否已执行时间戳校验
 * @author qiheng
 */
public record WebhookRequestValidationContext(
        String checksum,
        String appKey,
        String time,
        String requestBody,
        boolean timestampValidated) {
}
