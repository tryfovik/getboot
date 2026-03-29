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
package com.getboot.transaction.support.compatibility;

import com.getboot.transaction.api.properties.DistributedTransactionProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.env.Environment;

import java.util.Locale;

/**
 * Seata 与分库分表组合兼容性校验器。
 *
 * <p>默认将 Seata 与 ShardingSphere 的混用视为高风险场景，要求业务显式确认。</p>
 *
 * @author qiheng
 */
@Slf4j
public class SeataShardingCompatibilityVerifier implements InitializingBean {

    private final Environment environment;
    private final DistributedTransactionProperties properties;

    public SeataShardingCompatibilityVerifier(Environment environment,
                                              DistributedTransactionProperties properties) {
        this.environment = environment;
        this.properties = properties;
    }

    @Override
    public void afterPropertiesSet() {
        if (!properties.isEnabled() || !properties.getSeata().isEnabled()) {
            return;
        }
        boolean shardingEnabled = environment.getProperty("getboot.database.sharding.enabled", Boolean.class, false);
        if (!shardingEnabled) {
            return;
        }

        String currentTransactionType = environment.getProperty(
                "getboot.database.sharding.transaction-type",
                environment.getProperty("spring.shardingsphere.props.default-transaction-type", "LOCAL")
        );
        String expectedTransactionType = properties.getCompatibility().getExpectedShardingTransactionType();
        boolean transactionTypeMatches = expectedTransactionType != null
                && expectedTransactionType.equalsIgnoreCase(currentTransactionType);

        if (properties.getCompatibility().isAllowShardingHybrid() && transactionTypeMatches) {
            log.warn("Seata and sharding are enabled together. Proceeding with explicit override. seataMode={}, shardingTransactionType={}",
                    properties.getSeata().getMode(), currentTransactionType);
            return;
        }

        String message = String.format(Locale.ROOT,
                "Seata and sharding are enabled together. Current shardingTransactionType=%s, expected=%s. "
                        + "GetBoot treats this combination as conflict-prone by default. "
                        + "If you really need the hybrid mode, set getboot.transaction.compatibility.allow-sharding-hybrid=true "
                        + "and keep getboot.database.sharding.transaction-type=%s.",
                currentTransactionType,
                expectedTransactionType,
                expectedTransactionType
        );
        if (properties.getCompatibility().isFailFastOnShardingConflict()) {
            throw new IllegalStateException(message);
        }
        log.warn(message);
    }
}
