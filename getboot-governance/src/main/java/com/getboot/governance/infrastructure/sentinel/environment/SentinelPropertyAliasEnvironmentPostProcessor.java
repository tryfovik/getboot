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

import com.getboot.support.infrastructure.environment.PropertyAliasEnvironmentPostProcessorSupport;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.Map;

/**
 * Sentinel 配置别名处理器。
 *
 * <p>用于将 GetBoot 风格的治理配置前缀映射为 Sentinel 与其桥接组件的原生前缀。</p>
 *
 * @author qiheng
 */
public class SentinelPropertyAliasEnvironmentPostProcessor extends PropertyAliasEnvironmentPostProcessorSupport {

    @Override
    protected String aliasedPropertySourceName() {
        return "getbootGovernanceSentinelAliasedProperties";
    }

    @Override
    protected void contributeAliases(ConfigurableEnvironment environment, Map<String, Object> aliasedProperties) {
        aliasPrefix(environment, aliasedProperties, "getboot.governance.sentinel.", "spring.cloud.sentinel.");
        aliasProperty(environment, aliasedProperties,
                "getboot.governance.sentinel.openfeign.enabled",
                "feign.sentinel.enabled");
        aliasProperty(environment, aliasedProperties,
                "getboot.governance.sentinel.rest-template.enabled",
                "resttemplate.sentinel.enabled");
        aliasProperty(environment, aliasedProperties,
                "getboot.governance.sentinel.management.endpoint.enabled",
                "management.endpoint.sentinel.enabled");
        aliasProperty(environment, aliasedProperties,
                "getboot.governance.sentinel.management.health.enabled",
                "management.health.sentinel.enabled");
    }

    private void aliasProperty(ConfigurableEnvironment environment,
                               Map<String, Object> aliasedProperties,
                               String sourcePropertyName,
                               String targetPropertyName) {
        if (!environment.containsProperty(sourcePropertyName) || environment.containsProperty(targetPropertyName)) {
            return;
        }
        aliasedProperties.putIfAbsent(targetPropertyName, environment.getProperty(sourcePropertyName));
    }
}
