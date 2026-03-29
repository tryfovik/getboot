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
package com.getboot.database.infrastructure.datasource.autoconfigure;

import com.getboot.database.api.properties.DatabaseProperties;
import com.getboot.database.support.datasource.DataSourceInitializer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;

/**
 * 数据源增强自动配置。
 *
 * <p>当前主要负责在应用启动阶段执行数据源预热与连通性校验。</p>
 *
 * @author qiheng
 */
@AutoConfiguration
@ConditionalOnClass(DataSource.class)
@EnableConfigurationProperties(DatabaseProperties.class)
@ConditionalOnProperty(prefix = "getboot.database", name = "enabled", havingValue = "true")
public class DataSourceAutoConfiguration {

    /**
     * 注册标准数据源初始化器。
     *
     * @param dataSource 数据源实例
     * @param properties 初始化配置
     * @return 数据源初始化器
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(
            value = "getboot.database.datasource.init.enabled",
            havingValue = "true",
            matchIfMissing = true
    )
    public DataSourceInitializer dataSourceInitializer(DataSource dataSource,
                                                       DatabaseProperties databaseProperties) {
        return new DataSourceInitializer(dataSource, databaseProperties.getDatasource().getInit());
    }
}
