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
package com.getboot.storage.infrastructure.minio.autoconfigure;

import com.getboot.storage.api.operator.StorageOperator;
import com.getboot.storage.api.properties.StorageProperties;
import com.getboot.storage.infrastructure.minio.support.MinioStorageOperator;
import com.getboot.storage.spi.StorageBucketRouter;
import com.getboot.storage.spi.StorageMetadataCustomizer;
import com.getboot.storage.spi.StorageObjectKeyGenerator;
import io.minio.MinioClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * MinIO 对象存储自动配置。
 *
 * @author qiheng
 */
@AutoConfiguration
@ConditionalOnClass(MinioClient.class)
@ConditionalOnProperty(prefix = "getboot.storage", name = "enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnExpression("'${getboot.storage.type:minio}' == 'minio' and '${getboot.storage.minio.enabled:true}' == 'true'")
public class MinioStorageAutoConfiguration {

    /**
     * 注册 MinIO 客户端。
     *
     * @param properties 对象存储模块配置
     * @return MinIO 客户端
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "getboot.storage.minio", name = {"endpoint", "access-key", "secret-key"})
    public MinioClient minioClient(StorageProperties properties) {
        StorageProperties.Minio minio = properties.getMinio();
        MinioClient.Builder builder = MinioClient.builder()
                .endpoint(minio.getEndpoint())
                .credentials(minio.getAccessKey(), minio.getSecretKey());
        if (StringUtils.hasText(minio.getRegion())) {
            builder.region(minio.getRegion().trim());
        }
        return builder.build();
    }

    /**
     * 注册默认对象存储门面。
     *
     * @param minioClient MinIO 客户端
     * @param storageBucketRouter 存储桶路由器
     * @param storageObjectKeyGenerator 对象键生成器
     * @param metadataCustomizers 元数据定制器提供器
     * @param properties 对象存储模块配置
     * @return 对象存储门面
     */
    @Bean
    @ConditionalOnBean(MinioClient.class)
    @ConditionalOnMissingBean(StorageOperator.class)
    public StorageOperator storageOperator(MinioClient minioClient,
                                           StorageBucketRouter storageBucketRouter,
                                           StorageObjectKeyGenerator storageObjectKeyGenerator,
                                           ObjectProvider<StorageMetadataCustomizer> metadataCustomizers,
                                           StorageProperties properties) {
        List<StorageMetadataCustomizer> customizers = metadataCustomizers.orderedStream().toList();
        return new MinioStorageOperator(
                minioClient,
                storageBucketRouter,
                storageObjectKeyGenerator,
                customizers,
                properties
        );
    }
}
