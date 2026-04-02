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
package com.getboot.storage.api.properties;

import com.getboot.storage.api.constant.StorageConstants;
import com.getboot.storage.api.model.StoragePresignMethod;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 对象存储模块配置。
 *
 * @author qiheng
 */
@ConfigurationProperties(prefix = "getboot.storage")
@Data
public class StorageProperties {

    /**
     * 是否启用对象存储能力。
     */
    private boolean enabled = true;

    /**
     * 当前存储实现类型。
     */
    private String type = StorageConstants.STORAGE_TYPE_MINIO;

    /**
     * 默认存储桶。
     */
    private String defaultBucket;

    /**
     * 默认下载预签名有效期。
     */
    private Duration defaultDownloadUrlTtl = StorageConstants.DEFAULT_PRESIGNED_URL_TTL;

    /**
     * 默认上传预签名有效期。
     */
    private Duration defaultUploadUrlTtl = StorageConstants.DEFAULT_PRESIGNED_URL_TTL;

    /**
     * 场景到存储桶的路由配置。
     */
    private Map<String, String> sceneBuckets = new LinkedHashMap<>();

    /**
     * MinIO 配置。
     */
    private Minio minio = new Minio();

    /**
     * 按请求方法解析默认预签名有效期。
     *
     * @param method 预签名请求方法
     * @return 默认有效期
     */
    public Duration resolveDefaultTtl(StoragePresignMethod method) {
        return method == StoragePresignMethod.UPLOAD ? defaultUploadUrlTtl : defaultDownloadUrlTtl;
    }

    /**
     * 设置场景到存储桶的路由配置。
     *
     * @param sceneBuckets 场景到存储桶的路由配置
     */
    public void setSceneBuckets(Map<String, String> sceneBuckets) {
        this.sceneBuckets = sceneBuckets == null ? new LinkedHashMap<>() : new LinkedHashMap<>(sceneBuckets);
    }

    /**
     * MinIO 配置。
     *
     * @author qiheng
     */
    @Data
    public static class Minio {

        /**
         * 是否启用 MinIO 实现。
         */
        private boolean enabled = true;

        /**
         * MinIO 服务地址。
         */
        private String endpoint;

        /**
         * 对外暴露的公共地址。
         */
        private String publicEndpoint;

        /**
         * MinIO 访问密钥。
         */
        private String accessKey;

        /**
         * MinIO 访问密钥密码。
         */
        private String secretKey;

        /**
         * MinIO 所在地域。
         */
        private String region;

        /**
         * 不存在时是否自动创建存储桶。
         */
        private boolean createBucketIfMissing = false;
    }
}
