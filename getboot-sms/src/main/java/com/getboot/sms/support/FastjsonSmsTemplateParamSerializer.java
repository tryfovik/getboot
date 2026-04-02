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

import com.alibaba.fastjson2.JSON;
import com.getboot.sms.spi.SmsTemplateParamSerializer;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 基于 Fastjson 的模板变量序列化器。
 *
 * @author qiheng
 */
public class FastjsonSmsTemplateParamSerializer implements SmsTemplateParamSerializer {

    /**
     * 使用 Fastjson 序列化模板变量。
     *
     * @param templateParams 模板变量
     * @return JSON 字符串
     */
    @Override
    public String serialize(Map<String, Object> templateParams) {
        Map<String, Object> safeTemplateParams = templateParams == null
                ? new LinkedHashMap<>()
                : new LinkedHashMap<>(templateParams);
        return JSON.toJSONString(safeTemplateParams);
    }
}
