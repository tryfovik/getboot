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
package com.getboot.mq.support;

import org.springframework.util.StringUtils;

/**
 * MQ 逻辑目标地址。
 *
 * <p>统一承接能力层使用的 {@code topic[:tag]} 语义，具体实现再映射到底层技术栈。</p>
 *
 * @author qiheng
 */
public record MqDestination(String destination, String topic, String tag) {

    public static MqDestination of(String topic, String tag) {
        if (!StringUtils.hasText(topic)) {
            throw new IllegalArgumentException("Topic must not be blank.");
        }
        String normalizedTopic = topic.trim();
        if (!StringUtils.hasText(tag)) {
            return new MqDestination(normalizedTopic, normalizedTopic, null);
        }
        String normalizedTag = tag.trim();
        return new MqDestination(normalizedTopic + ":" + normalizedTag, normalizedTopic, normalizedTag);
    }

    public static MqDestination parse(String destination) {
        if (!StringUtils.hasText(destination)) {
            throw new IllegalArgumentException("Destination must not be blank.");
        }
        String normalizedDestination = destination.trim();
        int separatorIndex = normalizedDestination.indexOf(':');
        if (separatorIndex < 0) {
            return new MqDestination(normalizedDestination, normalizedDestination, null);
        }
        String topic = normalizedDestination.substring(0, separatorIndex).trim();
        String tag = normalizedDestination.substring(separatorIndex + 1).trim();
        if (!StringUtils.hasText(topic)) {
            throw new IllegalArgumentException("Destination topic must not be blank.");
        }
        if (!StringUtils.hasText(tag)) {
            return new MqDestination(topic, topic, null);
        }
        return new MqDestination(normalizedDestination, topic, tag);
    }
}
