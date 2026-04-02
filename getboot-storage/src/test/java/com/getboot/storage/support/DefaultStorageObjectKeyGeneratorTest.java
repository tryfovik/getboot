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
package com.getboot.storage.support;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 默认对象键生成器测试。
 *
 * @author qiheng
 */
class DefaultStorageObjectKeyGeneratorTest {

    /**
     * 默认对象键生成器。
     */
    private final DefaultStorageObjectKeyGenerator generator = new DefaultStorageObjectKeyGenerator();

    /**
     * 验证显式对象键优先返回。
     */
    @Test
    void shouldKeepExplicitObjectKey() {
        String key = generator.generateKey("invoice", "  invoice/custom-file.pdf  ", "ignored.pdf");

        assertEquals("invoice/custom-file.pdf", key);
    }

    /**
     * 验证按场景和日期生成对象键并保留扩展名。
     */
    @Test
    void shouldGenerateSceneDateBasedKeyAndPreserveExtension() {
        LocalDate today = LocalDate.now();

        String key = generator.generateKey("invoice\\archive", null, "Receipt.PDF");

        String prefix = "invoice/archive/"
                + today.getYear()
                + "/"
                + String.format("%02d", today.getMonthValue())
                + "/"
                + String.format("%02d", today.getDayOfMonth())
                + "/";
        assertTrue(key.startsWith(prefix));
        assertTrue(key.endsWith(".pdf"));

        String randomSegment = key.substring(prefix.length(), key.length() - ".pdf".length());
        assertTrue(randomSegment.matches("[a-f0-9]{32}"));
    }
}
