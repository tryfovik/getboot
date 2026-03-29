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
package com.getboot.mq.api.producer;

import com.getboot.mq.api.message.MqMessage;
import com.getboot.mq.api.model.MqSendReceipt;
import com.getboot.mq.api.model.MqTransactionReceipt;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * MQ 消息生产能力接口。
 *
 * <p>对外提供稳定的消息发送入口，具体技术栈由基础设施层实现。</p>
 *
 * @author qiheng
 */
public interface MqMessageProducer {

    <T extends MqMessage> MqSendReceipt send(String topic, String tag, T message);

    <T extends MqMessage> MqSendReceipt send(String destination, T message);

    <T extends MqMessage> CompletableFuture<MqSendReceipt> asyncSend(String topic, String tag, T message);

    <T extends MqMessage> CompletableFuture<MqSendReceipt> asyncSend(String destination, T message);

    <T extends MqMessage> MqSendReceipt sendWithDelay(String topic, String tag, T message, int delayLevel);

    <T extends MqMessage> MqSendReceipt sendWithDelay(String destination, T message, int delayLevel);

    <T extends MqMessage> MqSendReceipt sendBatch(String topic, String tag, List<T> messages);

    <T extends MqMessage> MqSendReceipt sendOrderly(String topic, String tag, T message, String hashKey);

    <T extends MqMessage> MqSendReceipt sendOrderly(String destination, T message, String hashKey);

    <T extends MqMessage> MqTransactionReceipt sendTransaction(String destination, T message, Object arg);

    <T extends MqMessage> MqTransactionReceipt sendTransaction(String topic, String tag, T message, Object arg);
}
