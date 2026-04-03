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
package com.getboot.search.infrastructure.elasticsearch.support;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.getboot.search.api.exception.SearchException;
import com.getboot.search.api.model.SearchSortField;
import com.getboot.search.api.model.SearchSortOrder;
import com.getboot.search.api.operator.SearchOperator;
import com.getboot.search.api.properties.SearchProperties;
import com.getboot.search.api.request.SearchDeleteRequest;
import com.getboot.search.api.request.SearchIndexRequest;
import com.getboot.search.api.request.SearchQueryRequest;
import com.getboot.search.api.response.SearchDeleteResponse;
import com.getboot.search.api.response.SearchHit;
import com.getboot.search.api.response.SearchIndexResponse;
import com.getboot.search.api.response.SearchPageResponse;
import com.getboot.search.spi.SearchIndexNameResolver;
import com.getboot.search.support.SearchSupport;
import org.springframework.util.StringUtils;

import java.lang.reflect.Array;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 基于 Elasticsearch 的默认搜索门面实现。
 *
 * @author qiheng
 */
public class ElasticsearchSearchOperator implements SearchOperator {

    /**
     * Elasticsearch 请求网关。
     */
    private final ElasticsearchRestGateway gateway;

    /**
     * 索引名解析器。
     */
    private final SearchIndexNameResolver indexNameResolver;

    /**
     * Jackson 映射器。
     */
    private final ObjectMapper objectMapper;

    /**
     * 搜索模块配置。
     */
    private final SearchProperties properties;

    /**
     * 构造默认搜索门面。
     *
     * @param gateway Elasticsearch 请求网关
     * @param indexNameResolver 索引名解析器
     * @param objectMapper Jackson 映射器
     * @param properties 搜索模块配置
     */
    public ElasticsearchSearchOperator(
            ElasticsearchRestGateway gateway,
            SearchIndexNameResolver indexNameResolver,
            ObjectMapper objectMapper,
            SearchProperties properties) {
        this.gateway = gateway;
        this.indexNameResolver = indexNameResolver;
        this.objectMapper = objectMapper;
        this.properties = properties;
    }

    /**
     * 写入或更新索引文档。
     *
     * @param request 索引写入请求
     * @return 写入结果
     */
    @Override
    public SearchIndexResponse index(SearchIndexRequest request) {
        String indexName = indexNameResolver.resolveIndexName(request == null ? null : request.getIndexName());
        SearchSupport.requireDocument(request == null ? null : request.getDocument());
        String documentId = request == null ? null : request.getDocumentId();

        String endpoint;
        String method;
        if (StringUtils.hasText(documentId)) {
            endpoint = "/" + encodePathSegment(indexName) + "/_doc/" + encodePathSegment(documentId);
            method = "PUT";
        } else {
            endpoint = "/" + encodePathSegment(indexName) + "/_doc";
            method = "POST";
        }
        JsonNode response = gateway.execute(
                method,
                endpoint,
                refreshParameters(request != null && request.isRefresh()),
                request.getDocument()
        );
        return SearchIndexResponse.builder()
                .indexName(text(response, "_index"))
                .documentId(text(response, "_id"))
                .version(number(response, "_version"))
                .created("created".equalsIgnoreCase(text(response, "result")))
                .build();
    }

    /**
     * 删除索引文档。
     *
     * @param request 索引删除请求
     * @return 删除结果
     */
    @Override
    public SearchDeleteResponse delete(SearchDeleteRequest request) {
        String indexName = indexNameResolver.resolveIndexName(request == null ? null : request.getIndexName());
        String documentId = SearchSupport.requireDocumentId(request == null ? null : request.getDocumentId());
        JsonNode response = gateway.execute(
                "DELETE",
                "/" + encodePathSegment(indexName) + "/_doc/" + encodePathSegment(documentId),
                refreshParameters(request != null && request.isRefresh()),
                null
        );
        return SearchDeleteResponse.builder()
                .indexName(text(response, "_index"))
                .documentId(text(response, "_id"))
                .deleted("deleted".equalsIgnoreCase(text(response, "result")))
                .build();
    }

    /**
     * 执行基础搜索查询。
     *
     * @param request 查询请求
     * @param documentType 文档映射类型
     * @param <T> 文档类型
     * @return 分页查询结果
     */
    @Override
    public <T> SearchPageResponse<T> search(SearchQueryRequest request, Class<T> documentType) {
        if (documentType == null) {
            throw new SearchException("Search documentType must not be null.");
        }
        String indexName = indexNameResolver.resolveIndexName(request == null ? null : request.getIndexName());
        int pageNo = SearchSupport.normalizePageNo(request == null ? null : request.getPageNo());
        int pageSize = SearchSupport.normalizePageSize(request == null ? null : request.getPageSize(), properties);
        JsonNode response = gateway.execute(
                "POST",
                "/" + encodePathSegment(indexName) + "/_search",
                Map.of(),
                buildSearchBody(request, pageNo, pageSize)
        );
        return buildSearchResponse(response, documentType, pageNo, pageSize);
    }

