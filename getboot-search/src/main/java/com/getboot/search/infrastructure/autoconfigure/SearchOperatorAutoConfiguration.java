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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.getboot.search.api.operator.SearchOperator;
import com.getboot.search.api.properties.SearchProperties;
import com.getboot.search.infrastructure.elasticsearch.support.ElasticsearchRestGateway;
import com.getboot.search.infrastructure.elasticsearch.support.ElasticsearchSearchOperator;
import com.getboot.search.spi.SearchIndexNameResolver;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

/**
 * 搜索门面自动配置。
 *
 * @author qiheng
 */
@AutoConfiguration
@ConditionalOnProperty(prefix = "getboot.search", name = "enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnExpression("'${getboot.search.type:elasticsearch}' == 'elasticsearch'")
public class SearchOperatorAutoConfiguration {

    /**
     * 注册默认搜索门面。
     *
     * @param gateway Elasticsearch 请求网关
     * @param indexNameResolver 索引名解析器
     * @param properties 搜索模块配置
     * @param objectMapperProvider Jackson 映射器提供器
     * @return 搜索门面
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(ElasticsearchRestGateway.class)
    public SearchOperator searchOperator(
            ElasticsearchRestGateway gateway,
            SearchIndexNameResolver indexNameResolver,
            SearchProperties properties,
            ObjectProvider<ObjectMapper> objectMapperProvider) {
        return new ElasticsearchSearchOperator(
                gateway,
                indexNameResolver,
                objectMapperProvider.getIfAvailable(ObjectMapper::new),
                properties
        );
    }
}
