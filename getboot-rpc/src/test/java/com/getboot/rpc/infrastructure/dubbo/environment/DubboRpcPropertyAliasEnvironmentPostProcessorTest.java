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
package com.getboot.rpc.infrastructure.dubbo.environment;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link DubboRpcPropertyAliasEnvironmentPostProcessor} 测试。
 *
 * @author qiheng
 */
class DubboRpcPropertyAliasEnvironmentPostProcessorTest {

    /**
     * 验证 GetBoot Dubbo 前缀会桥接到 Dubbo 原生前缀。
     */
    @Test
    void shouldAliasGetbootRpcDubboPropertiesToNativePrefix() {
        ConfigurableEnvironment environment = new StandardEnvironment();
        environment.getPropertySources().addFirst(new MapPropertySource("source", Map.of(
                "getboot.rpc.dubbo.application.name", "demo-service",
                "getboot.rpc.dubbo.protocol.name", "dubbo",
                "getboot.rpc.dubbo.registry.address", "nacos://127.0.0.1:8848"
        )));

        DubboRpcPropertyAliasEnvironmentPostProcessor processor = new DubboRpcPropertyAliasEnvironmentPostProcessor();
        processor.postProcessEnvironment(environment, new SpringApplication(Object.class));

        assertEquals("demo-service", environment.getProperty("dubbo.application.name"));
        assertEquals("dubbo", environment.getProperty("dubbo.protocol.name"));
        assertEquals("nacos://127.0.0.1:8848", environment.getProperty("dubbo.registry.address"));
    }

    /**
     * 验证显式声明的 Dubbo 原生配置不会被桥接覆盖。
     */
    @Test
    void shouldNotOverrideExistingNativeDubboProperties() {
        ConfigurableEnvironment environment = new StandardEnvironment();
        environment.getPropertySources().addFirst(new MapPropertySource("source", Map.of(
                "getboot.rpc.dubbo.application.name", "demo-service",
                "dubbo.application.name", "keep-service"
        )));

        DubboRpcPropertyAliasEnvironmentPostProcessor processor = new DubboRpcPropertyAliasEnvironmentPostProcessor();
        processor.postProcessEnvironment(environment, new SpringApplication(Object.class));

        assertEquals("keep-service", environment.getProperty("dubbo.application.name"));
    }
}
