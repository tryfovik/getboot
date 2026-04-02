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
package com.getboot.database.infrastructure.mongodb.autoconfigure;

import com.getboot.database.api.properties.DatabaseProperties;
import com.getboot.database.support.mongodb.MongoDatabaseInitializer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.core.MongoTemplate;

/**
 * MongoDB 增强自动配置。
 *
 * <p>当前负责 MongoTemplate 启动预热与连通性校验。</p>
 *
 * @author qiheng
 */
@AutoConfiguration
@ConditionalOnClass(MongoTemplate.class)
@EnableConfigurationProperties(DatabaseProperties.class)
@ConditionalOnProperty(prefix = "getboot.database", name = {"enabled", "mongodb.enabled"}, havingValue = "true")
public class MongoDatabaseAutoConfiguration {

    @Bean
    @ConditionalOnBean(MongoTemplate.class)
    @ConditionalOnMissingBean
    @ConditionalOnProperty(
            value = "getboot.database.mongodb.init.enabled",
            havingValue = "true",
            matchIfMissing = true
    )
    public MongoDatabaseInitializer mongoDatabaseInitializer(MongoTemplate mongoTemplate,
                                                             DatabaseProperties databaseProperties) {
        return new MongoDatabaseInitializer(mongoTemplate, databaseProperties.getMongodb().getInit());
    }
}
