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
package com.getboot.governance.infrastructure.sentinel.environment;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class SentinelPropertyAliasEnvironmentPostProcessorTest {

    @Test
    void shouldAliasSentinelPropertiesToExpectedNativePrefixes() {
        ConfigurableEnvironment environment = new StandardEnvironment();
        environment.getPropertySources().addFirst(new MapPropertySource("source", Map.of(
                "getboot.governance.sentinel.transport.dashboard", "127.0.0.1:8858",
                "getboot.governance.sentinel.filter.order", "-100",
                "getboot.governance.sentinel.openfeign.enabled", "true",
                "getboot.governance.sentinel.rest-template.enabled", "false",
                "getboot.governance.sentinel.management.endpoint.enabled", "true",
                "getboot.governance.sentinel.management.health.enabled", "false"
        )));

        SentinelPropertyAliasEnvironmentPostProcessor processor = new SentinelPropertyAliasEnvironmentPostProcessor();
        processor.postProcessEnvironment(environment, new SpringApplication(Object.class));

        assertEquals("127.0.0.1:8858", environment.getProperty("spring.cloud.sentinel.transport.dashboard"));
        assertEquals("-100", environment.getProperty("spring.cloud.sentinel.filter.order"));
        assertEquals("true", environment.getProperty("feign.sentinel.enabled"));
        assertEquals("false", environment.getProperty("resttemplate.sentinel.enabled"));
        assertEquals("true", environment.getProperty("management.endpoint.sentinel.enabled"));
        assertEquals("false", environment.getProperty("management.health.sentinel.enabled"));
        assertFalse(environment.containsProperty("spring.cloud.sentinel.openfeign.enabled"));
        assertFalse(environment.containsProperty("spring.cloud.sentinel.rest-template.enabled"));
        assertFalse(environment.containsProperty("spring.cloud.sentinel.management.endpoint.enabled"));
        assertFalse(environment.containsProperty("spring.cloud.sentinel.management.health.enabled"));
    }

    @Test
    void shouldNotOverrideExistingNativeProperties() {
        ConfigurableEnvironment environment = new StandardEnvironment();
        environment.getPropertySources().addFirst(new MapPropertySource("source", Map.of(
                "getboot.governance.sentinel.openfeign.enabled", "true",
                "feign.sentinel.enabled", "keep-existing"
        )));

        SentinelPropertyAliasEnvironmentPostProcessor processor = new SentinelPropertyAliasEnvironmentPostProcessor();
        processor.postProcessEnvironment(environment, new SpringApplication(Object.class));

        assertEquals("keep-existing", environment.getProperty("feign.sentinel.enabled"));
    }
}
