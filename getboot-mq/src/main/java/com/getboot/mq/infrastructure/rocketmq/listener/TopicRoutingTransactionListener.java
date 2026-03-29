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
package com.getboot.mq.infrastructure.rocketmq.listener;

import com.getboot.mq.api.properties.MqTraceProperties;
import com.getboot.mq.infrastructure.rocketmq.support.RocketMqTraceContextSupport;
import com.getboot.mq.spi.rocketmq.TopicTransactionStrategy;
import org.apache.rocketmq.spring.annotation.RocketMQTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionState;
import org.apache.rocketmq.spring.support.RocketMQHeaders;
import org.springframework.messaging.Message;

import java.util.List;

/**
 * 按 Topic 路由的事务监听器。
 *
 * @author qiheng
 */
@RocketMQTransactionListener
public class TopicRoutingTransactionListener implements RocketMQLocalTransactionListener {

    private final List<TopicTransactionStrategy> strategies;
    private final RocketMqTraceContextSupport traceContextSupport;

    public TopicRoutingTransactionListener(List<TopicTransactionStrategy> strategies,
                                           MqTraceProperties traceProperties) {
        this.strategies = strategies;
        this.traceContextSupport = new RocketMqTraceContextSupport(traceProperties);
    }

    @Override
    public RocketMQLocalTransactionState executeLocalTransaction(Message msg, Object arg) {
        return executeWithTrace(msg, () -> {
            String topic = msg.getHeaders().get(RocketMQHeaders.TOPIC, String.class);
            return strategies.stream()
                    .filter(strategy -> strategy.supports(topic))
                    .findFirst()
                    .map(strategy -> strategy.executeTransaction(msg))
                    .orElse(RocketMQLocalTransactionState.ROLLBACK);
        });
    }

    @Override
    public RocketMQLocalTransactionState checkLocalTransaction(Message msg) {
        return executeWithTrace(msg, () -> {
            String topic = msg.getHeaders().get(RocketMQHeaders.TOPIC, String.class);
            return strategies.stream()
                    .filter(strategy -> strategy.supports(topic))
                    .findFirst()
                    .map(strategy -> strategy.checkTransaction(msg))
                    .orElse(RocketMQLocalTransactionState.ROLLBACK);
        });
    }

    private RocketMQLocalTransactionState executeWithTrace(Message msg, TransactionCallback callback) {
        String traceId = traceContextSupport.resolveInboundTraceId(msg);
        try (RocketMqTraceContextSupport.TraceScope ignored = traceContextSupport.openScope(traceId)) {
            return callback.execute();
        }
    }

    @FunctionalInterface
    private interface TransactionCallback {
        RocketMQLocalTransactionState execute();
    }
}
