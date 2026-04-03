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
package com.getboot.search.api.response;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 搜索分页响应。
 *
 * @param <T> 文档类型
 * @author qiheng
 */
@Data
@Builder
public class SearchPageResponse<T> {

    /**
     * 当前页码。
     */
    private Integer pageNo;

    /**
     * 分页大小。
     */
    private Integer pageSize;

    /**
     * 总记录数。
     */
    private Long total;

    /**
     * 查询耗时，单位毫秒。
     */
    private Long tookMs;

    /**
     * 命中结果列表。
     */
    @Builder.Default
    private List<SearchHit<T>> items = new ArrayList<>();
}
