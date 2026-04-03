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
package com.getboot.lock.infrastructure.database.jdbc.autoconfigure;

import com.getboot.lock.api.properties.LockProperties;
import com.getboot.lock.infrastructure.database.jdbc.aspect.JdbcDistributedLockAspect;
import com.getboot.lock.infrastructure.database.jdbc.support.JdbcDistributedLockRepository;
import com.getboot.lock.infrastructure.database.jdbc.support.JdbcDistributedLockSchemaInitializer;
import com.getboot.lock.spi.DistributedLockAcquireFailureHandler;
import com.getboot.lock.spi.DistributedLockKeyResolver;
import com.getboot.lock.support.DefaultDistributedLockAcquireFailureHandler;
import com.getboot.lock.support.SpelDistributedLockKeyResolver;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

/**
 * JDBC 分布式锁自动配置。
 *
 * @author qiheng
 */
@AutoConfiguration
@ConditionalOnClass(DataSource.class)
@ConditionalOnSingleCandidate(DataSource.class)
@ConditionalOnProperty(prefix = "getboot.lock", name = "enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnExpression("'${getboot.lock.type:}' == 'database' and '${getboot.lock.database.enabled:false}' == 'true'")
@EnableConfigurationProperties(LockProperties.class)
public class JdbcDistributedLockAutoConfiguration {

    /**
     * 注册默认锁 key 解析器。
     *
     * @return 锁 key 解析器
     */
    @Bean
    @ConditionalOnMissingBean
    public DistributedLockKeyResolver distributedLockKeyResolver() {
        return new SpelDistributedLockKeyResolver();
    }

    /**
     * 注册默认锁获取失败处理器。
     *
     * @return 锁获取失败处理器
     */
    @Bean
    @ConditionalOnMissingBean
    public DistributedLockAcquireFailureHandler distributedLockAcquireFailureHandler() {
        return new DefaultDistributedLockAcquireFailureHandler();
    }

    /**
     * 注册 JDBC 锁仓储。
     *
     * @param dataSource 数据源
     * @param properties 锁配置属性
     * @return JDBC 锁仓储
     */
    @Bean
    @ConditionalOnMissingBean
    public JdbcDistributedLockRepository jdbcDistributedLockRepository(
            DataSource dataSource,
            LockProperties properties) {
        return new JdbcDistributedLockRepository(
                new JdbcTemplate(dataSource),
                properties.getDatabase().getTableName()
        );
    }

    /**
     * 注册 JDBC 分布式锁切面。
     *
     * @param jdbcDistributedLockRepository JDBC 锁仓储
     * @param distributedLockKeyResolver 锁 key 解析器
     * @param distributedLockAcquireFailureHandler 锁获取失败处理器
     * @param properties 锁配置属性
     * @return JDBC 分布式锁切面
     */
    @Bean
    @ConditionalOnMissingBean
    public JdbcDistributedLockAspect distributedLockAspect(
            JdbcDistributedLockRepository jdbcDistributedLockRepository,
            DistributedLockKeyResolver distributedLockKeyResolver,
            DistributedLockAcquireFailureHandler distributedLockAcquireFailureHandler,
            LockProperties properties) {
        return new JdbcDistributedLockAspect(
                jdbcDistributedLockRepository,
                distributedLockKeyResolver,
                distributedLockAcquireFailureHandler,
                properties
        );
    }

    /**
     * 注册 JDBC 锁表初始化器。
     *
     * @param dataSource 数据源
     * @param properties 锁配置属性
     * @return JDBC 锁表初始化器
     */
    @Bean
    @ConditionalOnProperty(prefix = "getboot.lock.database", name = "initialize-schema", havingValue = "true")
    @ConditionalOnMissingBean
    public JdbcDistributedLockSchemaInitializer jdbcDistributedLockSchemaInitializer(
            DataSource dataSource,
            LockProperties properties) {
        return new JdbcDistributedLockSchemaInitializer(
                new JdbcTemplate(dataSource),
                properties.getDatabase().getTableName()
        );
    }
}
