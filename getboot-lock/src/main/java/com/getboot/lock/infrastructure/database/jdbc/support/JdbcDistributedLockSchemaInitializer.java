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
package com.getboot.lock.infrastructure.database.jdbc.support;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * JDBC 锁表初始化器。
 *
 * @author qiheng
 */
public class JdbcDistributedLockSchemaInitializer implements InitializingBean {

    /**
     * JDBC 模板。
     */
    private final JdbcTemplate jdbcTemplate;

    /**
     * 建表 DDL。
     */
    private final String ddl;

    /**
     * 创建 JDBC 锁表初始化器。
     *
     * @param jdbcTemplate JDBC 模板
     * @param tableName 锁表名
     */
    public JdbcDistributedLockSchemaInitializer(JdbcTemplate jdbcTemplate, String tableName) {
        this.jdbcTemplate = jdbcTemplate;
        String resolvedTableName = JdbcDistributedLockRepository.validateTableName(tableName);
        this.ddl = "CREATE TABLE IF NOT EXISTS " + resolvedTableName + " ("
                + "lock_key VARCHAR(255) PRIMARY KEY, "
                + "owner_id VARCHAR(64) NOT NULL, "
                + "lock_until TIMESTAMP(6) NOT NULL, "
                + "created_at TIMESTAMP(6) NOT NULL, "
                + "updated_at TIMESTAMP(6) NOT NULL"
                + ")";
    }

    /**
     * 在 Bean 初始化后执行建表逻辑。
     */
    @Override
    public void afterPropertiesSet() {
        jdbcTemplate.execute(ddl);
    }
}
