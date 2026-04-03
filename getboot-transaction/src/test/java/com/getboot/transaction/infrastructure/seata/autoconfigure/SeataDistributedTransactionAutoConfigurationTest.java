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

import com.getboot.transaction.support.compatibility.SeataShardingCompatibilityVerifier;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link SeataDistributedTransactionAutoConfiguration} 测试。
 *
 * @author qiheng
 */
class SeataDistributedTransactionAutoConfigurationTest {

    /**
     * 自动配置上下文运行器。
     */
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(SeataDistributedTransactionAutoConfiguration.class));

    /**
     * 验证事务模块启用时会注册兼容性校验器。
     */
    @Test
    void shouldRegisterCompatibilityVerifierWhenTransactionEnabled() {
        contextRunner
                .withPropertyValues(
                        "getboot.transaction.enabled=true",
                        "getboot.transaction.seata.enabled=true"
                )
                .run(context -> {
                    assertTrue(context.containsBean("seataShardingCompatibilityVerifier"));
                    assertTrue(context.getBean(SeataShardingCompatibilityVerifier.class) != null);
                });
    }

    /**
     * 验证事务模块关闭时不会注册兼容性校验器。
     */
    @Test
    void shouldSkipCompatibilityVerifierWhenTransactionDisabled() {
        contextRunner
                .withPropertyValues(
                        "getboot.transaction.enabled=false",
                        "getboot.transaction.seata.enabled=true"
                )
                .run(context -> assertFalse(context.containsBean("seataShardingCompatibilityVerifier")));
    }
}
