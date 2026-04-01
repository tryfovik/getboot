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
package com.getboot.mq.infrastructure.kafka.producer;

import com.getboot.mq.api.message.MqMessage;
import com.getboot.mq.api.model.MqSendReceipt;
import com.getboot.mq.api.model.MqTransactionReceipt;
import com.getboot.mq.api.producer.MqMessageProducer;
import com.getboot.mq.api.properties.MqTraceProperties;
import com.getboot.mq.spi.MqMessageHeadersCustomizer;
import com.getboot.mq.support.MqDestination;
import com.getboot.mq.support.MqTraceContextSupport;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.util.StopWatch;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/**
 * Kafka 消息生产者实现。
 *
 * <p>当前承接 MQ 能力层中真正可跨技术栈复用的发送能力。</p>
 *
 * @author qiheng
 */
public class KafkaMqMessageProducer implements MqMessageProducer {

    public static final String TAG_HEADER = "GETBOOT_MQ_TAG";
    private static final Logger log = LoggerFactory.getLogger(KafkaMqMessageProducer.class);

    private final KafkaTemplate<Object, Object> template;
    private final MqTraceContextSupport traceContextSupport;
    private final List<MqMessageHeadersCustomizer> messageHeadersCustomizers;

    public KafkaMqMessageProducer(KafkaTemplate<Object, Object> template) {
        this(template, new MqTraceProperties(), List.of());
    }

    public KafkaMqMessageProducer(KafkaTemplate<Object, Object> template,
                                  MqTraceProperties traceProperties,
                                  List<MqMessageHeadersCustomizer> messageHeadersCustomizers) {
        this.template = template;
        this.traceContextSupport = new MqTraceContextSupport(traceProperties);
        this.messageHeadersCustomizers = messageHeadersCustomizers == null ? List.of() : List.copyOf(messageHeadersCustomizers);
    }

    @Override
    public <T extends MqMessage> MqSendReceipt send(String topic, String tag, T message) {
        return send(MqDestination.of(topic, tag).destination(), message);
    }

    @Override
    public <T extends MqMessage> MqSendReceipt send(String destination, T message) {
        MqDestination mqDestination = MqDestination.parse(destination);
        StopWatch watch = new StopWatch();
        try {
            watch.start();
            template.send(buildMessage(mqDestination, message, null)).join();
            log.info("[{}] Kafka message sent successfully. messageId={}", destination, message.getMessageId());
            return toSendReceipt(mqDestination, message);
        } catch (CompletionException ex) {
            throw logAndWrapFailure(destination, "Failed to send Kafka message.", ex);
        } catch (Exception ex) {
            throw logAndWrapFailure(destination, "Failed to send Kafka message.", ex);
        } finally {
            if (log.isDebugEnabled()) {
                log.debug("[{}] Kafka send cost={}ms", destination, watch.getTotalTimeMillis());
            }
        }
    }

    @Override
    public <T extends MqMessage> CompletableFuture<MqSendReceipt> asyncSend(String topic, String tag, T message) {
        return asyncSend(MqDestination.of(topic, tag).destination(), message);
    }

    @Override
    public <T extends MqMessage> CompletableFuture<MqSendReceipt> asyncSend(String destination, T message) {
        MqDestination mqDestination = MqDestination.parse(destination);
        return template.send(buildMessage(mqDestination, message, null))
                .thenApply(ignored -> {
                    log.debug("[{}] Kafka asynchronous send succeeded.", destination);
                    return toSendReceipt(mqDestination, message);
                })
                .whenComplete((receipt, throwable) -> {
                    if (throwable != null) {
                        log.error("[{}] Kafka asynchronous send failed.", destination, throwable);
                    }
                });
    }

    @Override
    public <T extends MqMessage> MqSendReceipt sendWithDelay(String topic, String tag, T message, int delayLevel) {
        throw unsupported("Kafka producer does not support RocketMQ delayLevel based delayed send.");
    }

    @Override
    public <T extends MqMessage> MqSendReceipt sendWithDelay(String destination, T message, int delayLevel) {
        throw unsupported("Kafka producer does not support RocketMQ delayLevel based delayed send.");
    }

