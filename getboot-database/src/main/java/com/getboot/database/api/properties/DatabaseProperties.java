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
package com.getboot.database.api.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * 数据访问能力配置。
 *
 * <p>用于统一收敛 `getboot.database.*` 下由 GetBoot 自身管理的增强配置。</p>
 *
 * @author qiheng
 */
@ConfigurationProperties(prefix = "getboot.database")
@Data
public class DatabaseProperties {

    /**
     * 是否启用数据库增强能力。
     */
    private boolean enabled;

    /**
     * 数据源增强配置。
     */
    private Datasource datasource = new Datasource();

    /**
     * 分库分表增强配置。
     */
    private Sharding sharding = new Sharding();

    @Data
    public static class Datasource {

        /**
         * 数据源预热初始化配置。
         */
        private Init init = new Init();
    }

    /**
     * 数据源预热初始化配置。
     */
    @Data
    public static class Init {
        /**
         * 是否启用立即初始化。
         */
        private boolean enabled = true;

        /**
         * 严格模式，开启后初始化失败会阻止应用启动。
         */
        private boolean strictMode = true;

        /**
         * 初始化超时时间，单位毫秒。
         */
        private long timeout = 30000;

        /**
         * 是否在应用启动完成后再次校验连接。
         */
        private boolean validateAfterStartup = true;
    }

    /**
     * 分库分表配置。
     */
    @Data
    public static class Sharding {

        /**
         * 是否启用分库分表能力。
         */
        private boolean enabled;

        /**
         * ShardingSphere 规则 YAML 文件位置。
         */
        private String ruleConfig = "classpath:sharding/sharding-rule.yaml";

        /**
         * 是否复用 Spring 容器中已存在的数据源 Bean。
         */
        private boolean reuseBeanDataSources = false;

        /**
         * 需要复用的数据源 Bean 名称列表；为空时默认复用全部候选数据源。
         */
        private List<String> dataSourceBeans = new ArrayList<>();

        /**
         * 对外声明的事务类型，供文档与兼容性检查使用。
         */
        private String transactionType = "LOCAL";
    }
}
