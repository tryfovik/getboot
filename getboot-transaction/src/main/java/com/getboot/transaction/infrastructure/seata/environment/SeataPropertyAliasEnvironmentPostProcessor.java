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

import com.getboot.support.infrastructure.environment.PropertyAliasEnvironmentPostProcessorSupport;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.Map;

/**
 * Seata 配置别名处理器。
 *
 * <p>用于将 GetBoot 风格的事务配置前缀映射为 Seata 原生前缀。</p>
 *
 * @author qiheng
 */
public class SeataPropertyAliasEnvironmentPostProcessor extends PropertyAliasEnvironmentPostProcessorSupport {

    /**
     * GetBoot 事务总开关配置项。
     */
    private static final String TRANSACTION_ENABLED_PROPERTY = "getboot.transaction.enabled";

    /**
     * Seata 原生启用开关配置项。
     */
    private static final String SEATA_ENABLED_PROPERTY = "seata.enabled";

    /**
     * 返回桥接属性源名称。
     *
     * @return 桥接属性源名称
     */
    @Override
    protected String aliasedPropertySourceName() {
        return "getbootTransactionSeataAliasedProperties";
    }

    /**
     * 将 GetBoot 事务配置桥接为 Seata 原生配置。
     *
     * @param environment 当前环境
     * @param aliasedProperties 待写入的别名属性集合
     */
    @Override
    protected void contributeAliases(ConfigurableEnvironment environment, Map<String, Object> aliasedProperties) {
        if (!environment.getProperty(TRANSACTION_ENABLED_PROPERTY, Boolean.class, true)
                && !environment.containsProperty(SEATA_ENABLED_PROPERTY)) {
            aliasedProperties.put(SEATA_ENABLED_PROPERTY, false);
        }
        aliasPrefix(
                environment,
                aliasedProperties,
                "getboot.transaction.seata.",
                "seata.",
                suffix -> !"mode".equals(suffix)
        );
    }
}
