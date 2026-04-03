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
package com.getboot.search.spi;

/**
 * 搜索索引名解析器。
 *
 * @author qiheng
 */
public interface SearchIndexNameResolver {

    /**
     * 解析最终索引名。
     *
     * @param requestedIndexName 请求中的逻辑索引名或物理索引名
     * @return 最终索引名
     */
    String resolveIndexName(String requestedIndexName);
}
