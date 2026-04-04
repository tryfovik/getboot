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
package com.getboot.auth.infrastructure.satoken.environment;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link SaTokenPropertyAliasEnvironmentPostProcessor} 测试。
 *
 * @author qiheng
 */
class SaTokenPropertyAliasEnvironmentPostProcessorTest {

    /**
     * 验证 GetBoot Sa-Token 配置前缀会桥接到 Sa-Token 原生前缀。
     */
    @Test
    void shouldAliasGetbootSaTokenPropertiesToNativePrefix() {
        ConfigurableEnvironment environment = new StandardEnvironment();
        environment.getPropertySources().addFirst(new MapPropertySource("source", Map.of(
                "getboot.auth.satoken.token-name", "demo-token",
                "getboot.auth.satoken.timeout", "3600",
                "getboot.auth.satoken.is-share", "false"
        )));

        SaTokenPropertyAliasEnvironmentPostProcessor processor = new SaTokenPropertyAliasEnvironmentPostProcessor();
        processor.postProcessEnvironment(environment, new SpringApplication(Object.class));

        assertEquals("demo-token", environment.getProperty("sa-token.token-name"));
        assertEquals("3600", environment.getProperty("sa-token.timeout"));
        assertEquals("false", environment.getProperty("sa-token.is-share"));
    }

    /**
     * 验证已显式声明的 Sa-Token 原生配置不会被别名覆盖。
     */
    @Test
    void shouldNotOverrideExistingNativeSaTokenProperties() {
        ConfigurableEnvironment environment = new StandardEnvironment();
        environment.getPropertySources().addFirst(new MapPropertySource("source", Map.of(
                "getboot.auth.satoken.timeout", "3600",
                "sa-token.timeout", "7200"
        )));

        SaTokenPropertyAliasEnvironmentPostProcessor processor = new SaTokenPropertyAliasEnvironmentPostProcessor();
        processor.postProcessEnvironment(environment, new SpringApplication(Object.class));

        assertEquals("7200", environment.getProperty("sa-token.timeout"));
    }
}
