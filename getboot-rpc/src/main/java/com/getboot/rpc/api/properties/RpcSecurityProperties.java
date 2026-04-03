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
package com.getboot.rpc.api.properties;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * RPC 安全增强配置。
 *
 * <p>包含鉴权配置与序列化安全配置两部分。</p>
 *
 * @author qiheng
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "getboot.rpc.security")
public class RpcSecurityProperties {

    /**
     * RPC 鉴权配置。
     */
    private Authentication authentication = new Authentication();

    /**
     * Dubbo 序列化安全配置。
     */
    private Serialization serialization = new Serialization();

    /**
     * RPC 鉴权配置。
     *
     * @author qiheng
     */
    @Getter
    @Setter
    public static class Authentication {

        /**
         * 是否启用 RPC 鉴权。
         */
        private boolean enabled = true;

        /**
         * 请求时间戳允许偏差，单位秒。
         */
        private long allowedClockSkewSeconds = 300;

        /**
         * 消费方凭证配置。
         */
        private Consumer consumer = new Consumer();

        /**
         * 提供方校验配置。
         */
        private Provider provider = new Provider();

        /**
         * 不参与鉴权的服务名前缀。
         */
        @Setter(AccessLevel.NONE)
        private List<String> excludedServicePrefixes = new ArrayList<>(List.of("org.apache.dubbo."));

        /**
         * 设置不参与鉴权的服务名前缀列表，并复制入参内容。
         *
         * @param excludedServicePrefixes 服务名前缀列表
         */
        public void setExcludedServicePrefixes(List<String> excludedServicePrefixes) {
            this.excludedServicePrefixes = excludedServicePrefixes != null
                    ? new ArrayList<>(excludedServicePrefixes)
                    : new ArrayList<>(Collections.singletonList("org.apache.dubbo."));
        }
    }

    /**
     * RPC 消费方凭证配置。
     *
     * @author qiheng
     */
    @Getter
    @Setter
    public static class Consumer {

        /**
         * 消费方应用标识。
         */
        private String appId;

        /**
         * 消费方签名密钥。
         */
        private String appSecret;

        /**
         * 判断消费方凭证是否已完整配置。
         *
         * @return appId 与 appSecret 是否同时存在
         */
        public boolean isConfigured() {
            return StringUtils.hasText(appId) && StringUtils.hasText(appSecret);
        }

        /**
         * 判断消费方凭证是否至少配置了一部分内容。
         *
         * @return 是否存在任意非空字段
         */
        public boolean hasAnyConfiguredValue() {
            return StringUtils.hasText(appId) || StringUtils.hasText(appSecret);
        }
    }

    /**
     * RPC 提供方鉴权配置。
     *
     * @author qiheng
     */
    @Getter
    @Setter
    public static class Provider {

        /**
         * 是否强制要求提供方校验认证信息。
         */
        private boolean required = false;

        /**
         * 调用方应用标识与签名密钥映射。
         */
        @Setter(AccessLevel.NONE)
        private Map<String, String> credentials = new LinkedHashMap<>();

        /**
         * 设置调用方凭证映射，并复制入参内容。
         *
         * @param credentials 调用方凭证映射
         */
        public void setCredentials(Map<String, String> credentials) {
            this.credentials = credentials != null ? new LinkedHashMap<>(credentials) : new LinkedHashMap<>();
        }
    }

    /**
     * Dubbo 序列化安全配置。
     *
     * @author qiheng
     */
    @Getter
    @Setter
    public static class Serialization {

        /**
         * 是否启用序列化安全校验。
         */
        private boolean enabled = true;

        /**
         * 序列化安全校验级别。
         */
        private String checkStatus = "STRICT";

        /**
         * 是否要求对象实现 Serializable。
         */
        private boolean checkSerializable = true;

        /**
         * 永远允许反序列化的包前缀列表。
         */
        @Setter(AccessLevel.NONE)
        private List<String> allowedPrefixes = new ArrayList<>(List.of("com.getboot"));

        /**
         * 设置永远允许反序列化的包前缀列表，并复制入参内容。
         *
         * @param allowedPrefixes 包前缀列表
         */
        public void setAllowedPrefixes(List<String> allowedPrefixes) {
            this.allowedPrefixes = allowedPrefixes != null
                    ? new ArrayList<>(allowedPrefixes)
                    : new ArrayList<>(Collections.singletonList("com.getboot"));
        }
    }
}
