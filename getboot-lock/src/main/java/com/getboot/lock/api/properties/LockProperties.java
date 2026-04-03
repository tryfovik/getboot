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
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 分布式锁配置属性。
 *
 * @author qiheng
 */
@Data
@ConfigurationProperties(prefix = "getboot.lock")
public class LockProperties {

    /**
     * 是否启用分布式锁能力。
     */
    private boolean enabled = true;

    /**
     * 当前启用的锁实现类型。
     */
    private String type = DistributedLockConstants.LOCK_TYPE_REDIS;

    /**
     * Redis 锁配置。
     */
    private Redis redis = new Redis();

    /**
     * 数据库锁配置。
     */
    private Database database = new Database();

    /**
     * ZooKeeper 锁配置。
     */
    private Zookeeper zookeeper = new Zookeeper();

    /**
     * Redis 锁配置项。
     */
    @Data
    public static class Redis {

        /**
         * 是否启用 Redis 锁实现。
         */
        private boolean enabled = true;

        /**
         * Redis 锁 key 前缀。
         */
        private String keyPrefix = "distributed_lock";
    }

    /**
     * 数据库锁配置项。
     */
    @Data
    public static class Database {

        /**
         * 是否启用数据库锁实现。
         */
        private boolean enabled = false;

        /**
         * 数据库锁 key 前缀。
         */
        private String keyPrefix = "distributed_lock";

        /**
         * 锁表名称。
         */
        private String tableName = "distributed_lock";

        /**
         * 默认锁租约时长，单位毫秒。
         */
        private long leaseMs = 30000;

        /**
         * 获取锁失败后的重试间隔，单位毫秒。
         */
        private long retryIntervalMs = 100;

        /**
         * 是否在启动时自动初始化锁表。
         */
        private boolean initializeSchema = false;
    }

    /**
     * ZooKeeper 锁配置项。
     */
    @Data
    public static class Zookeeper {

        /**
         * 是否启用 ZooKeeper 锁实现。
         */
        private boolean enabled = false;

        /**
         * ZooKeeper 锁 key 前缀。
         */
        private String keyPrefix = "distributed_lock";

        /**
         * ZooKeeper 锁节点根路径。
         */
        private String basePath = "/getboot/lock";
    }
}
