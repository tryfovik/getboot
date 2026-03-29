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
package com.getboot.lock.api.annotation;

import com.getboot.lock.api.constant.DistributedLockConstants;
import org.springframework.core.annotation.Order;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 分布式锁注解
 * 用于标注需要加分布式锁的方法
 *
 * @author qiheng
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Order(Integer.MIN_VALUE)
public @interface DistributedLock {

    /**
     * 锁的应用场景
     *
     * @return 场景名称
     */
    String scene();

    /**
     * 加锁的key值
     * 优先取key()，如果没有设置，则取keyExpression()
     *
     * @return 锁的key值
     */
    String key() default DistributedLockConstants.NONE_KEY;

    /**
     * SPEL表达式，用于动态生成锁的key
     * 示例：
     * <pre>
     *     #id
     *     #insertResult.id
     * </pre>
     *
     * @return SPEL表达式
     */
    String keyExpression() default DistributedLockConstants.NONE_KEY;

    /**
     * 锁的超时时间（毫秒）
     * 默认不设置超时时间（自动续期）
     *
     * @return 超时时间
     */
    int expireTime() default DistributedLockConstants.DEFAULT_EXPIRE_TIME;

    /**
     * 获取锁的最大等待时间（毫秒）
     * 默认不设置等待时间（一直等待直到获取锁）
     *
     * @return 等待时间
     */
    int waitTime() default DistributedLockConstants.DEFAULT_WAIT_TIME;
}
