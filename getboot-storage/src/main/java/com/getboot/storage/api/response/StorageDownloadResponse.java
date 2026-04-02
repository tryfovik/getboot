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
package com.getboot.storage.api.response;

import lombok.Getter;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 对象下载响应。
 *
 * @author qiheng
 */
@Getter
public class StorageDownloadResponse implements AutoCloseable {

    /**
     * 所属存储桶。
     */
    private final String bucket;

    /**
     * 对象键。
     */
    private final String objectKey;

    /**
     * 内容长度。
     */
    private final long contentLength;

    /**
     * 内容类型。
     */
    private final String contentType;

    /**
     * ETag。
     */
    private final String etag;

    /**
     * 版本号。
     */
    private final String versionId;

    /**
     * 用户元数据。
     */
    private final Map<String, String> metadata;

    /**
     * 下载输入流。
     */
    private final InputStream inputStream;

    /**
     * 构造对象下载响应。
     *
     * @param bucket 所属存储桶
     * @param objectKey 对象键
     * @param contentLength 内容长度
     * @param contentType 内容类型
     * @param etag ETag
     * @param versionId 版本号
     * @param metadata 用户元数据
     * @param inputStream 下载输入流
     */
    public StorageDownloadResponse(String bucket,
                                   String objectKey,
                                   long contentLength,
                                   String contentType,
                                   String etag,
                                   String versionId,
                                   Map<String, String> metadata,
                                   InputStream inputStream) {
        this.bucket = bucket;
        this.objectKey = objectKey;
        this.contentLength = contentLength;
        this.contentType = contentType;
        this.etag = etag;
        this.versionId = versionId;
        this.metadata = metadata == null ? new LinkedHashMap<>() : new LinkedHashMap<>(metadata);
        this.inputStream = inputStream;
    }

    /**
     * 关闭下载输入流。
     *
     * @throws IOException 关闭输入流时的异常
     */
    @Override
    public void close() throws IOException {
        inputStream.close();
    }
}
