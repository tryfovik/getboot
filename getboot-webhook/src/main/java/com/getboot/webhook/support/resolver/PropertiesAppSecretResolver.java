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
package com.getboot.webhook.support.resolver;

import com.getboot.exception.api.code.CommonErrorCode;
import com.getboot.exception.api.exception.BusinessException;
import com.getboot.webhook.api.properties.WebhookSecurityProperties;
import com.getboot.webhook.api.resolver.AppSecretResolver;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 基于配置项的应用密钥解析器。
 *
 * <p>从 Webhook 安全配置中读取调用方应用标识与签名密钥映射关系。</p>
 *
 * @author qiheng
 */
public class PropertiesAppSecretResolver implements AppSecretResolver {

    /**
     * Webhook 安全配置。
     */
    private final WebhookSecurityProperties securityProperties;

    /**
     * 创建基于配置的应用密钥解析器。
     *
     * @param securityProperties Webhook 安全配置
     */
    public PropertiesAppSecretResolver(WebhookSecurityProperties securityProperties) {
        this.securityProperties = securityProperties;
    }

    /**
     * 从配置列表中解析调用方签名密钥。
     *
     * @param appKey 调用方应用标识
     * @return 调用方签名密钥
     */
    @Override
    public String getAppSecret(String appKey) {
        List<WebhookSecurityProperties.AppCredentials> credentialsList = securityProperties.getCredentials();
        if (appKey == null || credentialsList == null || credentialsList.isEmpty()) {
            throw new BusinessException(CommonErrorCode.INVALID_APP_KEY);
        }

        for (WebhookSecurityProperties.AppCredentials credentials : credentialsList) {
            if (credentials == null) {
                continue;
            }
            if (appKey.equals(credentials.getAppKey())) {
                if (!StringUtils.hasText(credentials.getAppSecret())) {
                    throw new BusinessException(CommonErrorCode.INVALID_APP_KEY);
                }
                return credentials.getAppSecret().trim();
            }
        }
        throw new BusinessException(CommonErrorCode.INVALID_APP_KEY);
    }
}
