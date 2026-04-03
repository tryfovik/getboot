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
import com.getboot.mail.spi.MailTemplateRenderer;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 默认邮件模板渲染器。
 *
 * @author qiheng
 */
public class DefaultMailTemplateRenderer implements MailTemplateRenderer {

    /**
     * 模板占位符模式。
     */
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{\\{\\s*([a-zA-Z0-9_.-]+)\\s*}}");

    /**
     * 渲染邮件模板。
     *
     * @param template 模板内容
     * @param variables 模板变量
     * @return 渲染后的内容
     */
    @Override
    public String render(String template, Map<String, Object> variables) {
        String content = MailSupport.requireText(template, "Mail template");
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(content);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            String variableName = matcher.group(1);
            Object variableValue = variables == null ? null : variables.get(variableName);
            if (variableValue == null) {
                throw new MailException("Mail template variable '" + variableName + "' is missing.");
            }
            matcher.appendReplacement(buffer, Matcher.quoteReplacement(String.valueOf(variableValue)));
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }
}
