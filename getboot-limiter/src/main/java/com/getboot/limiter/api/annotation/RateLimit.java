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
package com.getboot.limiter.api.annotation;

import com.getboot.limiter.api.model.LimiterAlgorithm;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * 方法级限流注解。
 *
 * <p>对外只暴露一个注解入口，由业务方法直接声明算法、限流规则与 key 解析方式。</p>
 *
 * @author qiheng
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimit {

    /**
     * 限流场景名。
     *
     * <p>为空时默认使用“类名.方法名”。</p>
     *
     * @return 限流场景名
     */
    String scene() default "";

    /**
     * 固定限流 key。
     *
     * <p>配置了该值时优先使用，不再解析 {@link #keyExpression()}。</p>
     *
     * @return 固定 key
     */
    String key() default "";

    /**
     * SpEL 形式的动态限流 key。
     *
     * <p>例如 {@code #userId}、{@code #request.phone}。</p>
     *
     * @return 动态 key 表达式
     */
    String keyExpression() default "";

    /**
     * 限流算法。
     *
     * @return 限流算法
     */
    LimiterAlgorithm algorithm() default LimiterAlgorithm.SLIDING_WINDOW;

    /**
     * 单个周期的容量或配额。
     *
     * @return 限流配额
     */
    long rate();

    /**
     * 周期大小。
     *
     * @return 周期大小
     */
    long interval() default 1;

    /**
     * 周期单位。
     *
     * @return 周期单位
     */
    TimeUnit intervalUnit() default TimeUnit.SECONDS;

    /**
     * 本次调用申请的许可数。
     *
     * @return 许可数
     */
    long permits() default 1;

    /**
     * 获取许可时的最大等待时间。
     *
     * @return 最大等待时间
     */
    long timeout() default 0;

    /**
     * 等待时间单位。
     *
     * @return 等待时间单位
     */
    TimeUnit timeoutUnit() default TimeUnit.SECONDS;

    /**
     * 获取许可失败时的提示信息。
     *
     * @return 提示信息
     */
    String message() default "The system is busy. Please try again later.";
}
