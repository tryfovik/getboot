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
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link DefaultSmsSignResolver} 测试。
 *
 * @author qiheng
 */
class DefaultSmsSignResolverTest {

    /**
     * 验证显式签名、场景签名与默认签名的优先级。
     */
    @Test
    void shouldResolveSignNameByRequestedSceneAndDefaultOrder() {
        SmsProperties properties = new SmsProperties();
        properties.setDefaultSignName(" DefaultSign ");
        properties.setSceneSignNames(Map.of("notice", " NoticeSign "));
        DefaultSmsSignResolver signResolver = new DefaultSmsSignResolver(properties);

        assertEquals("CustomSign", signResolver.resolveSignName("notice", " CustomSign "));
        assertEquals("NoticeSign", signResolver.resolveSignName(" notice ", null));
        assertEquals("DefaultSign", signResolver.resolveSignName("unknown", null));
    }

    /**
     * 验证未配置任何签名时返回空字符串。
     */
    @Test
    void shouldReturnEmptyStringWhenNoSignConfigured() {
        DefaultSmsSignResolver signResolver = new DefaultSmsSignResolver(new SmsProperties());

        assertEquals("", signResolver.resolveSignName("unknown", null));
    }
}
