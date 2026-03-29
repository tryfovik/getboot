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
package com.getboot.limiter.infrastructure.slidingwindow.environment;

import com.getboot.support.infrastructure.environment.PropertyAliasEnvironmentPostProcessorSupport;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.Map;

/**
 * limiter 配置别名处理器。
 *
 * <p>将旧版 `getboot.limiter.*` 配置映射到新的 `getboot.limiter.sliding-window.*` 子树，避免存量项目立即改配置。</p>
 *
 * @author qiheng
 */
public class SlidingWindowRateLimiterPropertyAliasEnvironmentPostProcessor
        extends PropertyAliasEnvironmentPostProcessorSupport {

    @Override
    protected String aliasedPropertySourceName() {
        return "getbootLimiterSlidingWindowAliasedProperties";
    }

    @Override
    protected void contributeAliases(ConfigurableEnvironment environment, Map<String, Object> aliasedProperties) {
        aliasPrefix(
                environment,
                aliasedProperties,
                "getboot.limiter.limiters.",
                "getboot.limiter.sliding-window.limiters."
        );
        aliasProperty(environment, aliasedProperties,
                "getboot.limiter.default-timeout",
                "getboot.limiter.sliding-window.default-timeout");
        aliasProperty(environment, aliasedProperties,
                "getboot.limiter.key-prefix",
                "getboot.limiter.sliding-window.key-prefix");
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
