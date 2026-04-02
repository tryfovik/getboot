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

import com.getboot.storage.api.properties.StorageProperties;
import com.getboot.storage.spi.StorageBucketRouter;
import org.springframework.util.StringUtils;

/**
 * 默认存储桶路由器。
 *
 * @author qiheng
 */
public class DefaultStorageBucketRouter implements StorageBucketRouter {

    /**
     * 对象存储模块配置。
     */
    private final StorageProperties properties;

    /**
     * 构造默认存储桶路由器。
     *
     * @param properties 对象存储模块配置
     */
    public DefaultStorageBucketRouter(StorageProperties properties) {
        this.properties = properties;
    }

    /**
     * 解析最终存储桶。
     *
     * @param scene 业务场景
     * @param requestedBucket 显式指定的存储桶
     * @return 最终存储桶
     */
    @Override
    public String resolveBucket(String scene, String requestedBucket) {
        if (StringUtils.hasText(requestedBucket)) {
            return requestedBucket.trim();
        }
        if (StringUtils.hasText(scene)) {
            String routedBucket = properties.getSceneBuckets().get(scene.trim());
            if (StringUtils.hasText(routedBucket)) {
                return routedBucket.trim();
            }
        }
        return StringUtils.hasText(properties.getDefaultBucket()) ? properties.getDefaultBucket().trim() : "";
    }
}
