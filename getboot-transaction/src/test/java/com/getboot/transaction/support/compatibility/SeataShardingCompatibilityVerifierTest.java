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
import org.junit.jupiter.api.Test;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SeataShardingCompatibilityVerifierTest {

    @Test
    void shouldFailFastWhenNativeShardingRulesArePresent() {
        StandardEnvironment environment = new StandardEnvironment();
        environment.getPropertySources().addFirst(new MapPropertySource("source", Map.of(
                "spring.shardingsphere.rules.sharding.tables.t_order.actual-data-nodes", "ds_0.t_order_${0..1}",
                "spring.shardingsphere.props.default-transaction-type", "XA"
        )));

        DistributedTransactionProperties properties = new DistributedTransactionProperties();

        SeataShardingCompatibilityVerifier verifier = new SeataShardingCompatibilityVerifier(environment, properties);

        assertThrows(IllegalStateException.class, verifier::afterPropertiesSet);
    }

    @Test
    void shouldAllowHybridModeWhenTransactionTypeMatches() {
        StandardEnvironment environment = new StandardEnvironment();
        environment.getPropertySources().addFirst(new MapPropertySource("source", Map.of(
                "spring.shardingsphere.rules.sharding.tables.t_order.actual-data-nodes", "ds_0.t_order_${0..1}",
                "spring.shardingsphere.props.default-transaction-type", "LOCAL"
        )));

        DistributedTransactionProperties properties = new DistributedTransactionProperties();
        properties.getCompatibility().setAllowShardingHybrid(true);

        SeataShardingCompatibilityVerifier verifier = new SeataShardingCompatibilityVerifier(environment, properties);

        assertDoesNotThrow(verifier::afterPropertiesSet);
    }

    @Test
    void shouldIgnoreCompatibilityCheckWhenShardingIsNotConfigured() {
        StandardEnvironment environment = new StandardEnvironment();
        environment.getPropertySources().addFirst(new MapPropertySource("source", Map.of(
                "spring.shardingsphere.props.default-transaction-type", "XA"
        )));

        DistributedTransactionProperties properties = new DistributedTransactionProperties();

        SeataShardingCompatibilityVerifier verifier = new SeataShardingCompatibilityVerifier(environment, properties);

        assertDoesNotThrow(verifier::afterPropertiesSet);
    }
}
