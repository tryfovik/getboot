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
package com.getboot.support.infrastructure.environment;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link PropertyAliasEnvironmentPostProcessorSupport} 测试。
 *
 * @author qiheng
 */
class PropertyAliasEnvironmentPostProcessorSupportTest {

    /**
     * 验证注册别名时不会覆盖已经存在的目标配置。
     */
    @Test
    void shouldRegisterAliasedPropertiesWithoutOverridingExistingTarget() {
        ConfigurableEnvironment environment = new StandardEnvironment();
        environment.getPropertySources().addFirst(new MapPropertySource("source", Map.of(
                "legacy.demo.alpha", "value-a",
                "legacy.demo.beta", "value-b",
                "modern.demo.beta", "keep-b"
        )));

        DemoPropertyAliasEnvironmentPostProcessor processor = new DemoPropertyAliasEnvironmentPostProcessor();
        processor.postProcessEnvironment(environment, new SpringApplication(Object.class));

        assertEquals("value-a", environment.getProperty("modern.demo.alpha"));
        assertEquals("keep-b", environment.getProperty("modern.demo.beta"));
    }

    /**
     * 测试用属性别名环境后处理器。
     */
    private static final class DemoPropertyAliasEnvironmentPostProcessor
            extends PropertyAliasEnvironmentPostProcessorSupport {

        /**
         * 返回测试用属性源名称。
         *
         * @return 属性源名称
         */
        @Override
        protected String aliasedPropertySourceName() {
            return "demoAliases";
        }

        /**
         * 注册测试用属性别名映射。
         *
         * @param environment 当前环境
         * @param aliasedProperties 别名属性容器
         */
        @Override
        protected void contributeAliases(ConfigurableEnvironment environment, Map<String, Object> aliasedProperties) {
            aliasPrefix(environment, aliasedProperties, "legacy.demo.", "modern.demo.");
        }
    }
}
