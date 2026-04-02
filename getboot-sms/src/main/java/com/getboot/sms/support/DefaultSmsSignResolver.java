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

import com.getboot.sms.api.properties.SmsProperties;
import com.getboot.sms.spi.SmsSignResolver;
import org.springframework.util.StringUtils;

/**
 * 默认短信签名解析器。
 *
 * @author qiheng
 */
public class DefaultSmsSignResolver implements SmsSignResolver {

    /**
     * 短信模块配置。
     */
    private final SmsProperties properties;

    /**
     * 构造默认短信签名解析器。
     *
     * @param properties 短信模块配置
     */
    public DefaultSmsSignResolver(SmsProperties properties) {
        this.properties = properties;
    }

    /**
     * 解析最终短信签名。
     *
     * @param scene 业务场景
     * @param requestedSignName 显式指定的签名
     * @return 最终签名
     */
    @Override
    public String resolveSignName(String scene, String requestedSignName) {
        if (StringUtils.hasText(requestedSignName)) {
            return requestedSignName.trim();
        }
        if (StringUtils.hasText(scene)) {
            String routedSignName = properties.getSceneSignNames().get(scene.trim());
            if (StringUtils.hasText(routedSignName)) {
                return routedSignName.trim();
            }
        }
        return StringUtils.hasText(properties.getDefaultSignName()) ? properties.getDefaultSignName().trim() : "";
    }
}
