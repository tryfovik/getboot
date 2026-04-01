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
package com.getboot.mq.infrastructure.kafka.aop;

import com.getboot.mq.api.properties.MqTraceProperties;
import com.getboot.support.api.trace.TraceContextHolder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class KafkaMqTraceListenerAspectTest {

    @AfterEach
    void tearDown() {
        TraceContextHolder.clear();
        MDC.clear();
    }

    @Test
    void shouldRestoreTraceContextForKafkaListenerMethod() {
        KafkaMqTraceListenerAspect aspect = new KafkaMqTraceListenerAspect(new MqTraceProperties());
        ListenerService target = new ListenerService();

        AspectJProxyFactory proxyFactory = new AspectJProxyFactory(target);
        proxyFactory.addAspect(aspect);
        ListenerService proxy = proxyFactory.getProxy();

        Message<String> inboundMessage = MessageBuilder.withPayload("payload")
                .setHeader("TRACE_ID", "trace-kafka-1")
                .build();

        assertEquals("trace-kafka-1", proxy.handle(inboundMessage));
        assertEquals("trace-kafka-1", target.traceIdInListener);
        assertEquals("trace-kafka-1", target.traceIdInMdc);
        assertNull(TraceContextHolder.getTraceId());
        assertNull(MDC.get("traceId"));
    }

    static class ListenerService {

        private String traceIdInListener;
        private String traceIdInMdc;

        @KafkaListener(topics = "orders")
        public String handle(Message<String> message) {
            traceIdInListener = TraceContextHolder.getTraceId();
            traceIdInMdc = MDC.get("traceId");
            return traceIdInListener;
        }
    }
}
