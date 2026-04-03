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

import com.getboot.lock.api.exception.DistributedLockException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Timestamp;
import java.time.Instant;

/**
 * JDBC 分布式锁仓储。
 *
 * @author qiheng
 */
public class JdbcDistributedLockRepository {

    /**
     * JDBC 模板。
     */
    private final JdbcTemplate jdbcTemplate;

    /**
     * 插入锁记录 SQL。
     */
    private final String insertSql;

    /**
     * 更新已过期锁记录 SQL。
     */
    private final String updateExpiredSql;

    /**
     * 删除锁记录 SQL。
     */
    private final String deleteSql;

    /**
     * 创建 JDBC 分布式锁仓储。
     *
     * @param jdbcTemplate JDBC 模板
     * @param tableName 锁表名
     */
    public JdbcDistributedLockRepository(JdbcTemplate jdbcTemplate, String tableName) {
        this.jdbcTemplate = jdbcTemplate;
        String resolvedTableName = validateTableName(tableName);
        this.insertSql = "INSERT INTO " + resolvedTableName
                + " (lock_key, owner_id, lock_until, created_at, updated_at) VALUES (?, ?, ?, ?, ?)";
        this.updateExpiredSql = "UPDATE " + resolvedTableName
                + " SET owner_id = ?, lock_until = ?, updated_at = ?"
                + " WHERE lock_key = ? AND lock_until <= ?";
        this.deleteSql = "DELETE FROM " + resolvedTableName + " WHERE lock_key = ? AND owner_id = ?";
    }

    /**
     * 尝试获取锁。
     *
     * @param lockKey 完整锁 key
     * @param ownerId 锁持有者标识
     * @param lockUntil 锁过期时间
     * @param now 当前时间
     * @return 是否获取成功
     */
    public boolean tryAcquire(String lockKey, String ownerId, Instant lockUntil, Instant now) {
        Timestamp lockUntilTimestamp = Timestamp.from(lockUntil);
        Timestamp nowTimestamp = Timestamp.from(now);
        try {
            return jdbcTemplate.update(
                    insertSql,
                    lockKey,
                    ownerId,
                    lockUntilTimestamp,
                    nowTimestamp,
                    nowTimestamp
            ) > 0;
        } catch (DuplicateKeyException ignored) {
            return jdbcTemplate.update(
                    updateExpiredSql,
                    ownerId,
                    lockUntilTimestamp,
                    nowTimestamp,
                    lockKey,
                    nowTimestamp
            ) > 0;
        }
    }

    /**
     * 释放指定持有者的锁。
     *
     * @param lockKey 完整锁 key
     * @param ownerId 锁持有者标识
     */
    public void release(String lockKey, String ownerId) {
        jdbcTemplate.update(deleteSql, lockKey, ownerId);
    }

    /**
     * 校验锁表名是否合法。
     *
     * @param tableName 锁表名
     * @return 合法的锁表名
     */
    public static String validateTableName(String tableName) {
        if (tableName == null || !tableName.matches("[A-Za-z0-9_]+")) {
            throw new DistributedLockException(
                    "Distributed lock table-name must contain only letters, numbers and underscores."
            );
        }
        return tableName;
    }
}
