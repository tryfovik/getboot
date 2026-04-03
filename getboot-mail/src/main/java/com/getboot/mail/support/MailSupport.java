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
package com.getboot.mail.support;

import com.getboot.mail.api.exception.MailException;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 邮件模块辅助工具。
 *
 * @author qiheng
 */
public final class MailSupport {

    /**
     * 工具类不允许实例化。
     */
    private MailSupport() {
    }

    /**
     * 校验文本非空。
     *
     * @param value 文本值
     * @param fieldName 字段名
     * @return 规整后的文本值
     */
    public static String requireText(String value, String fieldName) {
        if (!StringUtils.hasText(value)) {
            throw new MailException(fieldName + " must not be blank.");
        }
        return value.trim();
    }

    /**
     * 判断文本是否有效。
     *
     * @param value 文本值
     * @return 是否有效
     */
    public static boolean hasText(String value) {
        return StringUtils.hasText(value);
    }

    /**
     * 判断内容类型是否为 HTML 正文。
     *
     * @param contentType 内容类型
     * @return 是否为 HTML 正文
     */
    public static boolean isHtmlContentType(String contentType) {
        return hasText(contentType) && contentType.toLowerCase().contains("text/html");
    }

    /**
     * 校验地址列表非空。
     *
     * @param addresses 地址列表
     * @param fieldName 字段名
     * @return 规整后的地址列表
     */
    public static List<String> requireAddresses(List<String> addresses, String fieldName) {
        if (CollectionUtils.isEmpty(addresses)) {
            throw new MailException(fieldName + " must not be empty.");
        }
        List<String> normalizedAddresses = new ArrayList<>(addresses.size());
        for (String address : addresses) {
            normalizedAddresses.add(requireText(address, fieldName + " item"));
        }
        return normalizedAddresses;
    }

    /**
     * 归一化可选地址列表。
     *
     * @param addresses 地址列表
     * @return 规整后的地址列表
     */
    public static List<String> normalizeAddresses(List<String> addresses) {
        if (CollectionUtils.isEmpty(addresses)) {
            return new ArrayList<>();
        }
        List<String> normalizedAddresses = new ArrayList<>(addresses.size());
        for (String address : addresses) {
            if (hasText(address)) {
                normalizedAddresses.add(address.trim());
            }
        }
        return normalizedAddresses;
    }
}
