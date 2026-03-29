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

    public Authentication getAuthentication() {
        return authentication;
    }

    public void setAuthentication(Authentication authentication) {
        this.authentication = authentication;
    }

    public Serialization getSerialization() {
        return serialization;
    }

    public void setSerialization(Serialization serialization) {
        this.serialization = serialization;
    }

    /**
     * RPC 鉴权配置。
     */
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
        private List<String> excludedServicePrefixes = new ArrayList<>(List.of("org.apache.dubbo."));

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public long getAllowedClockSkewSeconds() {
            return allowedClockSkewSeconds;
        }

        public void setAllowedClockSkewSeconds(long allowedClockSkewSeconds) {
            this.allowedClockSkewSeconds = allowedClockSkewSeconds;
        }

        public Consumer getConsumer() {
            return consumer;
        }

        public void setConsumer(Consumer consumer) {
            this.consumer = consumer;
        }

        public Provider getProvider() {
            return provider;
        }

        public void setProvider(Provider provider) {
            this.provider = provider;
        }

        public List<String> getExcludedServicePrefixes() {
            return excludedServicePrefixes;
        }

        public void setExcludedServicePrefixes(List<String> excludedServicePrefixes) {
            this.excludedServicePrefixes = excludedServicePrefixes != null
                    ? new ArrayList<>(excludedServicePrefixes)
                    : new ArrayList<>(Collections.singletonList("org.apache.dubbo."));
        }
    }

    /**
     * RPC 消费方凭证配置。
     */
    public static class Consumer {

        private String appId;

        private String appSecret;

        public String getAppId() {
            return appId;
        }

        public void setAppId(String appId) {
            this.appId = appId;
        }

        public String getAppSecret() {
            return appSecret;
        }

        public void setAppSecret(String appSecret) {
            this.appSecret = appSecret;
        }

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
     */
    public static class Provider {

        private boolean required = false;

        private Map<String, String> credentials = new LinkedHashMap<>();

        public boolean isRequired() {
            return required;
        }

        public void setRequired(boolean required) {
            this.required = required;
        }

        public Map<String, String> getCredentials() {
            return credentials;
        }

        public void setCredentials(Map<String, String> credentials) {
            this.credentials = credentials != null ? new LinkedHashMap<>(credentials) : new LinkedHashMap<>();
        }
    }

    /**
     * Dubbo 序列化安全配置。
     */
    public static class Serialization {

        private boolean enabled = true;

        private String checkStatus = "STRICT";

        private boolean checkSerializable = true;

        private List<String> allowedPrefixes = new ArrayList<>(List.of("com.getboot"));

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getCheckStatus() {
            return checkStatus;
        }

        public void setCheckStatus(String checkStatus) {
            this.checkStatus = checkStatus;
        }

        public boolean isCheckSerializable() {
            return checkSerializable;
        }

        public void setCheckSerializable(boolean checkSerializable) {
            this.checkSerializable = checkSerializable;
        }

        public List<String> getAllowedPrefixes() {
            return allowedPrefixes;
        }

        public void setAllowedPrefixes(List<String> allowedPrefixes) {
            this.allowedPrefixes = allowedPrefixes != null
                    ? new ArrayList<>(allowedPrefixes)
                    : new ArrayList<>(Collections.singletonList("com.getboot"));
        }
    }
}
