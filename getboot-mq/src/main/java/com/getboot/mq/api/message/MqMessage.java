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
package com.getboot.mq.api.message;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * MQ 通用消息基类。
 *
 * <p>定义跨 MQ 技术栈可复用的通用消息字段。</p>
 *
 * @author qiheng
 */
@Getter
@Setter
@Accessors(chain = true)
public abstract class MqMessage implements Serializable {

    @Serial
    private static final long serialVersionUID = 2023081801L;

    private String bizKey;

    private String version = "1.0";

    private String messageId = UUID.randomUUID().toString().replace("-", "");

    private String traceId;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private LocalDateTime sendTime;

    private int retryCount;

    private int maxRetries = 3;

    private String sourceSystem;

    /**
     * 获取消息类型。
     *
     * @return 消息类型
     */
    public abstract String getMessageType();

    /**
     * 快速创建消息对象。
     *
     * @param clazz 消息类型
     * @param <T> 消息泛型
     * @return 消息实例
     */
    public static <T extends MqMessage> T create(Class<T> clazz) {
        try {
            T message = clazz.getDeclaredConstructor().newInstance();
            message.setSendTime(LocalDateTime.now());
            message.setTraceId(UUID.randomUUID().toString());
            return message;
        } catch (Exception e) {
            throw new MessageCreateException("Failed to instantiate MQ message.", e);
        }
    }

    public String toSimpleString() {
        return String.format("Message{id=%s, type=%s, key=%s}",
                messageId, getMessageType(), bizKey);
    }

    public static class MessageCreateException extends RuntimeException {
        public MessageCreateException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
