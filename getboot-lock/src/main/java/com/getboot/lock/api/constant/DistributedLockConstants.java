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
 * Distributed lock constants.
 *
 * @author qiheng
 */
public final class DistributedLockConstants {

    private DistributedLockConstants() {
    }

    public static final String NONE_KEY = "NONE";

    public static final int DEFAULT_EXPIRE_TIME = -1;

    public static final int DEFAULT_WAIT_TIME = Integer.MAX_VALUE;

    public static final String LOCK_TYPE_REDIS = "redis";

    public static final String LOCK_TYPE_DATABASE = "database";

    public static final String LOCK_TYPE_ZOOKEEPER = "zookeeper";
}
