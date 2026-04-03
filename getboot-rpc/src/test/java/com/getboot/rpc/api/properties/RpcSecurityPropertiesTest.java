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

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link RpcSecurityProperties} 测试。
 *
 * @author qiheng
 */
class RpcSecurityPropertiesTest {

    /**
     * 验证集合类型配置项会复制外部传入的数据，避免共享可变引用。
     */
    @Test
    void shouldCopyCollectionValuesWhenSettingSecurityProperties() {
        RpcSecurityProperties.Authentication authentication = new RpcSecurityProperties.Authentication();
        List<String> excludedServicePrefixes = new ArrayList<>(List.of("com.demo"));
        authentication.setExcludedServicePrefixes(excludedServicePrefixes);

        RpcSecurityProperties.Provider provider = new RpcSecurityProperties.Provider();
        Map<String, String> credentials = new LinkedHashMap<>(Map.of("consumer-app", "secret"));
        provider.setCredentials(credentials);

        RpcSecurityProperties.Serialization serialization = new RpcSecurityProperties.Serialization();
        List<String> allowedPrefixes = new ArrayList<>(List.of("com.demo"));
        serialization.setAllowedPrefixes(allowedPrefixes);

        excludedServicePrefixes.add("com.other");
        credentials.put("another-app", "another-secret");
        allowedPrefixes.add("org.demo");

        assertEquals(List.of("com.demo"), authentication.getExcludedServicePrefixes());
        assertEquals(Map.of("consumer-app", "secret"), provider.getCredentials());
        assertEquals(List.of("com.demo"), serialization.getAllowedPrefixes());
        assertNotSame(excludedServicePrefixes, authentication.getExcludedServicePrefixes());
        assertNotSame(credentials, provider.getCredentials());
        assertNotSame(allowedPrefixes, serialization.getAllowedPrefixes());
    }

    /**
     * 验证消费方凭证辅助判断方法符合预期。
     */
    @Test
    void shouldRecognizeConsumerCredentialConfigurationState() {
        RpcSecurityProperties.Consumer consumer = new RpcSecurityProperties.Consumer();

        assertFalse(consumer.hasAnyConfiguredValue());
        assertFalse(consumer.isConfigured());

        consumer.setAppId("consumer-app");
        assertTrue(consumer.hasAnyConfiguredValue());
        assertFalse(consumer.isConfigured());

        consumer.setAppSecret("consumer-secret");
        assertTrue(consumer.isConfigured());
    }
}
