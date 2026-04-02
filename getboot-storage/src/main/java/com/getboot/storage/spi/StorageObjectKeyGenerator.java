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
 * 对象键生成扩展点。
 *
 * @author qiheng
 */
public interface StorageObjectKeyGenerator {

    /**
     * 生成最终对象键。
     *
     * @param scene 业务场景
     * @param requestedObjectKey 显式指定的对象键
     * @param originalFilename 原始文件名
     * @return 最终对象键
     */
    String generateKey(String scene, String requestedObjectKey, String originalFilename);
}
