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
package com.getboot.sms.support;

import com.getboot.sms.api.exception.SmsException;
import com.getboot.sms.api.properties.SmsProperties;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 短信模块通用辅助方法。
 *
 * @author qiheng
 */
public final class SmsSupport {

    /**
     * 工具类私有构造器。
     */
    private SmsSupport() {
    }

    /**
     * 校验手机号并返回规整值。
     *
     * @param phoneNumber 手机号
     * @return 规整后的手机号
     */
    public static String requirePhoneNumber(String phoneNumber) {
        if (!StringUtils.hasText(phoneNumber)) {
            throw new SmsException("SMS phoneNumber must not be empty.");
        }
        return phoneNumber.trim();
    }

    /**
     * 校验短信签名并返回规整值。
     *
     * @param signName 短信签名
     * @return 规整后的短信签名
     */
    public static String requireSignName(String signName) {
        if (!StringUtils.hasText(signName)) {
            throw new SmsException("SMS signName must not be empty.");
        }
        return signName.trim();
    }

    /**
     * 校验模板编码并返回规整值。
     *
     * @param templateCode 模板编码
     * @return 规整后的模板编码
     */
    public static String requireTemplateCode(String templateCode) {
        if (!StringUtils.hasText(templateCode)) {
            throw new SmsException("SMS templateCode must not be empty.");
        }
        return templateCode.trim();
    }

    /**
     * 规整可选文本字段。
     *
     * @param value 原始文本
     * @return 规整后的文本，空白时返回 {@code null}
     */
    public static String normalizeOptionalText(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    /**
     * 拷贝模板变量，避免外部引用被内部修改。
     *
     * @param templateParams 模板变量
     * @return 拷贝后的模板变量
     */
    public static Map<String, Object> copyTemplateParams(Map<String, Object> templateParams) {
        return templateParams == null ? new LinkedHashMap<>() : new LinkedHashMap<>(templateParams);
    }

    /**
     * 获取验证码场景配置。
     *
     * @param scene 验证码场景
     * @param properties 短信模块配置
     * @return 场景配置
     */
    public static SmsProperties.VerificationScene requireVerificationScene(String scene, SmsProperties properties) {
        if (!StringUtils.hasText(scene)) {
            throw new SmsException("SMS verification scene must not be empty.");
        }
        SmsProperties.VerificationScene verificationScene = properties.getVerificationScenes().get(scene.trim());
        if (verificationScene == null) {
            throw new SmsException("SMS verification scene is not configured. scene=" + scene.trim());
        }
        return verificationScene;
    }
}
