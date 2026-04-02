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
package com.getboot.idempotency.api.properties;

import com.getboot.idempotency.api.constant.IdempotencyConstants;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Idempotency configuration properties.
 *
 * @author qiheng
 */
@ConfigurationProperties(prefix = "getboot.idempotency")
public class IdempotencyProperties {

    private boolean enabled = true;

    private String type = IdempotencyConstants.STORE_TYPE_REDIS;

    private long defaultTtlSeconds = IdempotencyConstants.DEFAULT_TTL_SECONDS;

    private Redis redis = new Redis();

    public String resolveKeyPrefix() {
        return redis.getKeyPrefix();
    }

    public static class Redis {

        private boolean enabled = true;

        private String keyPrefix = "getboot:idempotency";

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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getDefaultTtlSeconds() {
        return defaultTtlSeconds;
    }

    public void setDefaultTtlSeconds(long defaultTtlSeconds) {
        this.defaultTtlSeconds = defaultTtlSeconds;
    }

    public Redis getRedis() {
        return redis;
    }

    public void setRedis(Redis redis) {
        this.redis = redis;
    }
}
