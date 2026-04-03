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
package com.getboot.search.infrastructure.autoconfigure;

import com.getboot.search.api.operator.SearchOperator;
import com.getboot.search.infrastructure.elasticsearch.support.ElasticsearchRestGateway;
import com.getboot.search.spi.SearchIndexNameResolver;
import org.elasticsearch.client.RestClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

/**
 * 搜索自动配置测试。
 *
 * @author qiheng
 */
class SearchAutoConfigurationTest {

    /**
     * 上下文运行器。
     */
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(SearchAutoConfiguration.class))
            .withPropertyValues(
                    "getboot.search.enabled=true",
                    "getboot.search.type=elasticsearch",
                    "getboot.search.elasticsearch.enabled=true",
                    "getboot.search.elasticsearch.uris[0]=http://127.0.0.1:9200"
            );

    /**
     * 验证自动配置会注册默认搜索 Bean。
     */
    @Test
    void shouldRegisterDefaultSearchBeans() {
        contextRunner.run(context -> {
            assertInstanceOf(SearchIndexNameResolver.class, context.getBean(SearchIndexNameResolver.class));
            assertInstanceOf(RestClient.class, context.getBean(RestClient.class));
            assertInstanceOf(ElasticsearchRestGateway.class, context.getBean(ElasticsearchRestGateway.class));
            assertInstanceOf(SearchOperator.class, context.getBean(SearchOperator.class));
        });
    }
}
