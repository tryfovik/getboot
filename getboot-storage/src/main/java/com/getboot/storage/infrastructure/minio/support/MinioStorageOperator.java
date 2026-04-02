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
package com.getboot.storage.infrastructure.minio.support;

import com.getboot.storage.api.exception.StorageException;
import com.getboot.storage.api.model.StorageObjectMetadata;
import com.getboot.storage.api.model.StoragePresignMethod;
import com.getboot.storage.api.operator.StorageOperator;
import com.getboot.storage.api.properties.StorageProperties;
import com.getboot.storage.api.request.StorageObjectRequest;
import com.getboot.storage.api.request.StoragePresignRequest;
import com.getboot.storage.api.request.StorageUploadRequest;
import com.getboot.storage.api.response.StorageDownloadResponse;
import com.getboot.storage.api.response.StoragePresignResponse;
import com.getboot.storage.spi.StorageBucketRouter;
import com.getboot.storage.spi.StorageMetadataCustomizer;
import com.getboot.storage.spi.StorageObjectKeyGenerator;
import com.getboot.storage.support.StorageSupport;
import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.GetObjectResponse;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.ObjectWriteResponse;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import io.minio.http.Method;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 基于 MinIO 的对象存储门面实现。
 *
 * @author qiheng
 */
public class MinioStorageOperator implements StorageOperator {

    /**
     * 未知大小上传时使用的分片大小。
     */
    private static final long UNKNOWN_SIZE_PART_SIZE = 10 * 1024 * 1024L;

    /**
     * MinIO 允许的最大预签名有效期。
     */
    private static final Duration MAX_PRESIGNED_TTL = Duration.ofDays(7);

    /**
     * MinIO 客户端。
     */
    private final MinioClient minioClient;

    /**
     * 存储桶路由器。
     */
    private final StorageBucketRouter storageBucketRouter;

    /**
     * 对象键生成器。
     */
    private final StorageObjectKeyGenerator storageObjectKeyGenerator;

    /**
     * 元数据定制器集合。
     */
    private final List<StorageMetadataCustomizer> metadataCustomizers;

    /**
     * 对象存储模块配置。
     */
    private final StorageProperties properties;

    /**
     * 构造 MinIO 对象存储门面。
     *
     * @param minioClient MinIO 客户端
     * @param storageBucketRouter 存储桶路由器
     * @param storageObjectKeyGenerator 对象键生成器
     * @param metadataCustomizers 元数据定制器集合
     * @param properties 对象存储模块配置
     */
    public MinioStorageOperator(MinioClient minioClient,
                                StorageBucketRouter storageBucketRouter,
                                StorageObjectKeyGenerator storageObjectKeyGenerator,
                                List<StorageMetadataCustomizer> metadataCustomizers,
                                StorageProperties properties) {
        this.minioClient = minioClient;
        this.storageBucketRouter = storageBucketRouter;
        this.storageObjectKeyGenerator = storageObjectKeyGenerator;
        this.metadataCustomizers = metadataCustomizers;
        this.properties = properties;
    }

