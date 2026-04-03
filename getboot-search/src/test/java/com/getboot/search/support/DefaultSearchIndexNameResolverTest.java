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
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 默认索引名解析器测试。
 *
 * @author qiheng
 */
class DefaultSearchIndexNameResolverTest {

    /**
     * 验证逻辑索引名会应用别名和前缀。
     */
    @Test
    void shouldResolveAliasAndPrefix() {
        SearchProperties properties = new SearchProperties();
        properties.setDefaultIndexPrefix("prod");
        properties.getIndexAliases().put("article", "article_v2");

        DefaultSearchIndexNameResolver resolver = new DefaultSearchIndexNameResolver(properties);

        assertEquals("prod-article-v2", resolver.resolveIndexName("article"));
    }

    /**
     * 验证已经带前缀的索引名不会重复拼接。
     */
    @Test
    void shouldKeepExistingPrefix() {
        SearchProperties properties = new SearchProperties();
        properties.setDefaultIndexPrefix("prod");

        DefaultSearchIndexNameResolver resolver = new DefaultSearchIndexNameResolver(properties);

        assertEquals("prod-order", resolver.resolveIndexName("prod-order"));
    }
}
