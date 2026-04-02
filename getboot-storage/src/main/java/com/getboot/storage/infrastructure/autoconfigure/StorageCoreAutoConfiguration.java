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
package com.getboot.storage.infrastructure.autoconfigure;

import com.getboot.storage.api.properties.StorageProperties;
import com.getboot.storage.spi.StorageBucketRouter;
import com.getboot.storage.spi.StorageObjectKeyGenerator;
import com.getboot.storage.support.DefaultStorageBucketRouter;
import com.getboot.storage.support.DefaultStorageObjectKeyGenerator;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * 对象存储核心自动配置。
 *
 * @author qiheng
 */
@AutoConfiguration
@ConditionalOnProperty(prefix = "getboot.storage", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(StorageProperties.class)
public class StorageCoreAutoConfiguration {

    /**
     * 注册默认存储桶路由器。
     *
     * @param properties 对象存储模块配置
     * @return 存储桶路由器
     */
    @Bean
    @ConditionalOnMissingBean
    public StorageBucketRouter storageBucketRouter(StorageProperties properties) {
        return new DefaultStorageBucketRouter(properties);
    }

    /**
     * 注册默认对象键生成器。
     *
     * @return 对象键生成器
     */
    @Bean
    @ConditionalOnMissingBean
    public StorageObjectKeyGenerator storageObjectKeyGenerator() {
        return new DefaultStorageObjectKeyGenerator();
    }
}
