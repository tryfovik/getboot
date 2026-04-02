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
package com.getboot.storage.api.request;

import com.getboot.storage.api.model.StoragePresignMethod;
import lombok.Data;

import java.time.Duration;

/**
 * 预签名 URL 请求。
 *
 * @author qiheng
 */
@Data
public class StoragePresignRequest {

    /**
     * 预签名请求方法。
     */
    private StoragePresignMethod method = StoragePresignMethod.DOWNLOAD;

    /**
     * 业务场景。
     */
    private String scene;

    /**
     * 指定存储桶。
     */
    private String bucket;

    /**
     * 对象键。
     */
    private String objectKey;

    /**
     * 原始文件名。
     */
    private String originalFilename;

    /**
     * 内容类型。
     */
    private String contentType;

    /**
     * 预签名有效期。
     */
    private Duration ttl;
}
