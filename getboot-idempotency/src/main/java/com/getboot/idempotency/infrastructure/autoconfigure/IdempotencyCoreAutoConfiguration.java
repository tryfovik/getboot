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
package com.getboot.idempotency.infrastructure.autoconfigure;

import com.getboot.idempotency.api.properties.IdempotencyProperties;
import com.getboot.idempotency.spi.IdempotencyDuplicateRequestHandler;
import com.getboot.idempotency.spi.IdempotencyKeyResolver;
import com.getboot.idempotency.spi.IdempotencyStore;
import com.getboot.idempotency.support.DefaultIdempotencyDuplicateRequestHandler;
import com.getboot.idempotency.support.SpelIdempotencyKeyResolver;
import com.getboot.idempotency.support.aop.IdempotencyAspect;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Idempotency core auto-configuration.
 *
 * @author qiheng
 */
@AutoConfiguration
@ConditionalOnClass(ProceedingJoinPoint.class)
@ConditionalOnProperty(prefix = "getboot.idempotency", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(IdempotencyProperties.class)
public class IdempotencyCoreAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public IdempotencyKeyResolver idempotencyKeyResolver() {
        return new SpelIdempotencyKeyResolver();
    }

    @Bean
    @ConditionalOnMissingBean
    public IdempotencyDuplicateRequestHandler idempotencyDuplicateRequestHandler() {
        return new DefaultIdempotencyDuplicateRequestHandler();
    }

    @Bean
    @ConditionalOnBean(IdempotencyStore.class)
    @ConditionalOnMissingBean
    public IdempotencyAspect idempotencyAspect(IdempotencyStore idempotencyStore,
                                               IdempotencyKeyResolver idempotencyKeyResolver,
                                               IdempotencyDuplicateRequestHandler duplicateRequestHandler,
                                               IdempotencyProperties properties) {
        return new IdempotencyAspect(
                idempotencyStore,
                idempotencyKeyResolver,
                duplicateRequestHandler,
                properties
        );
    }
}
