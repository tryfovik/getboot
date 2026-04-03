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
package com.getboot.webhook.api.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Webhook 安全处理配置。
 *
 * <p>用于定义是否启用验签处理，以及允许访问的应用凭证列表。</p>
 *
 * @author qiheng
 */
@Data
@ConfigurationProperties(prefix = "getboot.webhook.security")
public class WebhookSecurityProperties {

    /**
     * 是否启用 Webhook 安全处理。
     */
    private boolean enabled = false;

    /**
     * 允许访问的应用凭证列表。
     */
    private List<AppCredentials> credentials = new ArrayList<>();

    /**
     * 判断是否已经配置应用凭证。
     *
     * @return 是否存在至少一组可用凭证
     */
    public boolean hasCredentials() {
        return credentials != null && !credentials.isEmpty();
    }

    /**
     * 单个应用凭证配置。
     *
     * @author qiheng
     */
    @Data
    public static class AppCredentials {

        /**
         * 凭证名称，用于区分不同调用方。
         */
        private String name;

        /**
         * 调用方应用标识。
         */
        private String appKey;

        /**
         * 调用方签名密钥。
         */
        private String appSecret;
    }
}
