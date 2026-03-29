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
package com.getboot.database.infrastructure.environment;

import com.getboot.support.infrastructure.environment.PropertyAliasEnvironmentPostProcessorSupport;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.Map;

/**
 * 数据库配置别名处理器。
 *
 * <p>用于将 GetBoot 数据源与 MyBatis-Plus 配置前缀映射为底层组件原生前缀。</p>
 *
 * @author qiheng
 */
public class DatabasePropertyAliasEnvironmentPostProcessor extends PropertyAliasEnvironmentPostProcessorSupport {

    @Override
    protected String aliasedPropertySourceName() {
        return "getbootDatabaseAliasedProperties";
    }

    @Override
    protected void contributeAliases(ConfigurableEnvironment environment, Map<String, Object> aliasedProperties) {
        aliasPrefix(
                environment,
                aliasedProperties,
                "getboot.database.datasource.",
                "spring.datasource.",
                suffix -> !"enabled".equals(suffix) && !suffix.startsWith("init.")
        );
        aliasPrefix(environment, aliasedProperties, "getboot.database.mybatis-plus.", "mybatis-plus.");
        aliasPrefix(environment, aliasedProperties, "getboot.database.sharding.props.", "spring.shardingsphere.props.");
        aliasPrefix(environment, aliasedProperties, "getboot.database.sharding.mode.", "spring.shardingsphere.mode.");
        aliasPrefix(environment, aliasedProperties, "getboot.database.sharding.rules.", "spring.shardingsphere.rules.");
        aliasPrefix(environment, aliasedProperties, "getboot.database.sharding.datasource.", "spring.shardingsphere.datasource.");
    }
}
