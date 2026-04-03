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

import com.getboot.search.api.properties.SearchProperties;
import com.getboot.search.spi.SearchIndexNameResolver;
import com.getboot.search.support.DefaultSearchIndexNameResolver;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * 搜索模块核心自动配置。
 *
 * @author qiheng
 */
@AutoConfiguration
@ConditionalOnProperty(prefix = "getboot.search", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(SearchProperties.class)
public class SearchCoreAutoConfiguration {

    /**
     * 注册默认索引名解析器。
     *
     * @param properties 搜索模块配置
     * @return 索引名解析器
     */
    @Bean
    @ConditionalOnMissingBean
    public SearchIndexNameResolver searchIndexNameResolver(SearchProperties properties) {
        return new DefaultSearchIndexNameResolver(properties);
    }
}
