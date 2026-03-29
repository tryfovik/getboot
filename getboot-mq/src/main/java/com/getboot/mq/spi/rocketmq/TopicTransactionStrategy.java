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
package com.getboot.mq.spi.rocketmq;

import org.apache.rocketmq.spring.core.RocketMQLocalTransactionState;

/**
 * Topic 事务策略接口。
 *
 * <p>定义主题匹配、本地事务执行与事务回查的统一入口。</p>
 *
 * @author qiheng
 */
public interface TopicTransactionStrategy {

    /**
     * 判断当前策略是否支持指定 topic。
     *
     * @param topic 消息主题
     * @return 是否支持
     */
    boolean supports(String topic);

    /**
     * 执行本地事务。
     *
     * @param arg 渠道回传的事务上下文
     * @return 本地事务执行结果
     */
    RocketMQLocalTransactionState executeTransaction(Object arg);

    /**
     * 执行事务回查。
     *
     * @param arg 渠道回传的事务上下文
     * @return 事务回查结果
     */
    RocketMQLocalTransactionState checkTransaction(Object arg);
}
