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
import com.getboot.limiter.api.limiter.RateLimiter;
import com.getboot.webhook.api.processor.WebhookRequestProcessor;
import com.getboot.webhook.infrastructure.servlet.filter.CachedBodyHttpServletRequest;
import com.getboot.webhook.support.validator.WebhookRequestValidator;
import jakarta.servlet.http.HttpServletRequest;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
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

    private static final String PROCESSED_MARKER = "PROCESSED";

    private final WebhookRequestValidator webhookRequestValidator;
    private final RateLimiter rateLimiter;
    private final RedissonClient redissonClient;
    private final StringRedisTemplate stringRedisTemplate;

    public DefaultWebhookRequestProcessor(
            WebhookRequestValidator webhookRequestValidator,
            RateLimiter rateLimiter,
            RedissonClient redissonClient,
            StringRedisTemplate stringRedisTemplate) {
        this.webhookRequestValidator = webhookRequestValidator;
        this.rateLimiter = rateLimiter;
        this.redissonClient = redissonClient;
        this.stringRedisTemplate = stringRedisTemplate;
    }

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
        RLock lock = redissonClient.getLock(processedKey);
        try {
            if (!lock.tryLock(0, TimeUnit.SECONDS)) {
                throw new BusinessException(CommonErrorCode.REQUEST_PROCESSING);
            }

            if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(processedKey))) {
                throw new BusinessException(CommonErrorCode.REQUEST_ALREADY_PROCESSED);
            }

            T result = processor.get();
            stringRedisTemplate.opsForValue().set(processedKey, PROCESSED_MARKER, Duration.ofHours(24));
            return result;
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new BusinessException(CommonErrorCode.REQUEST_PROCESSING);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

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

    private String getRawRequestBody(HttpServletRequest request) {
        if (request instanceof CachedBodyHttpServletRequest wrapper) {
            return new String(wrapper.getCachedBody(), StandardCharsets.UTF_8);
        }
        throw new BusinessException(
                "Request body cache is unavailable. Ensure CachingRequestBodyFilter is registered before request validation.",
                CommonErrorCode.ERROR
        );
    }
}
