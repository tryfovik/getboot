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

import com.getboot.storage.spi.StorageObjectKeyGenerator;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.Locale;
import java.util.UUID;

/**
 * 默认对象键生成器。
 *
 * @author qiheng
 */
public class DefaultStorageObjectKeyGenerator implements StorageObjectKeyGenerator {

    /**
     * 生成对象键。
     *
     * @param scene 业务场景
     * @param requestedObjectKey 显式指定的对象键
     * @param originalFilename 原始文件名
     * @return 最终对象键
     */
    @Override
    public String generateKey(String scene, String requestedObjectKey, String originalFilename) {
        if (StringUtils.hasText(requestedObjectKey)) {
            return requestedObjectKey.trim();
        }

        LocalDate today = LocalDate.now();
        String normalizedScene = normalizeSegment(scene);
        String extension = extractExtension(originalFilename);
        return normalizedScene
                + "/"
                + today.getYear()
                + "/"
                + String.format(Locale.ROOT, "%02d", today.getMonthValue())
                + "/"
                + String.format(Locale.ROOT, "%02d", today.getDayOfMonth())
                + "/"
                + UUID.randomUUID().toString().replace("-", "")
                + extension;
    }

    /**
     * 规整场景片段。
     *
     * @param scene 业务场景
     * @return 规整后的场景片段
     */
    private String normalizeSegment(String scene) {
        if (!StringUtils.hasText(scene)) {
            return "default";
        }
        String normalized = scene.trim().replace("\\", "/");
        normalized = normalized.replaceAll("/+", "/");
        normalized = normalized.replaceAll("(^/+|/+$)", "");
        normalized = normalized.replaceAll("[^a-zA-Z0-9/_-]", "-");
        return StringUtils.hasText(normalized) ? normalized : "default";
    }

    /**
     * 提取文件扩展名。
     *
     * @param originalFilename 原始文件名
     * @return 规整后的扩展名
     */
    private String extractExtension(String originalFilename) {
        if (!StringUtils.hasText(originalFilename)) {
            return "";
        }
        String filename = originalFilename.trim();
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex < 0 || lastDotIndex == filename.length() - 1) {
            return "";
        }
        String extension = filename.substring(lastDotIndex).toLowerCase(Locale.ROOT);
        return extension.matches("\\.[a-z0-9]{1,16}") ? extension : "";
    }
}
