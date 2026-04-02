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
package com.getboot.sms.api.request;

import lombok.Data;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 验证码短信发送请求。
 *
 * @author qiheng
 */
@Data
public class SmsVerificationCodeRequest {

    /**
     * 验证码业务场景。
     */
    private String scene;

    /**
     * 接收手机号。
     */
    private String phoneNumber;

    /**
     * 短信签名。
     */
    private String signName;

    /**
     * 短信模板编码。
     */
    private String templateCode;

    /**
     * 验证码内容。
     */
    private String code;

    /**
     * 过期分钟数。
     */
    private Integer expireMinutes;

    /**
     * 外部业务流水号。
     */
    private String outId;

    /**
     * 附加模板变量。
     */
    private Map<String, Object> templateParams = new LinkedHashMap<>();

    /**
     * 设置附加模板变量。
     *
     * @param templateParams 附加模板变量
     */
    public void setTemplateParams(Map<String, Object> templateParams) {
        this.templateParams = templateParams == null ? new LinkedHashMap<>() : new LinkedHashMap<>(templateParams);
    }
}
