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
package com.getboot.transaction.infrastructure.seata.environment;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class SeataPropertyAliasEnvironmentPostProcessorTest {

    @Test
    void shouldAliasSeataPropertiesAndSkipMode() {
        ConfigurableEnvironment environment = new StandardEnvironment();
        environment.getPropertySources().addFirst(new MapPropertySource("source", Map.of(
                "getboot.transaction.seata.application-id", "demo-order-service",
                "getboot.transaction.seata.tx-service-group", "demo_tx_group",
                "getboot.transaction.seata.enable-auto-data-source-proxy", "false",
                "getboot.transaction.seata.mode", "XA"
        )));

        SeataPropertyAliasEnvironmentPostProcessor processor = new SeataPropertyAliasEnvironmentPostProcessor();
        processor.postProcessEnvironment(environment, new SpringApplication(Object.class));

        assertEquals("demo-order-service", environment.getProperty("seata.application-id"));
        assertEquals("demo_tx_group", environment.getProperty("seata.tx-service-group"));
        assertEquals("false", environment.getProperty("seata.enable-auto-data-source-proxy"));
        assertFalse(environment.containsProperty("seata.mode"));
    }

    @Test
    void shouldDisableNativeSeataWhenGetbootTransactionIsDisabled() {
        ConfigurableEnvironment environment = new StandardEnvironment();
        environment.getPropertySources().addFirst(new MapPropertySource("source", Map.of(
                "getboot.transaction.enabled", "false",
                "getboot.transaction.seata.enabled", "true"
        )));

        SeataPropertyAliasEnvironmentPostProcessor processor = new SeataPropertyAliasEnvironmentPostProcessor();
        processor.postProcessEnvironment(environment, new SpringApplication(Object.class));

        assertEquals("false", environment.getProperty("seata.enabled"));
    }

    @Test
    void shouldNotOverrideExistingNativeSeataEnabledFlag() {
        ConfigurableEnvironment environment = new StandardEnvironment();
        environment.getPropertySources().addFirst(new MapPropertySource("source", Map.of(
                "getboot.transaction.enabled", "false",
                "seata.enabled", "true"
        )));

        SeataPropertyAliasEnvironmentPostProcessor processor = new SeataPropertyAliasEnvironmentPostProcessor();
        processor.postProcessEnvironment(environment, new SpringApplication(Object.class));

        assertEquals("true", environment.getProperty("seata.enabled"));
    }
}
