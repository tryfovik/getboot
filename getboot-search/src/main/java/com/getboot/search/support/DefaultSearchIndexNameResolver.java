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

import com.getboot.search.api.properties.SearchProperties;
import com.getboot.search.spi.SearchIndexNameResolver;
import org.springframework.util.StringUtils;

/**
 * 默认索引名解析器。
 *
 * @author qiheng
 */
public class DefaultSearchIndexNameResolver implements SearchIndexNameResolver {

    /**
     * 搜索模块配置。
     */
    private final SearchProperties properties;

    /**
     * 构造默认索引名解析器。
     *
     * @param properties 搜索模块配置
     */
    public DefaultSearchIndexNameResolver(SearchProperties properties) {
        this.properties = properties;
    }

    /**
     * 解析最终索引名。
     *
     * @param requestedIndexName 请求中的逻辑索引名或物理索引名
     * @return 最终索引名
     */
    @Override
    public String resolveIndexName(String requestedIndexName) {
        String logicalName = SearchSupport.requireIndexName(requestedIndexName);
        String configuredName = properties.getIndexAliases().getOrDefault(logicalName, logicalName);
        String normalizedName = configuredName.trim().toLowerCase().replace('_', '-');
        if (!StringUtils.hasText(properties.getDefaultIndexPrefix())) {
            return normalizedName;
        }
        String prefix = properties.getDefaultIndexPrefix().trim().toLowerCase().replace('_', '-');
        String expectedPrefix = prefix + "-";
        return normalizedName.startsWith(expectedPrefix) ? normalizedName : expectedPrefix + normalizedName;
    }
}
