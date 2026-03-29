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
package com.getboot.transaction.infrastructure.seata.autoconfigure;

import com.getboot.transaction.api.properties.DistributedTransactionProperties;
import com.getboot.transaction.support.compatibility.SeataShardingCompatibilityVerifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

/**
 * Seata 分布式事务自动配置。
 *
 * <p>当前主要提供 Seata 配置桥接，以及与分库分表能力的兼容性守卫。</p>
 *
 * @author qiheng
 */
@AutoConfiguration
@ConditionalOnClass(name = "io.seata.spring.annotation.GlobalTransactionScanner")
@ConditionalOnProperty(prefix = "getboot.transaction", name = {"enabled", "seata.enabled"}, havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(DistributedTransactionProperties.class)
public class SeataDistributedTransactionAutoConfiguration {

    /**
     * 注册 Seata 与分库分表兼容性校验器。
     *
     * @param environment 当前环境
     * @param properties 事务配置
     * @return 兼容性校验器
     */
    @Bean
    @ConditionalOnMissingBean
    public SeataShardingCompatibilityVerifier seataShardingCompatibilityVerifier(
            Environment environment,
            DistributedTransactionProperties properties) {
        return new SeataShardingCompatibilityVerifier(environment, properties);
    }
}
