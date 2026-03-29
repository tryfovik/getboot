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
package com.getboot.transaction.api.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 分布式事务 starter 配置。
 *
 * <p>对外保持能力层配置前缀，当前内部实现基于 Seata。</p>
 *
 * @author qiheng
 */
@Data
@ConfigurationProperties(prefix = "getboot.transaction")
public class DistributedTransactionProperties {

    /**
     * 是否全局启用分布式事务能力。
     */
    private boolean enabled = true;

    /**
     * Seata 实现配置。
     */
    private final Seata seata = new Seata();

    /**
     * 跨技术栈兼容性配置。
     */
    private final Compatibility compatibility = new Compatibility();

    @Data
    public static class Seata {
        /**
         * 是否启用 Seata 实现。
         */
        private boolean enabled = true;

        /**
         * 当前事务模式标识，默认 AT。
         */
        private String mode = "AT";

        /**
         * 事务应用标识。
         */
        private String applicationId;

        /**
         * Seata 事务组名称。
         */
        private String txServiceGroup = "default_tx_group";
    }

    @Data
    public static class Compatibility {
        /**
         * 冲突时是否直接中断启动。
         */
        private boolean failFastOnShardingConflict = true;

        /**
         * 是否允许显式混用 Seata 与 Sharding。
         */
        private boolean allowShardingHybrid = false;

        /**
         * 当二者混用时期望的 ShardingSphere 事务类型。
         */
        private String expectedShardingTransactionType = "LOCAL";
    }
}
