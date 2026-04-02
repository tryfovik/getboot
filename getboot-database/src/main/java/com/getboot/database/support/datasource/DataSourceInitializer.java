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
package com.getboot.database.support.datasource;

import com.getboot.database.api.properties.DatabaseProperties;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;

import javax.sql.DataSource;
import java.sql.SQLException;

/**
 * 数据源启动初始化器。
 *
 * <p>用于在应用启动期间提前触发数据源连接，并按配置执行启动后连通性校验。</p>
 *
 * @author qiheng
 */
public class DataSourceInitializer {

    private static final Logger log = LoggerFactory.getLogger(DataSourceInitializer.class);

    private final DataSource dataSource;
    private final DatabaseProperties.Init properties;

    public DataSourceInitializer(DataSource dataSource, DatabaseProperties.Init properties) {
        this.dataSource = dataSource;
        this.properties = properties;
    }

    /**
     * 在Bean初始化后立即执行（较早的时机）
     */
    @PostConstruct
    @Order(1)
    public void init() {
        if (properties.isEnabled()) {
            log.info("Starting datasource initialization.");
            long startTime = System.currentTimeMillis();

            try {
                try (var connection = dataSource.getConnection()) {
                    String databaseProductName = connection.getMetaData().getDatabaseProductName();
                    String databaseVersion = connection.getMetaData().getDatabaseProductVersion();
                    long costTime = System.currentTimeMillis() - startTime;

                    log.info("Datasource initialized successfully. product={}, version={}, cost={}ms",
                            databaseProductName, databaseVersion, costTime);
                }
            } catch (SQLException e) {
                long costTime = System.currentTimeMillis() - startTime;
                    log.error("Datasource initialization failed. cost={}ms", costTime, e);

                if (properties.isStrictMode()) {
                    throw new RuntimeException("Datasource connection initialization failed during startup.", e);
                } else {
                    log.warn("Datasource initialization failed, but the application will continue in non-strict mode.");
                }
            } catch (Exception e) {
                long costTime = System.currentTimeMillis() - startTime;
                log.error("Unexpected datasource initialization error. cost={}ms", costTime, e);

                if (properties.isStrictMode()) {
                    throw new RuntimeException("Unexpected datasource initialization error during startup.", e);
                }
            }
        } else {
            log.info("Eager datasource initialization is disabled.");
        }
    }

    @EventListener(ApplicationReadyEvent.class)
    @Order(2)
    public void validateAfterStartup(ApplicationReadyEvent event) {
        log.info("Received datasource validation event: {}", event);
        if (properties.isValidateAfterStartup() && properties.isEnabled()) {
            log.info("Validating datasource connectivity after application startup.");
            try {
                try (var connection = dataSource.getConnection();
                     var statement = connection.createStatement()) {
                    statement.execute("SELECT 1");
                    log.info("Datasource validation after startup succeeded.");
                }
            } catch (SQLException e) {
                log.warn("Datasource validation after startup failed: {}", e.getMessage());
            }
        }
    }
}
