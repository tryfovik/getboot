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
package com.getboot.search.api.operator;

import com.getboot.search.api.request.SearchDeleteRequest;
import com.getboot.search.api.request.SearchIndexRequest;
import com.getboot.search.api.request.SearchQueryRequest;
import com.getboot.search.api.response.SearchDeleteResponse;
import com.getboot.search.api.response.SearchIndexResponse;
import com.getboot.search.api.response.SearchPageResponse;

/**
 * 搜索门面。
 *
 * @author qiheng
 */
public interface SearchOperator {

    /**
     * 写入或更新索引文档。
     *
     * @param request 索引写入请求
     * @return 写入结果
     */
    SearchIndexResponse index(SearchIndexRequest request);

    /**
     * 删除索引文档。
     *
     * @param request 索引删除请求
     * @return 删除结果
     */
    SearchDeleteResponse delete(SearchDeleteRequest request);

    /**
     * 执行基础搜索查询。
     *
     * @param request 查询请求
     * @param documentType 文档映射类型
     * @param <T> 文档类型
     * @return 分页查询结果
     */
    <T> SearchPageResponse<T> search(SearchQueryRequest request, Class<T> documentType);
}
