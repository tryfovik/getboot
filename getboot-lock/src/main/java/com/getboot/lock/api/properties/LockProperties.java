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

import com.getboot.lock.api.constant.DistributedLockConstants;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Distributed lock configuration properties.
 *
 * @author qiheng
 */
@ConfigurationProperties(prefix = "getboot.lock")
public class LockProperties {

    private boolean enabled = true;

    private String type = DistributedLockConstants.LOCK_TYPE_REDIS;

    private Redis redis = new Redis();

    private Database database = new Database();

    private Zookeeper zookeeper = new Zookeeper();

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

    public static class Database {
        private boolean enabled = false;
        private String keyPrefix = "distributed_lock";
        private String tableName = "distributed_lock";
        private long leaseMs = 30000;
        private long retryIntervalMs = 100;
        private boolean initializeSchema = false;

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

        public String getTableName() {
            return tableName;
        }

        public void setTableName(String tableName) {
            this.tableName = tableName;
        }

        public long getLeaseMs() {
            return leaseMs;
        }

        public void setLeaseMs(long leaseMs) {
            this.leaseMs = leaseMs;
        }

        public long getRetryIntervalMs() {
            return retryIntervalMs;
        }

        public void setRetryIntervalMs(long retryIntervalMs) {
            this.retryIntervalMs = retryIntervalMs;
        }

        public boolean isInitializeSchema() {
            return initializeSchema;
        }

        public void setInitializeSchema(boolean initializeSchema) {
            this.initializeSchema = initializeSchema;
        }
    }

    public static class Zookeeper {
        private boolean enabled = false;
        private String keyPrefix = "distributed_lock";
        private String basePath = "/getboot/lock";

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

        public String getBasePath() {
            return basePath;
        }

        public void setBasePath(String basePath) {
            this.basePath = basePath;
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

    public Redis getRedis() {
        return redis;
    }

    public void setRedis(Redis redis) {
        this.redis = redis;
    }

    public Database getDatabase() {
        return database;
    }

    public void setDatabase(Database database) {
        this.database = database;
    }

    public Zookeeper getZookeeper() {
        return zookeeper;
    }

    public void setZookeeper(Zookeeper zookeeper) {
        this.zookeeper = zookeeper;
    }
}
