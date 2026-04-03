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
package com.getboot.webhook.support.processor;

import com.getboot.exception.api.code.CommonErrorCode;
import com.getboot.exception.api.exception.BusinessException;
import com.getboot.idempotency.api.model.IdempotencyRecord;
import com.getboot.idempotency.api.model.IdempotencyStatus;
import com.getboot.idempotency.spi.IdempotencyStore;
import com.getboot.limiter.api.limiter.RateLimiter;
import com.getboot.webhook.api.processor.WebhookRequestProcessor;
import com.getboot.webhook.infrastructure.servlet.filter.CachedBodyHttpServletRequest;
import com.getboot.webhook.support.validator.WebhookRequestValidator;
import jakarta.servlet.http.HttpServletRequest;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Webhook 请求处理器默认实现。
 *
 * <p>负责串联验签、限流、幂等校验与分布式锁控制，统一管理事件请求处理流程。</p>
 *
 * @author qiheng
 */
public class DefaultWebhookRequestProcessor implements WebhookRequestProcessor {

    /**
     * Webhook 幂等记录保留时长。
     */
    private static final Duration WEBHOOK_IDEMPOTENCY_TTL = Duration.ofHours(24);

    /**
     * Webhook 请求校验器。
     */
    private final WebhookRequestValidator webhookRequestValidator;

    /**
     * 限流器。
     */
    private final RateLimiter rateLimiter;

    /**
     * 幂等存储。
     */
    private final IdempotencyStore idempotencyStore;

    /**
     * 创建默认 Webhook 请求处理器。
     *
     * @param webhookRequestValidator 请求校验器
     * @param rateLimiter 限流器
     * @param idempotencyStore 幂等存储
     */
    public DefaultWebhookRequestProcessor(
            WebhookRequestValidator webhookRequestValidator,
            RateLimiter rateLimiter,
            IdempotencyStore idempotencyStore) {
        this.webhookRequestValidator = webhookRequestValidator;
        this.rateLimiter = rateLimiter;
        this.idempotencyStore = idempotencyStore;
    }

    /**
     * 执行带幂等控制的 Webhook 请求处理流程。
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
    @Override
    public <T> T handle(
            String appKey,
            String rateLimitKey,
            int rateLimit,
            String lockPrefix,
            String checksum,
            String time,
            HttpServletRequest rawRequest,
            Supplier<T> processor,
            Function<String, String> fingerprintGenerator) {
        if (!rateLimiter.tryAcquire(rateLimitKey, rateLimit, 1)) {
            throw new BusinessException(CommonErrorCode.TOO_MANY_REQUESTS);
        }

        webhookRequestValidator.validateRequest(checksum, appKey, time, getRawRequestBody(rawRequest));

        String fingerprint = fingerprintGenerator.apply(appKey);
        String processedKey = lockPrefix + fingerprint;
        IdempotencyRecord existingRecord = idempotencyStore.get(processedKey);
        if (existingRecord != null) {
            return handleDuplicateRecord(existingRecord);
        }

        if (!idempotencyStore.markProcessing(processedKey, WEBHOOK_IDEMPOTENCY_TTL)) {
            IdempotencyRecord latestRecord = idempotencyStore.get(processedKey);
            if (latestRecord != null) {
                return handleDuplicateRecord(latestRecord);
            }
            throw new BusinessException(CommonErrorCode.REQUEST_PROCESSING);
        }

        try {
            T result = processor.get();
            idempotencyStore.markCompleted(processedKey, result, WEBHOOK_IDEMPOTENCY_TTL);
            return result;
        } catch (RuntimeException exception) {
            idempotencyStore.delete(processedKey);
            throw exception;
        }
    }

    /**
     * 执行仅需验签与限流的查询型 Webhook 请求处理流程。
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
    @Override
    public <T> T handleQuery(
            String appKey,
            String rateLimitKey,
            int rateLimit,
            String checksum,
            String time,
            HttpServletRequest rawRequest,
            Supplier<T> processor) {
        if (!rateLimiter.tryAcquire(rateLimitKey, rateLimit, 1)) {
            throw new BusinessException(CommonErrorCode.TOO_MANY_REQUESTS);
        }

        webhookRequestValidator.validateRequest(checksum, appKey, time, getRawRequestBody(rawRequest));
        return processor.get();
    }

    /**
     * 提取缓存后的原始请求体内容。
     *
     * @param request 请求对象
     * @return 原始请求体文本
     */
    private String getRawRequestBody(HttpServletRequest request) {
        if (request instanceof CachedBodyHttpServletRequest wrapper) {
            return new String(wrapper.getCachedBody(), StandardCharsets.UTF_8);
        }
        throw new BusinessException(
                "Request body cache is unavailable. Ensure CachingRequestBodyFilter is registered before request validation.",
                CommonErrorCode.ERROR
        );
    }

    /**
     * 根据幂等记录返回既有结果或处理中状态。
     *
     * @param record 幂等记录
     * @return 已完成请求的处理结果
     * @param <T> 返回值类型
     */
    @SuppressWarnings("unchecked")
    private <T> T handleDuplicateRecord(IdempotencyRecord record) {
        if (record.getStatus() == IdempotencyStatus.COMPLETED) {
            return (T) record.getResult();
        }
        throw new BusinessException(CommonErrorCode.REQUEST_PROCESSING);
    }
}
