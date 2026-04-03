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
package com.getboot.webhook.support.validator;

import com.getboot.exception.api.code.CommonErrorCode;
import com.getboot.exception.api.exception.BusinessException;
import com.getboot.webhook.api.resolver.AppSecretResolver;
import com.getboot.webhook.support.security.RequestSignatureChecksumGenerator;
import com.getboot.webhook.spi.WebhookRequestValidationContext;
import com.getboot.webhook.spi.WebhookRequestValidationHook;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

/**
 * Webhook 请求校验器。
 *
 * <p>负责校验请求头完整性、时间戳有效性以及请求体签名。</p>
 *
 * @author qiheng
 */
@Slf4j
public class WebhookRequestValidator {

    /**
     * 应用密钥解析器。
     */
    private final AppSecretResolver appSecretResolver;

    /**
     * 扩展校验钩子集合。
     */
    private final List<WebhookRequestValidationHook> validationHooks;

    /**
     * 创建仅使用默认校验逻辑的请求校验器。
     *
     * @param appSecretResolver 应用密钥解析器
     */
    public WebhookRequestValidator(AppSecretResolver appSecretResolver) {
        this(appSecretResolver, List.of());
    }

    /**
     * 创建支持扩展钩子的请求校验器。
     *
     * @param appSecretResolver 应用密钥解析器
     * @param validationHooks 扩展校验钩子集合
     */
    public WebhookRequestValidator(
            AppSecretResolver appSecretResolver,
            List<WebhookRequestValidationHook> validationHooks) {
        this.appSecretResolver = appSecretResolver;
        this.validationHooks = validationHooks == null ? List.of() : List.copyOf(validationHooks);
    }

    /**
     * 校验请求头完整性、时间戳有效性与请求签名。
     *
     * @param checksum 请求签名
     * @param appKey 调用方应用标识
     * @param time 请求时间戳
     * @param requestBody 原始请求体
     */
    public void validateRequest(String checksum, String appKey, String time, String requestBody) {
        validateRequiredHeaders(checksum, appKey, time);
        validateTimestamp(time);
        validateChecksum(checksum, appKey, time, requestBody);
        applyValidationHooks(checksum, appKey, time, requestBody, true);
    }

    /**
     * 校验请求头完整性与请求签名，但跳过时间戳时效检查。
     *
     * @param checksum 请求签名
     * @param appKey 调用方应用标识
     * @param time 请求时间戳
     * @param requestBody 原始请求体
     */
    public void validateRequestNoTimestamp(String checksum, String appKey, String time, String requestBody) {
        validateRequiredHeaders(checksum, appKey, time);
        validateChecksum(checksum, appKey, time, requestBody);
        applyValidationHooks(checksum, appKey, time, requestBody, false);
    }

    /**
     * 校验请求头中必需字段是否齐全。
     *
     * @param checksum 请求签名
     * @param appKey 调用方应用标识
     * @param time 请求时间戳
     */
    private void validateRequiredHeaders(String checksum, String appKey, String time) {
        if (Objects.isNull(checksum) || Objects.isNull(appKey) || Objects.isNull(time)) {
            throw new BusinessException(CommonErrorCode.MISSING_REQUIRED_HEADERS);
        }
    }

    /**
     * 校验请求时间戳是否仍在允许窗口内。
     *
     * @param timeHeader 请求时间戳
     */
    private void validateTimestamp(String timeHeader) {
        long requestTime;
        try {
            requestTime = Long.parseLong(timeHeader);
        } catch (NumberFormatException ex) {
            throw new BusinessException(CommonErrorCode.PARAM_ERROR);
        }
        if (Math.abs(System.currentTimeMillis() / 1000 - requestTime) > 300) {
            throw new BusinessException(CommonErrorCode.EXPIRED_REQUEST);
        }
    }

    /**
     * 校验请求签名是否与服务端计算结果一致。
     *
     * @param checksum 请求签名
     * @param appKey 调用方应用标识
     * @param time 请求时间戳
     * @param requestBody 原始请求体
     */
    private void validateChecksum(String checksum, String appKey, String time, String requestBody) {
        String appSecret = appSecretResolver.getAppSecret(appKey);
        String safeRequestBody = StringUtils.hasText(requestBody) ? requestBody : "";
        String md5 = DigestUtils.md5DigestAsHex(safeRequestBody.getBytes(StandardCharsets.UTF_8));
        String expectedChecksum = RequestSignatureChecksumGenerator.encode(appSecret, md5, time);

        if (log.isDebugEnabled()) {
            log.debug("Request signature verification. appKey={}, timestamp={}, bodyMd5={}", appKey, time, md5);
        }

        if (!checksum.equals(expectedChecksum)) {
            throw new BusinessException(CommonErrorCode.CHECKSUM_VALIDATION_FAILED);
        }
    }

    /**
     * 将默认校验结果传递给业务扩展钩子继续处理。
     *
     * @param checksum 请求签名
     * @param appKey 调用方应用标识
     * @param time 请求时间戳
     * @param requestBody 原始请求体
     * @param timestampValidated 是否已校验时间戳
     */
    private void applyValidationHooks(
            String checksum,
            String appKey,
            String time,
            String requestBody,
            boolean timestampValidated) {
        if (validationHooks.isEmpty()) {
            return;
        }
        WebhookRequestValidationContext context = new WebhookRequestValidationContext(
                checksum,
                appKey,
                time,
                requestBody,
                timestampValidated
        );
        validationHooks.forEach(hook -> hook.validate(context));
    }
}
