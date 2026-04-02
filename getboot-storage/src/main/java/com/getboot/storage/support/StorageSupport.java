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
package com.getboot.storage.support;

import com.getboot.storage.api.exception.StorageException;
import com.getboot.storage.api.model.StoragePresignMethod;
import com.getboot.storage.api.properties.StorageProperties;
import com.getboot.storage.api.request.StorageUploadRequest;
import com.getboot.storage.spi.StorageBucketRouter;
import com.getboot.storage.spi.StorageMetadataCustomizer;
import com.getboot.storage.spi.StorageObjectKeyGenerator;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 对象存储通用辅助方法。
 *
 * @author qiheng
 */
public final class StorageSupport {

    /**
     * 工具类私有构造器。
     */
    private StorageSupport() {
    }

    /**
     * 解析最终存储桶。
     *
     * @param scene 业务场景
     * @param requestedBucket 显式指定的存储桶
     * @param bucketRouter 存储桶路由器
     * @return 最终存储桶
     */
    public static String resolveBucket(String scene,
                                       String requestedBucket,
                                       StorageBucketRouter bucketRouter) {
        String bucket = bucketRouter.resolveBucket(scene, requestedBucket);
        if (!StringUtils.hasText(bucket)) {
            throw new StorageException("Storage bucket must not be empty.");
        }
        return bucket;
    }

    /**
     * 解析最终对象键。
     *
     * @param scene 业务场景
     * @param requestedObjectKey 显式指定的对象键
     * @param originalFilename 原始文件名
     * @param objectKeyGenerator 对象键生成器
     * @return 最终对象键
     */
    public static String resolveObjectKey(String scene,
                                          String requestedObjectKey,
                                          String originalFilename,
                                          StorageObjectKeyGenerator objectKeyGenerator) {
        String objectKey = objectKeyGenerator.generateKey(scene, requestedObjectKey, originalFilename);
        if (!StringUtils.hasText(objectKey)) {
            throw new StorageException("Storage object key must not be empty.");
        }
        return objectKey;
    }

    /**
     * 校验对象键。
     *
     * @param objectKey 对象键
     * @return 规整后的对象键
     */
    public static String requireObjectKey(String objectKey) {
        if (!StringUtils.hasText(objectKey)) {
            throw new StorageException("Storage object key must not be empty.");
        }
        return objectKey.trim();
    }

    /**
     * 解析预签名有效期。
     *
     * @param requestedTtl 显式指定的有效期
     * @param method 预签名方法
     * @param properties 对象存储模块配置
     * @return 最终有效期
     */
    public static Duration resolvePresignTtl(Duration requestedTtl,
                                             StoragePresignMethod method,
                                             StorageProperties properties) {
        Duration ttl = requestedTtl == null ? properties.resolveDefaultTtl(method) : requestedTtl;
        if (ttl == null || ttl.isNegative() || ttl.isZero()) {
            throw new StorageException("Storage presigned URL ttl must be greater than 0.");
        }
        return ttl;
    }

    /**
     * 合并上传元数据。
     *
     * @param request 上传请求
     * @param metadataCustomizers 元数据定制器
     * @return 合并后的元数据
     */
    public static Map<String, String> mergeMetadata(StorageUploadRequest request,
                                                    List<StorageMetadataCustomizer> metadataCustomizers) {
        Map<String, String> metadata = request.getMetadata() == null
                ? new LinkedHashMap<>()
                : new LinkedHashMap<>(request.getMetadata());
        if (metadataCustomizers == null) {
            return metadata;
        }
        metadataCustomizers.forEach(customizer -> customizer.customize(request, metadata));
        return metadata;
    }
}
