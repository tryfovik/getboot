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
package com.getboot.search.infrastructure.elasticsearch.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.getboot.search.api.exception.SearchException;
import com.getboot.search.api.properties.SearchProperties;
import com.getboot.search.infrastructure.elasticsearch.support.DefaultElasticsearchRestGateway;
import com.getboot.search.infrastructure.elasticsearch.support.ElasticsearchRestGateway;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.message.BasicHeader;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Elasticsearch 自动配置。
 *
 * @author qiheng
 */
@AutoConfiguration
@ConditionalOnClass(RestClient.class)
@ConditionalOnProperty(prefix = "getboot.search", name = "enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnExpression("${getboot.search.elasticsearch.enabled:true} and '${getboot.search.type:elasticsearch}' == 'elasticsearch'")
public class ElasticsearchSearchAutoConfiguration {

    /**
     * 注册 Elasticsearch RestClient。
     *
     * @param properties 搜索模块配置
     * @return RestClient
     */
    @Bean(destroyMethod = "close")
    @ConditionalOnMissingBean
    public RestClient elasticsearchRestClient(SearchProperties properties) {
        HttpHost[] hosts = resolveHosts(properties);
        RestClientBuilder builder = RestClient.builder(hosts)
                .setRequestConfigCallback(requestConfigBuilder -> requestConfigBuilder
                        .setConnectTimeout((int) properties.getElasticsearch().getConnectTimeout().toMillis())
                        .setSocketTimeout((int) properties.getElasticsearch().getSocketTimeout().toMillis()))
                .setDefaultHeaders(resolveDefaultHeaders(properties));

        if (StringUtils.hasText(properties.getElasticsearch().getPathPrefix())) {
            builder.setPathPrefix(properties.getElasticsearch().getPathPrefix().trim());
        }
        if (!StringUtils.hasText(properties.getElasticsearch().getApiKey())
                && StringUtils.hasText(properties.getElasticsearch().getUsername())) {
            builder.setHttpClientConfigCallback(httpClientBuilder -> httpClientBuilder
                    .setDefaultCredentialsProvider(resolveCredentialsProvider(properties)));
        }
        return builder.build();
    }

    /**
     * 注册 Elasticsearch 请求网关。
     *
     * @param restClient RestClient
     * @param objectMapperProvider Jackson 映射器提供器
     * @return Elasticsearch 请求网关
     */
    @Bean
    @ConditionalOnMissingBean
    public ElasticsearchRestGateway elasticsearchRestGateway(
            RestClient restClient,
            ObjectProvider<ObjectMapper> objectMapperProvider) {
        return new DefaultElasticsearchRestGateway(
                restClient,
                objectMapperProvider.getIfAvailable(ObjectMapper::new)
        );
    }

    /**
     * 解析 Elasticsearch 节点列表。
     *
     * @param properties 搜索模块配置
     * @return Elasticsearch 节点数组
     */
    private HttpHost[] resolveHosts(SearchProperties properties) {
        List<HttpHost> hosts = new ArrayList<>();
        for (String uri : properties.getElasticsearch().getUris()) {
            if (StringUtils.hasText(uri)) {
                hosts.add(HttpHost.create(uri.trim()));
            }
        }
        if (hosts.isEmpty()) {
            throw new SearchException("getboot.search.elasticsearch.uris must not be empty.");
        }
        return hosts.toArray(HttpHost[]::new);
    }

    /**
     * 解析默认请求头。
     *
     * @param properties 搜索模块配置
     * @return 默认请求头数组
     */
    private Header[] resolveDefaultHeaders(SearchProperties properties) {
        List<Header> headers = new ArrayList<>();
        for (Map.Entry<String, String> entry : properties.getElasticsearch().getDefaultHeaders().entrySet()) {
            if (StringUtils.hasText(entry.getKey()) && StringUtils.hasText(entry.getValue())) {
                headers.add(new BasicHeader(entry.getKey().trim(), entry.getValue().trim()));
            }
        }
        if (StringUtils.hasText(properties.getElasticsearch().getApiKey())) {
            headers.add(new BasicHeader("Authorization", "ApiKey " + properties.getElasticsearch().getApiKey().trim()));
        }
        return headers.toArray(Header[]::new);
    }

    /**
     * 解析基础认证提供器。
     *
     * @param properties 搜索模块配置
     * @return 基础认证提供器
     */
    private CredentialsProvider resolveCredentialsProvider(SearchProperties properties) {
        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(
                AuthScope.ANY,
                new UsernamePasswordCredentials(
                        properties.getElasticsearch().getUsername().trim(),
                        properties.getElasticsearch().getPassword()
                )
        );
        return credentialsProvider;
    }
}
