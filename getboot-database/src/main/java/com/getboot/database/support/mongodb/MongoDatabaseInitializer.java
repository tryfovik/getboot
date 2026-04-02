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
package com.getboot.database.support.mongodb;

import com.getboot.database.api.properties.DatabaseProperties;
import jakarta.annotation.PostConstruct;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.data.mongodb.core.MongoTemplate;

/**
 * MongoDB 启动初始化器。
 *
 * <p>用于在应用启动阶段提前执行 ping 校验，并在应用完成启动后再次验证连通性。</p>
 *
 * @author qiheng
 */
public class MongoDatabaseInitializer {

    private static final Logger log = LoggerFactory.getLogger(MongoDatabaseInitializer.class);

    private final MongoTemplate mongoTemplate;
    private final DatabaseProperties.Init properties;

    public MongoDatabaseInitializer(MongoTemplate mongoTemplate, DatabaseProperties.Init properties) {
        this.mongoTemplate = mongoTemplate;
        this.properties = properties;
    }

    @PostConstruct
    @Order(1)
    public void init() {
        if (!properties.isEnabled()) {
            log.info("Eager MongoDB initialization is disabled.");
            return;
        }
        log.info("Starting MongoDB initialization.");
        long startTime = System.currentTimeMillis();
        try {
            Document result = ping();
            long costTime = System.currentTimeMillis() - startTime;
            log.info("MongoDB initialized successfully. database={}, result={}, cost={}ms",
                    mongoTemplate.getDb().getName(), result.toJson(), costTime);
        } catch (Exception ex) {
            long costTime = System.currentTimeMillis() - startTime;
            log.error("MongoDB initialization failed. cost={}ms", costTime, ex);
            if (properties.isStrictMode()) {
                throw new RuntimeException("MongoDB initialization failed during startup.", ex);
            }
            log.warn("MongoDB initialization failed, but the application will continue in non-strict mode.");
        }
    }

    @EventListener(ApplicationReadyEvent.class)
    @Order(2)
    public void validateAfterStartup(ApplicationReadyEvent event) {
        log.info("Received MongoDB validation event: {}", event);
        if (!properties.isEnabled() || !properties.isValidateAfterStartup()) {
            return;
        }
        log.info("Validating MongoDB connectivity after application startup.");
        try {
            Document result = ping();
            log.info("MongoDB validation after startup succeeded. database={}, result={}",
                    mongoTemplate.getDb().getName(), result.toJson());
        } catch (Exception ex) {
            log.warn("MongoDB validation after startup failed: {}", ex.getMessage());
        }
    }

    private Document ping() {
        return mongoTemplate.executeCommand(new Document("ping", 1));
    }
}
