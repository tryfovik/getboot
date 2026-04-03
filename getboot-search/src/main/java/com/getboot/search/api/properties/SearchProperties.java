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
package com.getboot.search.api.properties;

import com.getboot.search.api.constant.SearchConstants;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 搜索模块配置。
 *
 * @author qiheng
 */
@ConfigurationProperties(prefix = "getboot.search")
@Data
public class SearchProperties {

    /**
     * 是否启用搜索能力。
     */
    private boolean enabled = true;

    /**
     * 当前搜索实现类型。
     */
    private String type = SearchConstants.SEARCH_TYPE_ELASTICSEARCH;

    /**
     * 默认索引名前缀。
     */
    private String defaultIndexPrefix;

    /**
     * 默认分页大小。
     */
    private int defaultPageSize = SearchConstants.DEFAULT_PAGE_SIZE;

    /**
     * 最大分页大小。
     */
    private int maxPageSize = SearchConstants.DEFAULT_MAX_PAGE_SIZE;

    /**
     * 逻辑索引名映射。
     */
    private Map<String, String> indexAliases = new LinkedHashMap<>();

    /**
     * Elasticsearch 配置。
     */
    private Elasticsearch elasticsearch = new Elasticsearch();

    /**
     * 设置逻辑索引名映射。
     *
     * @param indexAliases 逻辑索引名映射
     */
    public void setIndexAliases(Map<String, String> indexAliases) {
        this.indexAliases = indexAliases == null ? new LinkedHashMap<>() : new LinkedHashMap<>(indexAliases);
    }

    /**
     * Elasticsearch 配置。
     *
     * @author qiheng
     */
    @Data
    public static class Elasticsearch {

        /**
         * 是否启用 Elasticsearch 实现。
         */
        private boolean enabled = true;

        /**
         * Elasticsearch 节点地址列表。
         */
        private List<String> uris = new ArrayList<>(List.of("http://127.0.0.1:9200"));

        /**
         * 用户名。
         */
        private String username;

        /**
         * 密码。
         */
        private String password;

        /**
         * API Key。
         */
        private String apiKey;

        /**
         * 公共路径前缀。
         */
        private String pathPrefix;

        /**
         * 连接超时时间。
         */
        private Duration connectTimeout = SearchConstants.DEFAULT_CONNECT_TIMEOUT;

        /**
         * 响应读取超时时间。
         */
        private Duration socketTimeout = SearchConstants.DEFAULT_SOCKET_TIMEOUT;

        /**
         * 默认请求头。
         */
        private Map<String, String> defaultHeaders = new LinkedHashMap<>();

        /**
         * 设置 Elasticsearch 节点地址列表。
         *
         * @param uris 节点地址列表
         */
        public void setUris(List<String> uris) {
            this.uris = uris == null ? new ArrayList<>() : new ArrayList<>(uris);
        }

        /**
         * 设置默认请求头。
         *
         * @param defaultHeaders 默认请求头
         */
        public void setDefaultHeaders(Map<String, String> defaultHeaders) {
            this.defaultHeaders = defaultHeaders == null ? new LinkedHashMap<>() : new LinkedHashMap<>(defaultHeaders);
        }
    }
}
