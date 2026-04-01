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
package com.getboot.mq.api.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * MQ 模块配置。
 *
 * <p>用于统一控制当前启用的 MQ 实现类型。</p>
 *
 * @author qiheng
 */
@ConfigurationProperties(prefix = "getboot.mq")
public class MqProperties {

    private boolean enabled = true;

    private String type = "rocketmq";

    private Rocketmq rocketmq = new Rocketmq();

    private Kafka kafka = new Kafka();

    public static class Rocketmq {

        private boolean enabled = true;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }

    public static class Kafka {

        private boolean enabled = false;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Rocketmq getRocketmq() {
        return rocketmq;
    }

    public void setRocketmq(Rocketmq rocketmq) {
        this.rocketmq = rocketmq;
    }

    public Kafka getKafka() {
        return kafka;
    }

    public void setKafka(Kafka kafka) {
        this.kafka = kafka;
    }
}
