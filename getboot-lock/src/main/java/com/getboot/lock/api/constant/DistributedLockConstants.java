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
package com.getboot.lock.api.constant;

/**
 * 分布式锁常量定义。
 *
 * @author qiheng
 */
public final class DistributedLockConstants {

    /**
     * 工具类私有构造方法。
     */
    private DistributedLockConstants() {
    }

    /**
     * 表示未显式指定 key 的占位值。
     */
    public static final String NONE_KEY = "NONE";

    /**
     * 默认过期时间占位值。
     */
    public static final int DEFAULT_EXPIRE_TIME = -1;

    /**
     * 默认等待时间占位值。
     */
    public static final int DEFAULT_WAIT_TIME = Integer.MAX_VALUE;

    /**
     * Redis 锁实现类型。
     */
    public static final String LOCK_TYPE_REDIS = "redis";

    /**
     * 数据库锁实现类型。
     */
    public static final String LOCK_TYPE_DATABASE = "database";

    /**
     * ZooKeeper 锁实现类型。
     */
    public static final String LOCK_TYPE_ZOOKEEPER = "zookeeper";
}
