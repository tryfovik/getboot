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

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 搜索配置绑定测试。
 *
 * @author qiheng
 */
class SearchPropertiesBindingTest {

    /**
     * 验证 kebab-case 配置能够绑定到搜索属性。
     */
    @Test
    void shouldBindSearchPropertiesFromKebabCaseConfiguration() {
        Map<String, String> source = new LinkedHashMap<>();
        source.put("getboot.search.enabled", "false");
        source.put("getboot.search.type", "elasticsearch");
        source.put("getboot.search.default-index-prefix", "prod");
        source.put("getboot.search.default-page-size", "30");
        source.put("getboot.search.max-page-size", "200");
        source.put("getboot.search.index-aliases.article", "article_v2");
        source.put("getboot.search.elasticsearch.enabled", "true");
        source.put("getboot.search.elasticsearch.uris[0]", "http://127.0.0.1:9200");
        source.put("getboot.search.elasticsearch.path-prefix", "/es");
        source.put("getboot.search.elasticsearch.api-key", "api-key-001");
        source.put("getboot.search.elasticsearch.connect-timeout", "3s");
        source.put("getboot.search.elasticsearch.socket-timeout", "8s");
        source.put("getboot.search.elasticsearch.default-headers.X-Request-Source", "getboot");

        SearchProperties properties = new Binder(new MapConfigurationPropertySource(source))
                .bind("getboot.search", Bindable.of(SearchProperties.class))
                .orElseThrow(() -> new IllegalStateException("search properties should bind"));

        assertFalse(properties.isEnabled());
        assertEquals("elasticsearch", properties.getType());
        assertEquals("prod", properties.getDefaultIndexPrefix());
        assertEquals(30, properties.getDefaultPageSize());
        assertEquals(200, properties.getMaxPageSize());
        assertEquals("article_v2", properties.getIndexAliases().get("article"));

        assertTrue(properties.getElasticsearch().isEnabled());
        assertEquals("http://127.0.0.1:9200", properties.getElasticsearch().getUris().get(0));
        assertEquals("/es", properties.getElasticsearch().getPathPrefix());
        assertEquals("api-key-001", properties.getElasticsearch().getApiKey());
        assertEquals(Duration.ofSeconds(3), properties.getElasticsearch().getConnectTimeout());
        assertEquals(Duration.ofSeconds(8), properties.getElasticsearch().getSocketTimeout());
        assertEquals("getboot", properties.getElasticsearch().getDefaultHeaders().get("X-Request-Source"));
    }
}
