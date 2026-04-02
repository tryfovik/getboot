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
package com.getboot.sms.api.properties;

import com.getboot.sms.api.constant.SmsConstants;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 短信模块配置。
 *
 * @author qiheng
 */
@ConfigurationProperties(prefix = "getboot.sms")
@Data
public class SmsProperties {

    /**
     * 是否启用短信能力。
     */
    private boolean enabled = true;

    /**
     * 当前供应商类型。
     */
    private String type = SmsConstants.PROVIDER_ALIYUN;

    /**
     * 默认短信签名。
     */
    private String defaultSignName;

    /**
     * 场景签名路由。
     */
    private Map<String, String> sceneSignNames = new LinkedHashMap<>();

    /**
     * 验证码场景配置。
     */
    private Map<String, VerificationScene> verificationScenes = new LinkedHashMap<>();

    /**
     * 阿里云短信配置。
     */
    private Aliyun aliyun = new Aliyun();

    /**
     * 设置场景签名路由。
     *
     * @param sceneSignNames 场景签名路由
     */
    public void setSceneSignNames(Map<String, String> sceneSignNames) {
        this.sceneSignNames = sceneSignNames == null ? new LinkedHashMap<>() : new LinkedHashMap<>(sceneSignNames);
    }

    /**
     * 设置验证码场景配置。
     *
     * @param verificationScenes 验证码场景配置
     */
    public void setVerificationScenes(Map<String, VerificationScene> verificationScenes) {
        this.verificationScenes = verificationScenes == null
                ? new LinkedHashMap<>()
                : new LinkedHashMap<>(verificationScenes);
    }

    /**
     * 验证码场景配置。
     *
     * @author qiheng
     */
    @Data
    public static class VerificationScene {

        /**
         * 当前场景的短信签名。
         */
        private String signName;

        /**
         * 当前场景的短信模板编码。
         */
        private String templateCode;

        /**
         * 验证码模板变量名。
         */
        private String codeParamName = SmsConstants.DEFAULT_CODE_PARAM_NAME;

        /**
         * 过期时间模板变量名。
         */
        private String expireMinutesParamName = SmsConstants.DEFAULT_EXPIRE_MINUTES_PARAM_NAME;

        /**
         * 固定模板变量。
         */
        private Map<String, Object> extraParams = new LinkedHashMap<>();

        /**
         * 设置固定模板变量。
         *
         * @param extraParams 固定模板变量
         */
        public void setExtraParams(Map<String, Object> extraParams) {
            this.extraParams = extraParams == null ? new LinkedHashMap<>() : new LinkedHashMap<>(extraParams);
        }
    }

    /**
     * 阿里云短信配置。
     *
     * @author qiheng
     */
    @Data
    public static class Aliyun {

        /**
         * 是否启用阿里云短信实现。
         */
        private boolean enabled = true;

        /**
         * 阿里云短信服务地址。
         */
        private String endpoint = SmsConstants.ALIYUN_DEFAULT_ENDPOINT;

        /**
         * 阿里云地域编码。
         */
        private String regionId = SmsConstants.ALIYUN_DEFAULT_REGION_ID;

        /**
         * 阿里云 AccessKey ID。
         */
        private String accessKeyId;

        /**
         * 阿里云 AccessKey Secret。
         */
        private String accessKeySecret;

        /**
         * 连接超时时间，单位毫秒。
         */
        private Integer connectTimeout = 5000;

        /**
         * 读取超时时间，单位毫秒。
         */
        private Integer readTimeout = 5000;

    }
}
