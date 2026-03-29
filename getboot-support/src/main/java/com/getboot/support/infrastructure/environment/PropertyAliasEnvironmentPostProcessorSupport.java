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

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Predicate;

/**
 * 属性别名环境处理器抽象基类。
 *
 * <p>用于在 Spring Boot 启动早期统一收集并注册配置前缀别名映射。</p>
 *
 * @author qiheng
 */
public abstract class PropertyAliasEnvironmentPostProcessorSupport implements EnvironmentPostProcessor, Ordered {

    @Override
    public final void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        Map<String, Object> aliasedProperties = new LinkedHashMap<>();
        contributeAliases(environment, aliasedProperties);
        if (!aliasedProperties.isEmpty()) {
            environment.getPropertySources().addFirst(
                    new MapPropertySource(aliasedPropertySourceName(), aliasedProperties)
            );
        }
    }

    protected abstract String aliasedPropertySourceName();

    protected abstract void contributeAliases(ConfigurableEnvironment environment,
                                              Map<String, Object> aliasedProperties);

    protected void aliasPrefix(ConfigurableEnvironment environment,
                               Map<String, Object> aliasedProperties,
                               String sourcePrefix,
                               String targetPrefix) {
        aliasPrefix(environment, aliasedProperties, sourcePrefix, targetPrefix, suffix -> true);
    }

    protected void aliasPrefix(ConfigurableEnvironment environment,
                               Map<String, Object> aliasedProperties,
                               String sourcePrefix,
                               String targetPrefix,
                               Predicate<String> suffixFilter) {
        for (PropertySource<?> propertySource : environment.getPropertySources()) {
            if (!(propertySource instanceof EnumerablePropertySource<?> enumerablePropertySource)) {
                continue;
            }
            for (String propertyName : enumerablePropertySource.getPropertyNames()) {
                if (!propertyName.startsWith(sourcePrefix)) {
                    continue;
                }
                String suffix = propertyName.substring(sourcePrefix.length());
                if (!suffixFilter.test(suffix)) {
                    continue;
                }
                String targetPropertyName = targetPrefix + suffix;
                if (environment.containsProperty(targetPropertyName)) {
                    continue;
                }
                aliasedProperties.putIfAbsent(targetPropertyName, enumerablePropertySource.getProperty(propertyName));
            }
        }
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
