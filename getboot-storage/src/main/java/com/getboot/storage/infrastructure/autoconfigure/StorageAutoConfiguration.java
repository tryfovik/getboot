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
package com.getboot.storage.infrastructure.autoconfigure;

import com.getboot.storage.infrastructure.minio.autoconfigure.MinioStorageAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

/**
 * 对象存储模块自动配置入口。
 *
 * @author qiheng
 */
@AutoConfiguration
@Import({
        StorageCoreAutoConfiguration.class,
        MinioStorageAutoConfiguration.class
})
public class StorageAutoConfiguration {
}
