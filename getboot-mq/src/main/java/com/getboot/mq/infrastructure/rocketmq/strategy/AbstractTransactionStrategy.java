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
package com.getboot.mq.infrastructure.rocketmq.strategy;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.getboot.mq.spi.rocketmq.TopicTransactionStrategy;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionState;
import org.springframework.messaging.Message;

import java.nio.charset.StandardCharsets;

/**
 * Topic 事务策略抽象基类。
 *
 * @author qiheng
 */
public abstract class AbstractTransactionStrategy implements TopicTransactionStrategy {

    @Override
    public RocketMQLocalTransactionState executeTransaction(Object arg) {
        JSONObject jsonObject = parseMessageToJson(arg);
        return doExecuteTransaction(jsonObject);
    }

    @Override
    public RocketMQLocalTransactionState checkTransaction(Object arg) {
        JSONObject jsonObject = parseMessageToJson(arg);
        return doCheckTransaction(jsonObject);
    }

    private JSONObject parseMessageToJson(Object msg) {
        Message<?> message = (Message<?>) msg;
        Object payload = message.getPayload();
        if (payload instanceof byte[] payloadBytes) {
            return JSON.parseObject(new String(payloadBytes, StandardCharsets.UTF_8));
        }
        return JSON.parseObject(JSON.toJSONString(payload));
    }

    protected abstract RocketMQLocalTransactionState doExecuteTransaction(JSONObject jsonObject);

    protected abstract RocketMQLocalTransactionState doCheckTransaction(JSONObject jsonObject);
}
