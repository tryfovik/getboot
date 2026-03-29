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

    private final AppSecretResolver appSecretResolver;
    private final List<WebhookRequestValidationHook> validationHooks;

    public WebhookRequestValidator(AppSecretResolver appSecretResolver) {
        this(appSecretResolver, List.of());
    }

    public WebhookRequestValidator(
            AppSecretResolver appSecretResolver,
            List<WebhookRequestValidationHook> validationHooks) {
        this.appSecretResolver = appSecretResolver;
        this.validationHooks = validationHooks == null ? List.of() : List.copyOf(validationHooks);
    }

    /**
     * Validates request integrity and freshness.
     */
    public void validateRequest(String checksum, String appKey, String time, String requestBody) {
        validateRequiredHeaders(checksum, appKey, time);
        validateTimestamp(time);
        validateChecksum(checksum, appKey, time, requestBody);
        applyValidationHooks(checksum, appKey, time, requestBody, true);
    }

    /**
     * Validates request integrity without checking timestamp freshness.
     */
    public void validateRequestNoTimestamp(String checksum, String appKey, String time, String requestBody) {
        validateRequiredHeaders(checksum, appKey, time);
        validateChecksum(checksum, appKey, time, requestBody);
        applyValidationHooks(checksum, appKey, time, requestBody, false);
    }

    private void validateRequiredHeaders(String checksum, String appKey, String time) {
        if (Objects.isNull(checksum) || Objects.isNull(appKey) || Objects.isNull(time)) {
            throw new BusinessException(CommonErrorCode.MISSING_REQUIRED_HEADERS);
        }
    }

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
