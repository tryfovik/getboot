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
package com.getboot.storage.api.model;

import lombok.Data;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 对象元数据描述。
 *
 * @author qiheng
 */
@Data
public class StorageObjectMetadata {

    /**
     * 所属存储桶。
     */
    private String bucket;

    /**
     * 对象键。
     */
    private String objectKey;

    /**
     * 内容长度。
     */
    private long contentLength;

    /**
     * 内容类型。
     */
    private String contentType;

    /**
     * ETag。
     */
    private String etag;

    /**
     * 版本号。
     */
    private String versionId;

    /**
     * 用户元数据。
     */
    private Map<String, String> metadata = new LinkedHashMap<>();

    /**
     * 设置用户元数据。
     *
     * @param metadata 用户元数据
     */
    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata == null ? new LinkedHashMap<>() : new LinkedHashMap<>(metadata);
    }
}