    /**
     * 构建刷新参数。
     *
     * @param refresh 是否刷新索引
     * @return 刷新参数
     */
    private Map<String, String> refreshParameters(boolean refresh) {
        return refresh ? Map.of("refresh", "true") : Map.of();
    }

    /**
     * 构建搜索请求体。
     *
     * @param request 查询请求
     * @param pageNo 页码
     * @param pageSize 分页大小
     * @return 搜索请求体
     */
    private Map<String, Object> buildSearchBody(SearchQueryRequest request, int pageNo, int pageSize) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("from", (pageNo - 1) * pageSize);
        body.put("size", pageSize);
        body.put("track_total_hits", request == null || request.isTrackTotalHits());
        body.put("query", buildQuery(request));

        Map<String, Object> source = buildSourceFilter(request);
        if (!source.isEmpty()) {
            body.put("_source", source);
        }

        List<Map<String, Object>> sorts = buildSorts(request);
        if (!sorts.isEmpty()) {
            body.put("sort", sorts);
        }

        Map<String, Object> highlight = buildHighlight(request);
        if (!highlight.isEmpty()) {
            body.put("highlight", highlight);
        }
        return body;
    }

    /**
     * 构建查询 DSL。
     *
     * @param request 查询请求
     * @return 查询 DSL
     */
    private Map<String, Object> buildQuery(SearchQueryRequest request) {
        List<Object> mustClauses = new ArrayList<>();
        List<Object> filterClauses = new ArrayList<>();

        if (request != null && SearchSupport.hasKeyword(request.getKeyword())) {
            if (request.getKeywordFields().isEmpty()) {
                mustClauses.add(Map.of("query_string", Map.of("query", request.getKeyword().trim())));
            } else {
                Map<String, Object> multiMatch = new LinkedHashMap<>();
                multiMatch.put("query", request.getKeyword().trim());
                multiMatch.put("fields", request.getKeywordFields());
                mustClauses.add(Map.of("multi_match", multiMatch));
            }
        }

        if (request != null) {
            for (Map.Entry<String, Object> entry : request.getTermFilters().entrySet()) {
                if (!StringUtils.hasText(entry.getKey()) || entry.getValue() == null) {
                    continue;
                }
                if (isMultiValue(entry.getValue())) {
                    filterClauses.add(Map.of("terms", Map.of(entry.getKey(), toList(entry.getValue()))));
                } else {
                    filterClauses.add(Map.of("term", Map.of(entry.getKey(), entry.getValue())));
                }
            }
        }

        if (mustClauses.isEmpty() && filterClauses.isEmpty()) {
            return Map.of("match_all", Map.of());
        }

        Map<String, Object> bool = new LinkedHashMap<>();
        if (!mustClauses.isEmpty()) {
            bool.put("must", mustClauses);
        }
        if (!filterClauses.isEmpty()) {
            bool.put("filter", filterClauses);
        }
        return Map.of("bool", bool);
    }

    /**
     * 构建结果字段过滤配置。
     *
     * @param request 查询请求
     * @return 字段过滤配置
     */
    private Map<String, Object> buildSourceFilter(SearchQueryRequest request) {
        Map<String, Object> source = new LinkedHashMap<>();
        if (request == null) {
            return source;
        }
        if (!request.getSourceIncludes().isEmpty()) {
            source.put("includes", request.getSourceIncludes());
        }
        if (!request.getSourceExcludes().isEmpty()) {
            source.put("excludes", request.getSourceExcludes());
        }
        return source;
    }

    /**
     * 构建排序配置。
     *
     * @param request 查询请求
     * @return 排序配置
     */
    private List<Map<String, Object>> buildSorts(SearchQueryRequest request) {
        List<Map<String, Object>> sorts = new ArrayList<>();
        if (request == null) {
            return sorts;
        }
        for (SearchSortField sortField : request.getSortFields()) {
            if (sortField == null || !StringUtils.hasText(sortField.getFieldName())) {
                continue;
            }
            Map<String, Object> order = new LinkedHashMap<>();
            order.put("order", sortField.getOrder() == SearchSortOrder.ASC ? "asc" : "desc");
            sorts.add(Map.of(sortField.getFieldName().trim(), order));
        }
        return sorts;
    }

    /**
     * 构建高亮配置。
     *
     * @param request 查询请求
     * @return 高亮配置
     */
    private Map<String, Object> buildHighlight(SearchQueryRequest request) {
        Map<String, Object> highlight = new LinkedHashMap<>();
        if (request == null || request.getHighlightFields().isEmpty()) {
            return highlight;
        }
        Map<String, Object> fields = new LinkedHashMap<>();
        for (String field : request.getHighlightFields()) {
            if (StringUtils.hasText(field)) {
                fields.put(field.trim(), Map.of());
            }
        }
        if (!fields.isEmpty()) {
            highlight.put("fields", fields);
        }
        return highlight;
    }

    /**
     * 构建分页响应对象。
     *
     * @param response Elasticsearch 响应
     * @param documentType 文档类型
     * @param pageNo 页码
     * @param pageSize 分页大小
     * @param <T> 文档类型
     * @return 分页响应对象
     */
    private <T> SearchPageResponse<T> buildSearchResponse(
            JsonNode response,
            Class<T> documentType,
            int pageNo,
            int pageSize) {
        List<SearchHit<T>> items = new ArrayList<>();
        JsonNode hitNodes = response.path("hits").path("hits");
        if (hitNodes.isArray()) {
            for (JsonNode hitNode : hitNodes) {
                items.add(SearchHit.<T>builder()
                        .documentId(text(hitNode, "_id"))
                        .score(hitNode.path("_score").isMissingNode() ? null : hitNode.path("_score").asDouble())
                        .highlights(parseHighlights(hitNode.path("highlight")))
                        .document(convertDocument(hitNode.path("_source"), documentType))
                        .build());
            }
        }

        return SearchPageResponse.<T>builder()
                .pageNo(pageNo)
                .pageSize(pageSize)
                .total(resolveTotal(response.path("hits").path("total")))
                .tookMs(response.path("took").isNumber() ? response.path("took").asLong() : null)
                .items(items)
                .build();
    }

    /**
     * 解析总命中数。
     *
     * @param totalNode 总命中数字段
     * @return 总命中数
     */
    private Long resolveTotal(JsonNode totalNode) {
        if (totalNode == null || totalNode.isMissingNode() || totalNode.isNull()) {
            return 0L;
        }
        if (totalNode.isNumber()) {
            return totalNode.asLong();
        }
        return totalNode.path("value").isNumber() ? totalNode.path("value").asLong() : 0L;
    }

    /**
     * 转换命中文档。
     *
     * @param sourceNode 文档节点
     * @param documentType 文档类型
     * @param <T> 文档类型
     * @return 文档对象
     */
    private <T> T convertDocument(JsonNode sourceNode, Class<T> documentType) {
        if (sourceNode == null || sourceNode.isMissingNode() || sourceNode.isNull()) {
            return null;
        }
        return objectMapper.convertValue(sourceNode, documentType);
    }

    /**
     * 解析高亮片段。
     *
     * @param highlightNode 高亮节点
     * @return 高亮片段映射
     */
    private Map<String, List<String>> parseHighlights(JsonNode highlightNode) {
        Map<String, List<String>> highlights = new LinkedHashMap<>();
        if (highlightNode == null || !highlightNode.isObject()) {
            return highlights;
        }
        highlightNode.fields().forEachRemaining(entry -> {
            List<String> fragments = new ArrayList<>();
            if (entry.getValue().isArray()) {
                for (JsonNode fragment : entry.getValue()) {
                    fragments.add(fragment.asText());
                }
            }
            highlights.put(entry.getKey(), fragments);
        });
        return highlights;
    }

    /**
     * 读取文本字段。
     *
     * @param node JSON 节点
     * @param fieldName 字段名
     * @return 文本值
     */
    private String text(JsonNode node, String fieldName) {
        JsonNode value = node.path(fieldName);
        return value.isMissingNode() || value.isNull() ? null : value.asText();
    }

    /**
     * 读取长整数字段。
     *
     * @param node JSON 节点
     * @param fieldName 字段名
     * @return 长整数字段值
     */
    private Long number(JsonNode node, String fieldName) {
        JsonNode value = node.path(fieldName);
        return value.isNumber() ? value.asLong() : null;
    }

    /**
     * 判断是否为多值过滤条件。
     *
     * @param value 过滤值
     * @return 是否为多值
     */
    private boolean isMultiValue(Object value) {
        if (value instanceof Iterable<?>) {
            return true;
        }
        return value != null && value.getClass().isArray();
    }

    /**
     * 将过滤值转换为列表。
     *
     * @param value 过滤值
     * @return 列表结果
     */
    private List<Object> toList(Object value) {
        List<Object> items = new ArrayList<>();
        if (value instanceof Iterable<?> iterable) {
            iterable.forEach(items::add);
            return items;
        }
        if (value != null && value.getClass().isArray()) {
            int length = Array.getLength(value);
            for (int i = 0; i < length; i++) {
                items.add(Array.get(value, i));
            }
            return items;
        }
        items.add(value);
        return items;
    }

    /**
     * 对路径片段执行 URL 编码。
     *
     * @param value 原始值
     * @return 编码后的值
     */
    private String encodePathSegment(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
    }
}
