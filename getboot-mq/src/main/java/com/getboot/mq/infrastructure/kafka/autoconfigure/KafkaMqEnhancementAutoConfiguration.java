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
package com.getboot.mq.infrastructure.kafka.autoconfigure;

import com.getboot.mq.api.producer.MqMessageProducer;
import com.getboot.mq.api.properties.MqProperties;
import com.getboot.mq.api.properties.MqTraceProperties;
import com.getboot.mq.infrastructure.kafka.aop.KafkaMqTraceListenerAspect;
import com.getboot.mq.infrastructure.kafka.producer.KafkaMqMessageProducer;
import com.getboot.mq.spi.MqMessageHeadersCustomizer;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;

/**
 * Kafka MQ 增强自动配置。
 *
 * @author qiheng
 */
@AutoConfiguration
@ConditionalOnClass({KafkaTemplate.class, KafkaListener.class})
@EnableConfigurationProperties({MqProperties.class, MqTraceProperties.class})
@ConditionalOnProperty(prefix = "getboot.mq", name = "enabled", havingValue = "true")
@ConditionalOnExpression("'${getboot.mq.type:rocketmq}' == 'kafka' and '${getboot.mq.kafka.enabled:false}' == 'true'")
public class KafkaMqEnhancementAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public MqMessageProducer mqMessageProducer(
            KafkaTemplate<Object, Object> kafkaTemplate,
            MqTraceProperties traceProperties,
            ObjectProvider<MqMessageHeadersCustomizer> messageHeadersCustomizers) {
        return new KafkaMqMessageProducer(
                kafkaTemplate,
                traceProperties,
                messageHeadersCustomizers.orderedStream().toList()
        );
    }

    @Bean
    @ConditionalOnMissingBean
    public KafkaMqTraceListenerAspect kafkaMqTraceListenerAspect(MqTraceProperties traceProperties) {
        return new KafkaMqTraceListenerAspect(traceProperties);
    }
}
