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
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.data.mongodb.core.MongoTemplate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MongoDatabaseInitializerTest {

    @Test
    void shouldPingMongoDatabaseDuringInitialization() {
        MongoTemplate mongoTemplate = mock(MongoTemplate.class);
        MongoDatabase mongoDatabase = mock(MongoDatabase.class);
        when(mongoTemplate.getDb()).thenReturn(mongoDatabase);
        when(mongoDatabase.getName()).thenReturn("demo");
        when(mongoTemplate.executeCommand(any(Document.class))).thenReturn(new Document("ok", 1));

        DatabaseProperties.Init properties = new DatabaseProperties.Init();
        properties.setEnabled(true);
        properties.setStrictMode(true);

        MongoDatabaseInitializer initializer = new MongoDatabaseInitializer(mongoTemplate, properties);

        assertDoesNotThrow(initializer::init);
        initializer.validateAfterStartup(mock(ApplicationReadyEvent.class));

        verify(mongoTemplate, times(2)).executeCommand(new Document("ping", 1));
    }

    @Test
    void shouldThrowWhenStrictModeAndInitializationFails() {
        MongoTemplate mongoTemplate = mock(MongoTemplate.class);
        when(mongoTemplate.executeCommand(any(Document.class))).thenThrow(new IllegalStateException("mongo down"));

        DatabaseProperties.Init properties = new DatabaseProperties.Init();
        properties.setEnabled(true);
        properties.setStrictMode(true);

        MongoDatabaseInitializer initializer = new MongoDatabaseInitializer(mongoTemplate, properties);

        assertThrows(RuntimeException.class, initializer::init);
    }
}
