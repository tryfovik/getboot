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
package com.getboot.lock.support;

import com.getboot.lock.api.annotation.DistributedLock;
import com.getboot.lock.api.exception.DistributedLockException;
import com.getboot.lock.spi.DistributedLockAcquireFailureHandler;

/**
 * 默认分布式锁获取失败处理器。
 *
 * @author qiheng
 */
public class DefaultDistributedLockAcquireFailureHandler implements DistributedLockAcquireFailureHandler {

    /**
     * 默认异常消息。
     */
    private static final String DEFAULT_MESSAGE = "Failed to acquire distributed lock.";

    /**
     * 抛出默认的获取失败异常。
     *
     * @param lockKey 完整锁 key
     * @param distributedLock 锁注解
     */
    @Override
    public void onFailure(String lockKey, DistributedLock distributedLock) {
        throw new DistributedLockException(DEFAULT_MESSAGE + " key=" + lockKey);
    }
}
