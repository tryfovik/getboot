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
package com.getboot.storage.spi;

/**
 * 存储桶路由扩展点。
 *
 * @author qiheng
 */
public interface StorageBucketRouter {

    /**
     * 解析最终存储桶。
     *
     * @param scene 业务场景
     * @param requestedBucket 显式指定的存储桶
     * @return 最终存储桶
     */
    String resolveBucket(String scene, String requestedBucket);
}
