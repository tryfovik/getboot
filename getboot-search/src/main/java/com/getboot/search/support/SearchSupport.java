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
package com.getboot.search.support;

import com.getboot.search.api.exception.SearchException;
import com.getboot.search.api.properties.SearchProperties;
import org.springframework.util.StringUtils;

/**
 * 搜索模块辅助工具。
 *
 * @author qiheng
 */
public final class SearchSupport {

    /**
     * 工具类不允许实例化。
     */
    private SearchSupport() {
    }

    /**
     * 校验索引名非空。
     *
     * @param indexName 索引名
     * @return 规整后的索引名
     */
    public static String requireIndexName(String indexName) {
        if (!StringUtils.hasText(indexName)) {
            throw new SearchException("Search indexName must not be blank.");
        }
        return indexName.trim();
    }

    /**
     * 校验文档 ID 非空。
     *
     * @param documentId 文档 ID
     * @return 规整后的文档 ID
     */
    public static String requireDocumentId(String documentId) {
        if (!StringUtils.hasText(documentId)) {
            throw new SearchException("Search documentId must not be blank.");
        }
        return documentId.trim();
    }

    /**
     * 校验文档内容非空。
     *
     * @param document 文档内容
     */
    public static void requireDocument(Object document) {
        if (document == null) {
            throw new SearchException("Search document must not be null.");
        }
    }

    /**
     * 归一化页码。
     *
     * @param pageNo 请求页码
     * @return 最终页码
     */
    public static int normalizePageNo(Integer pageNo) {
        return pageNo == null || pageNo < 1 ? 1 : pageNo;
    }

    /**
     * 归一化分页大小。
     *
     * @param pageSize 请求分页大小
     * @param properties 搜索模块配置
     * @return 最终分页大小
     */
    public static int normalizePageSize(Integer pageSize, SearchProperties properties) {
        int resolvedPageSize = pageSize == null || pageSize < 1
                ? properties.getDefaultPageSize()
                : pageSize;
        return Math.min(resolvedPageSize, properties.getMaxPageSize());
    }

    /**
     * 判断关键字是否有效。
     *
     * @param keyword 关键字
     * @return 是否有效
     */
    public static boolean hasKeyword(String keyword) {
        return StringUtils.hasText(keyword);
    }
}
