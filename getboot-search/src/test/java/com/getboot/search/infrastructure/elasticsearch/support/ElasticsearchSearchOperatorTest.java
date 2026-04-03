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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.getboot.search.api.model.SearchSortField;
import com.getboot.search.api.model.SearchSortOrder;
import com.getboot.search.api.properties.SearchProperties;
import com.getboot.search.api.request.SearchDeleteRequest;
import com.getboot.search.api.request.SearchIndexRequest;
import com.getboot.search.api.request.SearchQueryRequest;
import com.getboot.search.support.DefaultSearchIndexNameResolver;
import lombok.Data;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Elasticsearch 搜索门面测试。
 *
 * @author qiheng
 */
class ElasticsearchSearchOperatorTest {

    /**
     * Jackson 映射器。
     */
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 验证索引写入请求会映射到 Elasticsearch 接口。
     *
     * @throws Exception JSON 解析异常
     */
    @Test
    void shouldIndexDocumentWithRefresh() throws Exception {
        ElasticsearchRestGateway gateway = mock(ElasticsearchRestGateway.class);
        when(gateway.execute(eq("PUT"), eq("/prod-article/_doc/doc-001"), eq(Map.of("refresh", "true")), eq(Map.of("title", "GetBoot"))))
                .thenReturn(objectMapper.readTree("""
                        {
                          "_index": "prod-article",
                          "_id": "doc-001",
                          "_version": 2,
                          "result": "created"
                        }
                        """));

        ElasticsearchSearchOperator operator = new ElasticsearchSearchOperator(
                gateway,
                new DefaultSearchIndexNameResolver(searchProperties()),
                objectMapper,
                searchProperties()
        );

        SearchIndexRequest request = new SearchIndexRequest();
        request.setIndexName("article");
        request.setDocumentId("doc-001");
        request.setRefresh(true);
        request.setDocument(Map.of("title", "GetBoot"));

        var response = operator.index(request);

        assertEquals("prod-article", response.getIndexName());
        assertEquals("doc-001", response.getDocumentId());
        assertEquals(2L, response.getVersion());
        assertTrue(response.isCreated());
    }

    /**
     * 验证查询请求体和结果映射。
     *
     * @throws Exception JSON 解析异常
     */
    @Test
    void shouldSearchWithFiltersSortAndHighlight() throws Exception {
        ElasticsearchRestGateway gateway = mock(ElasticsearchRestGateway.class);
        when(gateway.execute(eq("POST"), eq("/prod-article/_search"), eq(Map.of()), org.mockito.ArgumentMatchers.any()))
                .thenReturn(objectMapper.readTree("""
                        {
                          "took": 12,
                          "hits": {
                            "total": {
                              "value": 1
                            },
                            "hits": [
                              {
                                "_id": "doc-001",
                                "_score": 1.25,
                                "_source": {
                                  "title": "GetBoot Search",
                                  "status": "ONLINE"
                                },
                                "highlight": {
                                  "title": [
                                    "<em>GetBoot</em> Search"
                                  ]
                                }
                              }
                            ]
                          }
                        }
                        """));

        ElasticsearchSearchOperator operator = new ElasticsearchSearchOperator(
                gateway,
                new DefaultSearchIndexNameResolver(searchProperties()),
                objectMapper,
                searchProperties()
        );

        SearchQueryRequest request = new SearchQueryRequest();
        request.setIndexName("article");
        request.setKeyword("getboot");
        request.setKeywordFields(List.of("title", "content"));
        request.setHighlightFields(List.of("title"));
        request.setTermFilters(Map.of("status", "ONLINE"));
        request.setPageNo(2);
        request.setPageSize(10);
        SearchSortField sortField = new SearchSortField();
        sortField.setFieldName("createdAt");
        sortField.setOrder(SearchSortOrder.DESC);
        request.setSortFields(List.of(sortField));

        var response = operator.search(request, ArticleDocument.class);

        ArgumentCaptor<Object> requestBodyCaptor = ArgumentCaptor.forClass(Object.class);
        verify(gateway).execute(eq("POST"), eq("/prod-article/_search"), eq(Map.of()), requestBodyCaptor.capture());

        @SuppressWarnings("unchecked")
        Map<String, Object> requestBody = (Map<String, Object>) requestBodyCaptor.getValue();
        assertEquals(10, requestBody.get("from"));
        assertEquals(10, requestBody.get("size"));
        assertTrue(((Map<?, ?>) requestBody.get("highlight")).containsKey("fields"));

        assertEquals(1L, response.getTotal());
        assertEquals(12L, response.getTookMs());
        assertEquals("doc-001", response.getItems().get(0).getDocumentId());
        assertEquals("GetBoot Search", response.getItems().get(0).getDocument().getTitle());
        assertEquals("<em>GetBoot</em> Search", response.getItems().get(0).getHighlights().get("title").get(0));
    }

    /**
     * 验证删除请求会映射到 Elasticsearch 接口。
     *
     * @throws Exception JSON 解析异常
     */
    @Test
    void shouldDeleteDocument() throws Exception {
        ElasticsearchRestGateway gateway = mock(ElasticsearchRestGateway.class);
        when(gateway.execute(eq("DELETE"), eq("/prod-article/_doc/doc-001"), eq(Map.of()), eq(null)))
                .thenReturn(objectMapper.readTree("""
                        {
                          "_index": "prod-article",
                          "_id": "doc-001",
                          "result": "deleted"
                        }
                        """));

        ElasticsearchSearchOperator operator = new ElasticsearchSearchOperator(
                gateway,
                new DefaultSearchIndexNameResolver(searchProperties()),
                objectMapper,
                searchProperties()
        );

        SearchDeleteRequest request = new SearchDeleteRequest();
        request.setIndexName("article");
        request.setDocumentId("doc-001");

        var response = operator.delete(request);

        assertEquals("prod-article", response.getIndexName());
        assertEquals("doc-001", response.getDocumentId());
        assertTrue(response.isDeleted());
    }

    /**
     * 构造测试使用的搜索配置。
     *
     * @return 搜索配置
     */
    private SearchProperties searchProperties() {
        SearchProperties properties = new SearchProperties();
        properties.setDefaultIndexPrefix("prod");
        return properties;
    }

    /**
     * 测试文档对象。
     *
     * @author qiheng
     */
    @Data
    public static class ArticleDocument {

        /**
         * 标题。
         */
        private String title;

        /**
         * 状态。
         */
        private String status;
    }
}
