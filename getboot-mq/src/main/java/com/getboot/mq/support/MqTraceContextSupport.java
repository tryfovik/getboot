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

import com.getboot.mq.api.message.MqMessage;
import com.getboot.mq.api.properties.MqTraceProperties;
import com.getboot.support.api.trace.TraceContextHolder;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;
import org.apache.rocketmq.common.message.MessageExt;
import org.slf4j.MDC;
import org.springframework.messaging.Message;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;

/**
 * MQ Trace 上下文支撑工具。
 *
 * @author qiheng
 */
public class MqTraceContextSupport {

    private static final String DEFAULT_TRACE_MDC_KEY = "traceId";

    private final MqTraceProperties traceProperties;

    public MqTraceContextSupport(MqTraceProperties traceProperties) {
        this.traceProperties = traceProperties;
    }

    public boolean isEnabled() {
        return traceProperties != null && traceProperties.isEnabled();
    }

    public String getTraceHeaderName() {
        if (traceProperties != null && StringUtils.hasText(traceProperties.getHeaderName())) {
            return traceProperties.getHeaderName().trim();
        }
        return "TRACE_ID";
    }

    public String getTraceMdcKey() {
        if (traceProperties != null && StringUtils.hasText(traceProperties.getMdcKey())) {
            return traceProperties.getMdcKey().trim();
        }
        return DEFAULT_TRACE_MDC_KEY;
    }

    public String resolveOutboundTraceId(MqMessage message) {
        String currentTraceId = TraceContextHolder.getTraceId();
        if (StringUtils.hasText(currentTraceId)) {
            return currentTraceId.trim();
        }
        if (message != null && StringUtils.hasText(message.getTraceId())) {
            return message.getTraceId().trim();
        }
        return null;
    }

    public String resolveInboundTraceId(Object source) {
        if (!isEnabled() || source == null) {
            return null;
        }
        if (source instanceof Message<?> springMessage) {
            String traceId = normalizeTraceValue(springMessage.getHeaders().get(getTraceHeaderName()));
            if (StringUtils.hasText(traceId)) {
                return traceId;
            }
            return resolveInboundTraceId(springMessage.getPayload());
        }
        if (source instanceof MqMessage mqMessage && StringUtils.hasText(mqMessage.getTraceId())) {
            return mqMessage.getTraceId().trim();
        }
        if (source instanceof MessageExt messageExt) {
            String traceId = messageExt.getProperty(getTraceHeaderName());
            if (StringUtils.hasText(traceId)) {
                return traceId.trim();
            }
            return null;
        }
        if (source instanceof ConsumerRecord<?, ?> consumerRecord) {
            String traceId = resolveInboundTraceId(consumerRecord.headers());
            if (StringUtils.hasText(traceId)) {
                return traceId;
            }
            return resolveInboundTraceId(consumerRecord.value());
        }
        if (source instanceof Headers headers) {
            Header traceHeader = headers.lastHeader(getTraceHeaderName());
            if (traceHeader == null || traceHeader.value() == null) {
                return null;
            }
            return normalizeTraceValue(traceHeader.value());
        }
        if (source instanceof Header header) {
            return normalizeTraceValue(header.value());
        }
        if (source instanceof Iterable<?> iterable) {
            for (Object item : iterable) {
                String traceId = resolveInboundTraceId(item);
                if (StringUtils.hasText(traceId)) {
                    return traceId;
                }
            }
        }
        return null;
    }

    public TraceScope openScope(String traceId) {
        if (!StringUtils.hasText(traceId)) {
            return TraceScope.noop();
        }
        String normalizedTraceId = traceId.trim();
        String previousTraceId = TraceContextHolder.bindTraceId(normalizedTraceId);
        String traceMdcKey = getTraceMdcKey();
        String previousMdcTraceId = MDC.get(traceMdcKey);
        MDC.put(traceMdcKey, normalizedTraceId);
        return new TraceScope(previousTraceId, traceMdcKey, previousMdcTraceId, true);
    }

    private String normalizeTraceValue(Object traceValue) {
        if (traceValue instanceof byte[] bytes) {
            String traceId = new String(bytes, StandardCharsets.UTF_8);
            return StringUtils.hasText(traceId) ? traceId.trim() : null;
        }
        if (traceValue instanceof String traceId && StringUtils.hasText(traceId)) {
            return traceId.trim();
        }
        if (traceValue != null) {
            String traceId = traceValue.toString();
            return StringUtils.hasText(traceId) ? traceId.trim() : null;
        }
        return null;
    }

    public record TraceScope(
            String previousTraceId,
            String traceMdcKey,
            String previousMdcTraceId,
            boolean active) implements AutoCloseable {

        public static TraceScope noop() {
            return new TraceScope(null, null, null, false);
        }

        @Override
        public void close() {
            if (!active) {
                return;
            }
            TraceContextHolder.restoreTraceId(previousTraceId);
            if (previousMdcTraceId == null) {
                MDC.remove(traceMdcKey);
                return;
            }
            MDC.put(traceMdcKey, previousMdcTraceId);
        }
    }
}
