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
     * MongoDB 增强配置。
     */
    private MongoDb mongodb = new MongoDb();

    /**
     * 分库分表增强配置。
     */
    private Sharding sharding = new Sharding();

    public static class Datasource {

        /**
         * 数据源预热初始化配置。
         */
        private Init init = new Init();

        public Init getInit() {
            return init;
        }

        public void setInit(Init init) {
            this.init = init;
        }
    }

    /**
     * 数据源预热初始化配置。
     */
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

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public boolean isStrictMode() {
            return strictMode;
        }

        public void setStrictMode(boolean strictMode) {
            this.strictMode = strictMode;
        }

        public long getTimeout() {
            return timeout;
        }

        public void setTimeout(long timeout) {
            this.timeout = timeout;
        }

        public boolean isValidateAfterStartup() {
            return validateAfterStartup;
        }

        public void setValidateAfterStartup(boolean validateAfterStartup) {
            this.validateAfterStartup = validateAfterStartup;
        }
    }

    /**
     * MongoDB 配置。
     */
    public static class MongoDb {

        /**
         * 是否启用 MongoDB 增强能力。
         */
        private boolean enabled = false;

        /**
         * 是否启用自动索引创建。
         */
        private boolean autoIndexCreation = false;

        /**
         * MongoDB 启动初始化配置。
         */
        private Init init = new Init();

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public boolean isAutoIndexCreation() {
            return autoIndexCreation;
        }

        public void setAutoIndexCreation(boolean autoIndexCreation) {
            this.autoIndexCreation = autoIndexCreation;
        }

        public Init getInit() {
            return init;
        }

        public void setInit(Init init) {
            this.init = init;
        }
    }

    /**
     * 分库分表配置。
     */
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

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getRuleConfig() {
            return ruleConfig;
        }

        public void setRuleConfig(String ruleConfig) {
            this.ruleConfig = ruleConfig;
        }

        public boolean isReuseBeanDataSources() {
            return reuseBeanDataSources;
        }

        public void setReuseBeanDataSources(boolean reuseBeanDataSources) {
            this.reuseBeanDataSources = reuseBeanDataSources;
        }

        public List<String> getDataSourceBeans() {
            return dataSourceBeans;
        }

        public void setDataSourceBeans(List<String> dataSourceBeans) {
            this.dataSourceBeans = dataSourceBeans;
        }

        public String getTransactionType() {
            return transactionType;
        }

        public void setTransactionType(String transactionType) {
            this.transactionType = transactionType;
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Datasource getDatasource() {
        return datasource;
    }

    public void setDatasource(Datasource datasource) {
        this.datasource = datasource;
    }

    public MongoDb getMongodb() {
        return mongodb;
    }

    public void setMongodb(MongoDb mongodb) {
        this.mongodb = mongodb;
    }

    public Sharding getSharding() {
        return sharding;
    }

    public void setSharding(Sharding sharding) {
        this.sharding = sharding;
    }
}