    @Override
    public <T extends MqMessage> MqSendReceipt sendBatch(String topic, String tag, List<T> messages) {
        if (messages == null || messages.isEmpty()) {
            throw new IllegalArgumentException("Messages must not be empty.");
        }
        MqDestination destination = MqDestination.of(topic, tag);
        MqSendReceipt lastReceipt = null;
        for (T message : messages) {
            lastReceipt = send(destination.destination(), message);
        }
        log.info("[{}] Kafka batch send finished. size={}", destination.destination(), messages.size());
        return lastReceipt;
    }

    @Override
    public <T extends MqMessage> MqSendReceipt sendOrderly(String topic, String tag, T message, String hashKey) {
        return sendOrderly(MqDestination.of(topic, tag).destination(), message, hashKey);
    }

    @Override
    public <T extends MqMessage> MqSendReceipt sendOrderly(String destination, T message, String hashKey) {
        MqDestination mqDestination = MqDestination.parse(destination);
        try {
            template.send(buildMessage(mqDestination, message, hashKey)).join();
            log.info("[{}] Kafka ordered message sent successfully. messageId={}, hashKey={}",
                    destination, message.getMessageId(), hashKey);
            return toSendReceipt(mqDestination, message);
        } catch (CompletionException ex) {
            throw logAndWrapFailure(destination, "Failed to send Kafka ordered message.", ex);
        } catch (Exception ex) {
            throw logAndWrapFailure(destination, "Failed to send Kafka ordered message.", ex);
        }
    }

    @Override
    public <T extends MqMessage> MqTransactionReceipt sendTransaction(String destination, T message, Object arg) {
        throw unsupported("Kafka producer does not support RocketMQ transactional message semantics.");
    }

    @Override
    public <T extends MqMessage> MqTransactionReceipt sendTransaction(String topic, String tag, T message, Object arg) {
        throw unsupported("Kafka producer does not support RocketMQ transactional message semantics.");
    }

    private <T extends MqMessage> Message<T> buildMessage(MqDestination destination, T message, String hashKey) {
        Map<String, Object> headers = new LinkedHashMap<>();
        headers.put(KafkaHeaders.TOPIC, destination.topic());
        if (StringUtils.hasText(destination.tag())) {
            headers.put(TAG_HEADER, destination.tag());
        }
        String messageKey = StringUtils.hasText(hashKey) ? hashKey.trim() : message.getBizKey();
        if (StringUtils.hasText(messageKey)) {
            headers.put(KafkaHeaders.KEY, messageKey);
        }
        if (traceContextSupport.isEnabled()) {
            headers.put(traceContextSupport.getTraceHeaderName(), generateTraceId(message));
        }
        messageHeadersCustomizers.forEach(customizer -> customizer.customize(headers, message, destination.destination()));
        MessageBuilder<T> builder = MessageBuilder.withPayload(message);
        headers.forEach((key, value) -> {
            if (value != null) {
                builder.setHeader(key, value);
            }
        });
        return builder.build();
    }

    private String generateTraceId(MqMessage message) {
        String traceId = traceContextSupport.resolveOutboundTraceId(message);
        if (!StringUtils.hasText(traceId)) {
            traceId = message.getTraceId();
        }
        message.setTraceId(traceId);
        return traceId;
    }

    private MqSendReceipt toSendReceipt(MqDestination destination, MqMessage message) {
        return new MqSendReceipt(destination.destination(), message.getMessageId());
    }

    private RuntimeException logAndWrapFailure(String destination, String message, Throwable throwable) {
        Throwable cause = throwable instanceof CompletionException && throwable.getCause() != null
                ? throwable.getCause()
                : throwable;
        log.error("[{}] {}", destination, message, cause);
        return new KafkaMqSendException(message, cause);
    }

    private UnsupportedOperationException unsupported(String message) {
        return new UnsupportedOperationException(message);
    }

    public static class KafkaMqSendException extends RuntimeException {
        public KafkaMqSendException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public KafkaTemplate<Object, Object> getTemplate() {
        return template;
    }

    public MqTraceContextSupport getTraceContextSupport() {
        return traceContextSupport;
    }

    public List<MqMessageHeadersCustomizer> getMessageHeadersCustomizers() {
        return messageHeadersCustomizers;
    }
}
