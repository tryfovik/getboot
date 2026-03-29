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
package com.getboot.lock.api.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 分布式锁能力配置。
 *
 * <p>当前 lock 模块先通过 redis 子树承接实现能力，后续可继续扩展 database / zookeeper 等介质。</p>
 *
 * @author qiheng
 */
@ConfigurationProperties(prefix = "getboot.lock")
public class LockProperties {

    /**
     * 是否启用分布式锁能力。
     */
    private boolean enabled = true;

    /**
     * Redis 实现级配置。
     */
    private Redis redis = new Redis();

    /**
     * Redis 锁配置。
     */
    public static class Redis {
        private boolean enabled = true;
        private String keyPrefix = "distributed_lock";

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getKeyPrefix() {
            return keyPrefix;
        }

        public void setKeyPrefix(String keyPrefix) {
            this.keyPrefix = keyPrefix;
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Redis getRedis() {
        return redis;
    }

    public void setRedis(Redis redis) {
        this.redis = redis;
    }
}
