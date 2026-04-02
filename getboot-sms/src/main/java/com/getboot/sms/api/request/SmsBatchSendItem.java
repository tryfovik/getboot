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
 * 批量短信中的单个接收项。
 *
 * @author qiheng
 */
@Data
public class SmsBatchSendItem {

    /**
     * 接收手机号。
     */
    private String phoneNumber;

    /**
     * 当前接收项的短信签名。
     */
    private String signName;

    /**
     * 当前接收项的模板变量。
     */
    private Map<String, Object> templateParams = new LinkedHashMap<>();

    /**
     * 设置当前接收项的模板变量。
     *
     * @param templateParams 模板变量
     */
    public void setTemplateParams(Map<String, Object> templateParams) {
        this.templateParams = templateParams == null ? new LinkedHashMap<>() : new LinkedHashMap<>(templateParams);
    }
}