    /**
     * 上传对象。
     *
     * @param request 上传请求
     * @return 对象元数据
     */
    @Override
    public StorageObjectMetadata upload(StorageUploadRequest request) {
        if (request == null || request.getInputStream() == null) {
            throw new StorageException("Storage upload inputStream must not be null.");
        }
        String bucket = StorageSupport.resolveBucket(
                request.getScene(),
                request.getBucket(),
                storageBucketRouter
        );
        String objectKey = StorageSupport.resolveObjectKey(
                request.getScene(),
                request.getObjectKey(),
                request.getOriginalFilename(),
                storageObjectKeyGenerator
        );
        ensureBucketExistsIfNecessary(bucket);
        Map<String, String> metadata = StorageSupport.mergeMetadata(request, metadataCustomizers);
        try {
            PutObjectArgs.Builder builder = PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectKey)
                    .stream(
                            request.getInputStream(),
                            request.getContentLength(),
                            request.getContentLength() >= 0 ? -1 : UNKNOWN_SIZE_PART_SIZE
                    );
            if (StringUtils.hasText(request.getContentType())) {
                builder.contentType(request.getContentType().trim());
            }
            if (!metadata.isEmpty()) {
                builder.userMetadata(metadata);
            }

            ObjectWriteResponse response = minioClient.putObject(builder.build());
            StorageObjectMetadata result = new StorageObjectMetadata();
            result.setBucket(bucket);
            result.setObjectKey(objectKey);
            result.setContentLength(request.getContentLength());
            result.setContentType(request.getContentType());
            result.setEtag(response.etag());
            result.setVersionId(response.versionId());
            result.setMetadata(metadata);
            return result;
        } catch (Exception ex) {
            throw new StorageException(
                    "Failed to upload object to MinIO. bucket=" + bucket + ", objectKey=" + objectKey,
                    ex
            );
        }
    }

    /**
     * 下载对象。
     *
     * @param request 查询请求
     * @return 下载响应
     */
    @Override
    public StorageDownloadResponse download(StorageObjectRequest request) {
        String bucket = StorageSupport.resolveBucket(
                request == null ? null : request.getScene(),
                request == null ? null : request.getBucket(),
                storageBucketRouter
        );
        String objectKey = StorageSupport.requireObjectKey(request == null ? null : request.getObjectKey());
        try {
            StatObjectResponse statObjectResponse = minioClient.statObject(
                    StatObjectArgs.builder().bucket(bucket).object(objectKey).build()
            );
            GetObjectResponse getObjectResponse = minioClient.getObject(
                    GetObjectArgs.builder().bucket(bucket).object(objectKey).build()
            );
            StorageObjectMetadata metadata = toMetadata(bucket, objectKey, statObjectResponse);
            return new StorageDownloadResponse(
                    metadata.getBucket(),
                    metadata.getObjectKey(),
                    metadata.getContentLength(),
                    metadata.getContentType(),
                    metadata.getEtag(),
                    metadata.getVersionId(),
                    metadata.getMetadata(),
                    getObjectResponse
            );
        } catch (Exception ex) {
            throw new StorageException(
                    "Failed to download object from MinIO. bucket=" + bucket + ", objectKey=" + objectKey,
                    ex
            );
        }
    }

    /**
     * 查询对象元数据。
     *
     * @param request 查询请求
     * @return 对象元数据
     */
    @Override
    public StorageObjectMetadata stat(StorageObjectRequest request) {
        String bucket = StorageSupport.resolveBucket(
                request == null ? null : request.getScene(),
                request == null ? null : request.getBucket(),
                storageBucketRouter
        );
        String objectKey = StorageSupport.requireObjectKey(request == null ? null : request.getObjectKey());
        try {
            StatObjectResponse response = minioClient.statObject(
                    StatObjectArgs.builder().bucket(bucket).object(objectKey).build()
            );
            return toMetadata(bucket, objectKey, response);
        } catch (Exception ex) {
            throw new StorageException(
                    "Failed to stat object from MinIO. bucket=" + bucket + ", objectKey=" + objectKey,
                    ex
            );
        }
    }

    /**
     * 生成预签名 URL。
     *
     * @param request 预签名请求
     * @return 预签名响应
     */
    @Override
    public StoragePresignResponse generatePresignedUrl(StoragePresignRequest request) {
        if (request == null || request.getMethod() == null) {
            throw new StorageException("Storage presign request method must not be null.");
        }

        String bucket = StorageSupport.resolveBucket(
                request.getScene(),
                request.getBucket(),
                storageBucketRouter
        );
        String objectKey;
        if (request.getMethod() == StoragePresignMethod.UPLOAD) {
            objectKey = StorageSupport.resolveObjectKey(
                    request.getScene(),
                    request.getObjectKey(),
                    request.getOriginalFilename(),
                    storageObjectKeyGenerator
            );
            ensureBucketExistsIfNecessary(bucket);
        } else {
            objectKey = StorageSupport.requireObjectKey(request.getObjectKey());
        }

        Duration ttl = StorageSupport.resolvePresignTtl(request.getTtl(), request.getMethod(), properties);
        if (ttl.compareTo(MAX_PRESIGNED_TTL) > 0) {
            throw new StorageException("MinIO presigned URL ttl must not exceed 7 days.");
        }
        try {
            String url = minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .bucket(bucket)
                            .object(objectKey)
                            .method(toMethod(request.getMethod()))
                            .expiry((int) ttl.getSeconds(), TimeUnit.SECONDS)
                            .build()
            );

            StoragePresignResponse response = new StoragePresignResponse();
            response.setBucket(bucket);
            response.setObjectKey(objectKey);
            response.setMethod(request.getMethod());
            response.setTtl(ttl);
            response.setUrl(rewriteEndpointIfNecessary(url));
            return response;
        } catch (Exception ex) {
            throw new StorageException(
                    "Failed to generate MinIO presigned URL. bucket=" + bucket + ", objectKey=" + objectKey,
                    ex
            );
        }
    }

    /**
     * 删除对象。
     *
     * @param request 删除请求
     */
    @Override
    public void delete(StorageObjectRequest request) {
        String bucket = StorageSupport.resolveBucket(
                request == null ? null : request.getScene(),
                request == null ? null : request.getBucket(),
                storageBucketRouter
        );
        String objectKey = StorageSupport.requireObjectKey(request == null ? null : request.getObjectKey());
        try {
            minioClient.removeObject(RemoveObjectArgs.builder().bucket(bucket).object(objectKey).build());
        } catch (Exception ex) {
            throw new StorageException(
                    "Failed to delete object from MinIO. bucket=" + bucket + ", objectKey=" + objectKey,
                    ex
            );
        }
    }

    /**
     * 在需要时确保存储桶存在。
     *
     * @param bucket 存储桶名称
     */
    private void ensureBucketExistsIfNecessary(String bucket) {
        if (!properties.getMinio().isCreateBucketIfMissing()) {
            return;
        }
        try {
            boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
            if (exists) {
                return;
            }
            MakeBucketArgs.Builder builder = MakeBucketArgs.builder().bucket(bucket);
            if (StringUtils.hasText(properties.getMinio().getRegion())) {
                builder.region(properties.getMinio().getRegion().trim());
            }
            minioClient.makeBucket(builder.build());
        } catch (Exception ex) {
            throw new StorageException("Failed to ensure MinIO bucket exists. bucket=" + bucket, ex);
        }
    }

    /**
     * 将预签名方法转换为 MinIO HTTP 方法。
     *
     * @param method 预签名方法
     * @return MinIO HTTP 方法
     */
    private Method toMethod(StoragePresignMethod method) {
        return method == StoragePresignMethod.UPLOAD ? Method.PUT : Method.GET;
    }

    /**
     * 将 MinIO 元数据响应转换为统一元数据对象。
     *
     * @param bucket 存储桶名称
     * @param objectKey 对象键
     * @param response MinIO 元数据响应
     * @return 统一元数据对象
     */
    private StorageObjectMetadata toMetadata(String bucket,
                                             String objectKey,
                                             StatObjectResponse response) {
        StorageObjectMetadata metadata = new StorageObjectMetadata();
        metadata.setBucket(bucket);
        metadata.setObjectKey(objectKey);
        metadata.setContentLength(response.size());
        metadata.setContentType(response.contentType());
        metadata.setEtag(response.etag());
        metadata.setVersionId(response.versionId());
        metadata.setMetadata(new LinkedHashMap<>(response.userMetadata()));
        return metadata;
    }

    /**
     * 在配置了公共地址时重写预签名 URL 前缀。
     *
     * @param generatedUrl MinIO 生成的预签名 URL
     * @return 重写后的预签名 URL
     */
    private String rewriteEndpointIfNecessary(String generatedUrl) {
        String publicEndpoint = properties.getMinio().getPublicEndpoint();
        String endpoint = properties.getMinio().getEndpoint();
        if (!StringUtils.hasText(publicEndpoint) || !StringUtils.hasText(endpoint)) {
            return generatedUrl;
        }
        String normalizedEndpoint = endpoint.trim().replaceAll("/+$", "");
        String normalizedPublicEndpoint = publicEndpoint.trim().replaceAll("/+$", "");
        return generatedUrl.startsWith(normalizedEndpoint)
                ? normalizedPublicEndpoint + generatedUrl.substring(normalizedEndpoint.length())
                : generatedUrl;
    }
}
