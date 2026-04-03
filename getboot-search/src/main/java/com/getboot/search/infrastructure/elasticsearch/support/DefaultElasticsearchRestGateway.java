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
package com.getboot.search.infrastructure.elasticsearch.support;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NullNode;
import com.getboot.search.api.exception.SearchException;
import org.apache.http.HttpEntity;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * 默认 Elasticsearch 请求网关实现。
 *
 * @author qiheng
 */
public class DefaultElasticsearchRestGateway implements ElasticsearchRestGateway {

    /**
     * Elasticsearch RestClient。
     */
    private final RestClient restClient;

    /**
     * Jackson 映射器。
     */
    private final ObjectMapper objectMapper;

    /**
     * 构造默认 Elasticsearch 请求网关。
     *
     * @param restClient Elasticsearch RestClient
     * @param objectMapper Jackson 映射器
     */
    public DefaultElasticsearchRestGateway(RestClient restClient, ObjectMapper objectMapper) {
        this.restClient = restClient;
        this.objectMapper = objectMapper;
    }

    /**
     * 执行 Elasticsearch 请求。
     *
     * @param method 请求方法
     * @param endpoint 请求路径
     * @param parameters 请求参数
     * @param requestBody 请求体
     * @return 响应 JSON 节点
     */
    @Override
    public JsonNode execute(String method, String endpoint, Map<String, String> parameters, Object requestBody) {
        Request request = new Request(method, endpoint);
        if (parameters != null) {
            parameters.forEach((key, value) -> {
                if (StringUtils.hasText(key) && StringUtils.hasText(value)) {
                    request.addParameter(key.trim(), value.trim());
                }
            });
        }
        try {
            if (requestBody != null) {
                request.setJsonEntity(objectMapper.writeValueAsString(requestBody));
            }
            Response response = restClient.performRequest(request);
            return readResponseBody(response);
        } catch (SearchException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new SearchException("Failed to execute Elasticsearch request.", ex);
        }
    }

    /**
     * 读取响应体并转换为 JSON 节点。
     *
     * @param response HTTP 响应
     * @return 响应 JSON 节点
     * @throws Exception 解析异常
     */
    private JsonNode readResponseBody(Response response) throws Exception {
        if (response == null) {
            return NullNode.getInstance();
        }
        HttpEntity entity = response.getEntity();
        if (entity == null) {
            return NullNode.getInstance();
        }
        String body = EntityUtils.toString(entity, StandardCharsets.UTF_8);
        return StringUtils.hasText(body) ? objectMapper.readTree(body) : NullNode.getInstance();
    }
}
