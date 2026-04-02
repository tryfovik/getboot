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
package com.getboot.storage.api.operator;

import com.getboot.storage.api.model.StorageObjectMetadata;
import com.getboot.storage.api.request.StorageObjectRequest;
import com.getboot.storage.api.request.StoragePresignRequest;
import com.getboot.storage.api.request.StorageUploadRequest;
import com.getboot.storage.api.response.StorageDownloadResponse;
import com.getboot.storage.api.response.StoragePresignResponse;

/**
 * 对象存储门面。
 *
 * @author qiheng
 */
public interface StorageOperator {

    /**
     * 上传对象。
     *
     * @param request 上传请求
     * @return 对象元数据
     */
    StorageObjectMetadata upload(StorageUploadRequest request);

    /**
     * 下载对象。
     *
     * @param request 查询请求
     * @return 下载响应
     */
    StorageDownloadResponse download(StorageObjectRequest request);

    /**
     * 查询对象元数据。
     *
     * @param request 查询请求
     * @return 对象元数据
     */
    StorageObjectMetadata stat(StorageObjectRequest request);

    /**
     * 生成预签名 URL。
     *
     * @param request 预签名请求
     * @return 预签名响应
     */
    StoragePresignResponse generatePresignedUrl(StoragePresignRequest request);

    /**
     * 删除对象。
     *
     * @param request 删除请求
     */
    void delete(StorageObjectRequest request);
}
