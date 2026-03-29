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
package com.getboot.database.infrastructure.sharding.autoconfigure;

import com.getboot.database.api.properties.DatabaseProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.driver.api.yaml.YamlShardingSphereDataSourceFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.FileCopyUtils;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * ShardingSphere 数据源自动配置。
 *
 * <p>当前通过规则 YAML 文件装配 ShardingSphere 数据源，并支持复用 Spring 容器内已有底层数据源。</p>
 *
 * @author qiheng
 */
@Slf4j
@AutoConfiguration(before = com.getboot.database.infrastructure.datasource.autoconfigure.DataSourceAutoConfiguration.class)
@ConditionalOnClass({DataSource.class, YamlShardingSphereDataSourceFactory.class})
@ConditionalOnProperty(prefix = "getboot.database", name = {"enabled", "sharding.enabled"}, havingValue = "true")
@EnableConfigurationProperties(DatabaseProperties.class)
public class ShardingDataSourceAutoConfiguration {

    /**
     * 注册 ShardingSphere 数据源。
     *
     * @param applicationContext Spring 容器
     * @param resourceLoader 资源加载器
     * @param environment 当前环境
     * @param properties 分库分表配置
     * @return ShardingSphere 数据源
     * @throws SQLException 创建数据源失败
     * @throws IOException 读取规则文件失败
     */
    @Bean(name = "shardingSphereDataSource")
    @Primary
    @ConditionalOnMissingBean(name = "shardingSphereDataSource")
    public DataSource shardingSphereDataSource(ApplicationContext applicationContext,
                                               ResourceLoader resourceLoader,
                                               Environment environment,
                                               DatabaseProperties databaseProperties) throws SQLException, IOException {
        DatabaseProperties.Sharding properties = databaseProperties.getSharding();
        byte[] yamlBytes = loadRuleConfiguration(resourceLoader, environment, properties.getRuleConfig());
        if (properties.isReuseBeanDataSources()) {
            Map<String, DataSource> dataSources = resolveReusableDataSources(applicationContext, properties);
            log.info("Creating ShardingSphere datasource from existing datasource beans. ruleConfig={}, dataSources={}, transactionType={}",
                    properties.getRuleConfig(), dataSources.keySet(), properties.getTransactionType());
            return YamlShardingSphereDataSourceFactory.createDataSource(dataSources, yamlBytes);
        }
        log.info("Creating ShardingSphere datasource from standalone yaml config. ruleConfig={}, transactionType={}",
                properties.getRuleConfig(), properties.getTransactionType());
        return YamlShardingSphereDataSourceFactory.createDataSource(yamlBytes);
    }

    private byte[] loadRuleConfiguration(ResourceLoader resourceLoader,
                                         Environment environment,
                                         String ruleConfigLocation) throws IOException {
        Resource resource = resourceLoader.getResource(ruleConfigLocation);
        if (!resource.exists()) {
            throw new IllegalStateException("Sharding rule config not found: " + ruleConfigLocation);
        }
        try (var inputStream = resource.getInputStream()) {
            String content = new String(FileCopyUtils.copyToByteArray(inputStream), StandardCharsets.UTF_8);
            return environment.resolvePlaceholders(content).getBytes(StandardCharsets.UTF_8);
        }
    }

    private Map<String, DataSource> resolveReusableDataSources(ApplicationContext applicationContext,
                                                               DatabaseProperties.Sharding properties) {
        Map<String, DataSource> candidates = new LinkedHashMap<>(applicationContext.getBeansOfType(DataSource.class));
        candidates.remove("shardingSphereDataSource");
        if (candidates.isEmpty()) {
            throw new IllegalStateException("No datasource bean found for sharding reuse.");
        }
        List<String> requestedBeans = properties.getDataSourceBeans();
        if (requestedBeans == null || requestedBeans.isEmpty()) {
            return candidates;
        }
        Map<String, DataSource> selected = new LinkedHashMap<>();
        for (String beanName : requestedBeans) {
            DataSource dataSource = candidates.get(beanName);
            if (dataSource == null) {
                throw new IllegalStateException("Configured sharding datasource bean not found: " + beanName);
            }
            selected.put(beanName, dataSource);
        }
        return selected;
    }
}
