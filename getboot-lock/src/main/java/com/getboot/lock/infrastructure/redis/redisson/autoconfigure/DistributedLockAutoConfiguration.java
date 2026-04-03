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
package com.getboot.lock.infrastructure.redis.redisson.autoconfigure;

import com.getboot.lock.api.properties.LockProperties;
import com.getboot.lock.infrastructure.redis.redisson.aspect.DistributedLockAspect;
import com.getboot.lock.spi.DistributedLockAcquireFailureHandler;
import com.getboot.lock.spi.DistributedLockKeyResolver;
import com.getboot.lock.support.DefaultDistributedLockAcquireFailureHandler;
import com.getboot.lock.support.SpelDistributedLockKeyResolver;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Redis / Redisson 分布式锁自动配置类。
 *
 * @author qiheng
 */
@AutoConfiguration
@ConditionalOnClass(RedissonClient.class)
@ConditionalOnBean(RedissonClient.class)
@ConditionalOnProperty(prefix = "getboot.lock", name = "enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnExpression("'${getboot.lock.type:redis}' == 'redis' and '${getboot.lock.redis.enabled:true}' == 'true'")
@EnableConfigurationProperties(LockProperties.class)
public class DistributedLockAutoConfiguration {

    /**
     * 注册默认锁 key 解析器。
     *
     * @return 锁 key 解析器
     */
    @Bean
    @ConditionalOnMissingBean
    public DistributedLockKeyResolver distributedLockKeyResolver() {
        return new SpelDistributedLockKeyResolver();
    }

    /**
     * 注册默认锁获取失败处理器。
     *
     * @return 锁获取失败处理器
     */
    @Bean
    @ConditionalOnMissingBean
    public DistributedLockAcquireFailureHandler distributedLockAcquireFailureHandler() {
        return new DefaultDistributedLockAcquireFailureHandler();
    }

    /**
     * 注册 Redis 分布式锁切面。
     *
     * @param redisson Redisson 客户端
     * @param distributedLockKeyResolver 锁 key 解析器
     * @param distributedLockAcquireFailureHandler 锁获取失败处理器
     * @param properties 锁配置属性
     * @return Redis 分布式锁切面
     */
    @Bean
    @ConditionalOnMissingBean
    public DistributedLockAspect distributedLockAspect(
            RedissonClient redisson,
            DistributedLockKeyResolver distributedLockKeyResolver,
            DistributedLockAcquireFailureHandler distributedLockAcquireFailureHandler,
            LockProperties properties) {
        return new DistributedLockAspect(
                redisson,
                distributedLockKeyResolver,
                distributedLockAcquireFailureHandler,
                properties
        );
    }
}
