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
package com.getboot.lock.spi;

import com.getboot.lock.api.annotation.DistributedLock;

/**
 * 分布式锁获取失败处理器。
 *
 * <p>业务方可通过注册该类型 Bean，自定义锁获取失败时的异常转换与处理逻辑。</p>
 *
 * @author qiheng
 */
@FunctionalInterface
public interface DistributedLockAcquireFailureHandler {

    /**
     * 处理锁获取失败。
     *
     * @param lockKey 完整锁键
     * @param distributedLock 锁注解
     */
    void onFailure(String lockKey, DistributedLock distributedLock);
}
