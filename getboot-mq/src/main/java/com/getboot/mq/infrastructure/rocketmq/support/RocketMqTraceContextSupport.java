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
package com.getboot.mq.infrastructure.rocketmq.support;

import com.getboot.mq.api.message.MqMessage;
import com.getboot.mq.api.properties.MqTraceProperties;
import com.getboot.support.api.trace.TraceContextHolder;
import org.apache.rocketmq.common.message.MessageExt;
import org.slf4j.MDC;
import org.springframework.messaging.Message;
import org.springframework.util.StringUtils;

import java.util.Collection;

/**
 * RocketMQ Trace 上下文支撑工具。
 *
 * @author qiheng
 */
public class RocketMqTraceContextSupport {

    private static final String DEFAULT_TRACE_MDC_KEY = "traceId";

    private final MqTraceProperties traceProperties;

    public RocketMqTraceContextSupport(MqTraceProperties traceProperties) {
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
            String traceId = springMessage.getHeaders().get(getTraceHeaderName(), String.class);
            if (StringUtils.hasText(traceId)) {
                return traceId.trim();
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
        if (source instanceof Collection<?> collection) {
            for (Object item : collection) {
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
