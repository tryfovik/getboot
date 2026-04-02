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
package com.getboot.idempotency.api.annotation;

import com.getboot.idempotency.api.constant.IdempotencyConstants;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Method-level idempotency annotation.
 *
 * @author qiheng
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Idempotent {

    /**
     * Idempotency scene name.
     *
     * <p>When empty, falls back to "ClassName.methodName".</p>
     *
     * @return scene name
     */
    String scene() default "";

    /**
     * Fixed idempotency key.
     *
     * <p>When configured, takes precedence over {@link #keyExpression()}.</p>
     *
     * @return fixed key
     */
    String key() default "";

    /**
     * SpEL expression used to resolve the idempotency key.
     *
     * @return SpEL key expression
     */
    String keyExpression() default "";

    /**
     * Result cache TTL in seconds.
     *
     * <p>Use {@code -1} to follow {@code getboot.idempotency.default-ttl-seconds}.</p>
     *
     * @return ttl seconds
     */
    long ttlSeconds() default IdempotencyConstants.USE_DEFAULT_TTL_SECONDS;

    /**
     * Message used when the same key is still being processed.
     *
     * @return duplicate processing message
     */
    String message() default "Duplicate request. Please do not submit repeatedly.";
}
