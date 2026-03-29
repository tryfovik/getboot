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
package com.getboot.mq.spi;

import com.getboot.mq.api.message.MqMessage;

import java.util.Map;

/**
 * MQ 消息头定制器。
 *
 * <p>业务方可通过注册该类型 Bean，在消息发送前扩展或改写消息头。</p>
 *
 * @author qiheng
 */
@FunctionalInterface
public interface MqMessageHeadersCustomizer {

    /**
     * 定制消息头。
     *
     * @param headers 当前消息头
     * @param message 消息体
     * @param destination 目标地址
     */
    void customize(Map<String, Object> headers, MqMessage message, String destination);
}
