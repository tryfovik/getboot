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

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * 方法级限流注解。
 *
 * <p>用于声明单个方法的限流名称、许可数、超时时间与提示信息。</p>
 *
 * @author qiheng
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimit {
    String value();
    long permits() default 1;
    long timeout() default 5;
    TimeUnit timeUnit() default TimeUnit.SECONDS;
    String message() default "The system is busy. Please try again later.";
}
