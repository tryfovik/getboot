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
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * {@link SearchSupport} 测试。
 *
 * @author qiheng
 */
class SearchSupportTest {

    /**
     * 验证索引名、文档 ID 与文档内容校验。
     */
    @Test
    void shouldValidateRequiredSearchFields() {
        assertEquals("article", SearchSupport.requireIndexName(" article "));
        assertEquals("doc-001", SearchSupport.requireDocumentId(" doc-001 "));
        SearchSupport.requireDocument(Map.of("title", "GetBoot"));

        assertThrows(SearchException.class, () -> SearchSupport.requireIndexName("  "));
        assertThrows(SearchException.class, () -> SearchSupport.requireDocumentId(null));
        assertThrows(SearchException.class, () -> SearchSupport.requireDocument(null));
    }

    /**
     * 验证页码与分页大小会按默认值和上限归一化。
     */
    @Test
    void shouldNormalizePageNoAndPageSize() {
        SearchProperties properties = new SearchProperties();
        properties.setDefaultPageSize(20);
        properties.setMaxPageSize(100);

        assertEquals(1, SearchSupport.normalizePageNo(null));
        assertEquals(1, SearchSupport.normalizePageNo(0));
        assertEquals(3, SearchSupport.normalizePageNo(3));

        assertEquals(20, SearchSupport.normalizePageSize(null, properties));
        assertEquals(20, SearchSupport.normalizePageSize(0, properties));
        assertEquals(50, SearchSupport.normalizePageSize(50, properties));
        assertEquals(100, SearchSupport.normalizePageSize(200, properties));
    }

    /**
     * 验证关键字有效性判断。
     */
    @Test
    void shouldDetectWhetherKeywordIsPresent() {
        assertTrue(SearchSupport.hasKeyword(" getboot "));
        assertFalse(SearchSupport.hasKeyword("   "));
        assertFalse(SearchSupport.hasKeyword(null));
    }
}
