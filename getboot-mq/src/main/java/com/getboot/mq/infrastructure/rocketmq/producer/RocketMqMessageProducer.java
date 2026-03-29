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
package com.getboot.mq.infrastructure.rocketmq.producer;

import com.getboot.mq.api.message.MqMessage;
import com.getboot.mq.api.model.MqSendReceipt;
import com.getboot.mq.api.model.MqTransactionReceipt;
import com.getboot.mq.api.producer.MqMessageProducer;
import com.getboot.mq.api.properties.MqTraceProperties;
import com.getboot.mq.infrastructure.rocketmq.support.RocketMqTraceContextSupport;
import com.getboot.mq.spi.MqMessageHeadersCustomizer;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.TransactionSendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.apache.rocketmq.spring.support.RocketMQHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.util.StopWatch;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * RocketMQ 消息生产者实现。
 *
 * @author qiheng
 */
@Slf4j
@Getter
public class RocketMqMessageProducer implements MqMessageProducer {

    private final RocketMQTemplate template;
    private final RocketMqTraceContextSupport traceContextSupport;
    private final List<MqMessageHeadersCustomizer> messageHeadersCustomizers;

    public RocketMqMessageProducer(RocketMQTemplate template) {
        this(template, new MqTraceProperties(), List.of());
    }

    public RocketMqMessageProducer(RocketMQTemplate template,
                                   MqTraceProperties traceProperties,
                                   List<MqMessageHeadersCustomizer> messageHeadersCustomizers) {
        this.template = template;
        this.traceContextSupport = new RocketMqTraceContextSupport(traceProperties);
        this.messageHeadersCustomizers = messageHeadersCustomizers == null ? List.of() : List.copyOf(messageHeadersCustomizers);
    }

    @Override
    public <T extends MqMessage> MqSendReceipt send(String topic, String tag, T message) {
        return send(buildDestination(topic, tag), message);
    }

    @Override
    public <T extends MqMessage> MqSendReceipt send(String destination, T message) {
        StopWatch watch = new StopWatch();
        try {
            watch.start();
            SendResult result = template.syncSend(destination, buildMessage(destination, message));
            log.info("[{}] Message sent successfully. messageId={}", destination, result.getMsgId());
            return toSendReceipt(destination, result);
        } catch (Exception e) {
            log.error("[{}] Failed to send message.", destination, e);
            throw new RocketMqSendException("Failed to send message.", e);
        } finally {
            if (log.isDebugEnabled()) {
                log.debug("[{}] Send cost={}ms", destination, watch.getTotalTimeMillis());
            }
        }
    }

    @Override
    public <T extends MqMessage> CompletableFuture<MqSendReceipt> asyncSend(String topic, String tag, T message) {
        return asyncSend(buildDestination(topic, tag), message);
    }

