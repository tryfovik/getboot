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

import com.getboot.search.api.constant.SearchConstants;
import com.getboot.search.api.model.SearchSortField;
import lombok.Data;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 搜索查询请求。
 *
 * @author qiheng
 */
@Data
public class SearchQueryRequest {

    /**
     * 逻辑索引名或物理索引名。
     */
    private String indexName;

    /**
     * 全文检索关键字。
     */
    private String keyword;

    /**
     * 全文检索字段列表。
     */
    private List<String> keywordFields = new ArrayList<>();

    /**
     * 精确过滤条件。
     */
    private Map<String, Object> termFilters = new LinkedHashMap<>();

    /**
     * 高亮字段列表。
     */
    private List<String> highlightFields = new ArrayList<>();

    /**
     * 排序字段列表。
     */
    private List<SearchSortField> sortFields = new ArrayList<>();

    /**
     * 结果包含字段列表。
     */
    private List<String> sourceIncludes = new ArrayList<>();

    /**
     * 结果排除字段列表。
     */
    private List<String> sourceExcludes = new ArrayList<>();

    /**
     * 页码，从 1 开始。
     */
    private Integer pageNo = SearchConstants.DEFAULT_PAGE_NO;

    /**
     * 分页大小。
     */
    private Integer pageSize = SearchConstants.DEFAULT_PAGE_SIZE;

    /**
     * 是否统计总命中数。
     */
    private boolean trackTotalHits = true;

    /**
     * 设置全文检索字段列表。
     *
     * @param keywordFields 全文检索字段列表
     */
    public void setKeywordFields(List<String> keywordFields) {
        this.keywordFields = keywordFields == null ? new ArrayList<>() : new ArrayList<>(keywordFields);
    }

    /**
     * 设置精确过滤条件。
     *
     * @param termFilters 精确过滤条件
     */
    public void setTermFilters(Map<String, Object> termFilters) {
        this.termFilters = termFilters == null ? new LinkedHashMap<>() : new LinkedHashMap<>(termFilters);
    }

    /**
     * 设置高亮字段列表。
     *
     * @param highlightFields 高亮字段列表
     */
    public void setHighlightFields(List<String> highlightFields) {
        this.highlightFields = highlightFields == null ? new ArrayList<>() : new ArrayList<>(highlightFields);
    }

    /**
     * 设置排序字段列表。
     *
     * @param sortFields 排序字段列表
     */
    public void setSortFields(List<SearchSortField> sortFields) {
        this.sortFields = sortFields == null ? new ArrayList<>() : new ArrayList<>(sortFields);
    }

    /**
     * 设置结果包含字段列表。
     *
     * @param sourceIncludes 结果包含字段列表
     */
    public void setSourceIncludes(List<String> sourceIncludes) {
        this.sourceIncludes = sourceIncludes == null ? new ArrayList<>() : new ArrayList<>(sourceIncludes);
    }

    /**
     * 设置结果排除字段列表。
     *
     * @param sourceExcludes 结果排除字段列表
     */
    public void setSourceExcludes(List<String> sourceExcludes) {
        this.sourceExcludes = sourceExcludes == null ? new ArrayList<>() : new ArrayList<>(sourceExcludes);
    }
}
