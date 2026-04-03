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
package com.getboot.search.api.request;

import lombok.Data;

/**
 * 索引删除请求。
 *
 * @author qiheng
 */
@Data
public class SearchDeleteRequest {

    /**
     * 逻辑索引名或物理索引名。
     */
    private String indexName;

    /**
     * 文档 ID。
     */
    private String documentId;

    /**
     * 删除后是否立即刷新索引。
     */
    private boolean refresh = false;
}