    @Override
    public <T extends MqMessage> CompletableFuture<MqSendReceipt> asyncSend(String destination, T message) {
        CompletableFuture<MqSendReceipt> future = new CompletableFuture<>();
        template.asyncSend(destination, buildMessage(destination, message), new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                future.complete(toSendReceipt(destination, sendResult));
                log.debug("[{}] Asynchronous send succeeded.", destination);
            }

            @Override
            public void onException(Throwable throwable) {
                future.completeExceptionally(throwable);
                log.error("[{}] Asynchronous send failed.", destination, throwable);
            }
        });
        return future;
    }

    @Override
    public <T extends MqMessage> MqSendReceipt sendWithDelay(String topic, String tag, T message, int delayLevel) {
        validateDelayLevel(delayLevel);
        return sendWithDelay(buildDestination(topic, tag), message, delayLevel);
    }

    @Override
    public <T extends MqMessage> MqSendReceipt sendWithDelay(String destination, T message, int delayLevel) {
        try {
            SendResult result = template.syncSend(destination, buildMessage(destination, message), 3000, delayLevel);
            log.info("[{}] Delayed message sent successfully. delayLevel={}, messageId={}",
                    destination, delayLevel, result.getMsgId());
            return toSendReceipt(destination, result);
        } catch (Exception e) {
            log.error("[{}] Failed to send delayed message.", destination, e);
            throw new RocketMqSendException("Failed to send delayed message.", e);
        }
    }

    @Override
    public <T extends MqMessage> MqSendReceipt sendBatch(String topic, String tag, List<T> messages) {
        String destination = buildDestination(topic, tag);
        List<Message<T>> messageList = messages.stream()
                .map(message -> buildMessage(destination, message))
                .toList();
        try {
            SendResult result = template.syncSend(destination, messageList);
            log.info("[{}] Batch send succeeded. size={}", destination, messages.size());
            return toSendReceipt(destination, result);
        } catch (Exception e) {
            log.error("[{}] Batch send failed. size={}", destination, messages.size(), e);
            throw new RocketMqSendException("Failed to send batch messages.", e);
        }
    }

    @Override
    public <T extends MqMessage> MqSendReceipt sendOrderly(String topic, String tag, T message, String hashKey) {
        return sendOrderly(buildDestination(topic, tag), message, hashKey);
    }

    @Override
    public <T extends MqMessage> MqSendReceipt sendOrderly(String destination, T message, String hashKey) {
        try {
            SendResult result = template.syncSendOrderly(destination, buildMessage(destination, message), hashKey);
            log.info("[{}] Ordered message sent successfully. messageId={}", destination, result.getMsgId());
            return toSendReceipt(destination, result);
        } catch (Exception e) {
            log.error("[{}] Failed to send ordered message.", destination, e);
            throw new RocketMqSendException("Failed to send ordered message.", e);
        }
    }

    @Override
    public <T extends MqMessage> MqTransactionReceipt sendTransaction(String destination, T message, Object arg) {
        try {
            TransactionSendResult result = template.sendMessageInTransaction(destination, buildMessage(destination, message), arg);
            log.info("[{}] Transactional message submitted. transactionId={}", destination, result.getTransactionId());
            return new MqTransactionReceipt(destination, result.getMsgId(), result.getTransactionId());
        } catch (Exception e) {
            log.error("[{}] Failed to send transactional message.", destination, e);
            throw new RocketMqSendException("Failed to send transactional message.", e);
        }
    }

    @Override
    public <T extends MqMessage> MqTransactionReceipt sendTransaction(String topic, String tag, T message, Object arg) {
        return sendTransaction(buildDestination(topic, tag), message, arg);
    }

    private String buildDestination(String topic, String tag) {
        if (!StringUtils.hasText(tag)) {
            return topic;
        }
        return topic + ":" + tag;
    }

    private <T extends MqMessage> Message<T> buildMessage(String destination, T message) {
        Map<String, Object> headers = new LinkedHashMap<>();
        headers.put(RocketMQHeaders.KEYS, message.getBizKey());
        if (traceContextSupport.isEnabled()) {
            headers.put(traceContextSupport.getTraceHeaderName(), generateTraceId(message));
        }
        String topic = destination;
        int separatorIndex = destination.indexOf(':');
        if (separatorIndex >= 0) {
            topic = destination.substring(0, separatorIndex);
        }
        if (StringUtils.hasText(topic)) {
            headers.put(RocketMQHeaders.TOPIC, topic);
        }
        messageHeadersCustomizers.forEach(customizer -> customizer.customize(headers, message, destination));
        MessageBuilder<T> builder = MessageBuilder.withPayload(message);
        headers.forEach(builder::setHeader);
        return builder.build();
    }

    private String generateTraceId(MqMessage message) {
        String traceId = traceContextSupport.resolveOutboundTraceId(message);
        if (!StringUtils.hasText(traceId)) {
            traceId = UUID.randomUUID().toString();
        }
        message.setTraceId(traceId);
        return traceId;
    }

    private void validateDelayLevel(int delayLevel) {
        if (delayLevel < 0 || delayLevel > 18) {
            throw new IllegalArgumentException("Invalid delay level: " + delayLevel);
        }
    }

    private MqSendReceipt toSendReceipt(String destination, SendResult sendResult) {
        return new MqSendReceipt(destination, sendResult.getMsgId());
    }

    public static class RocketMqSendException extends RuntimeException {
        public RocketMqSendException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
