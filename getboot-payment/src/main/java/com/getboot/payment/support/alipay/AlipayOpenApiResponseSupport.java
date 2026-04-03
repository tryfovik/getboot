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
package com.getboot.payment.support.alipay;

import com.alipay.easysdk.util.generic.models.AlipayOpenApiGenericResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.getboot.exception.api.exception.BusinessException;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 支付宝 OpenAPI 泛化响应辅助工具。
 *
 * @author qiheng
 */
public final class AlipayOpenApiResponseSupport {

    /**
     * 共享 JSON 解析器。
     */
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * 工具类不允许实例化。
     */
    private AlipayOpenApiResponseSupport() {
    }

    /**
     * 校验泛化响应是否成功。
     *
     * @param response 响应对象
     * @param message  失败消息
     */
    public static void ensureSuccess(AlipayOpenApiGenericResponse response, String message) {
        if (response != null && "10000".equals(response.getCode())) {
            return;
        }
        throw new BusinessException(message + ": "
                + AlipayResponseSupport.firstNonBlank(response == null ? null : response.getCode(), "unknown")
                + " "
                + AlipayResponseSupport.firstNonBlank(response == null ? null : response.getMsg(), "")
                + " "
                + AlipayResponseSupport.firstNonBlank(response == null ? null : response.getSubCode(), "")
                + " "
                + AlipayResponseSupport.firstNonBlank(response == null ? null : response.getSubMsg(), ""));
    }

    /**
     * 提取业务响应节点。
     *
     * @param method   OpenAPI 方法名
     * @param httpBody 原始响应体
     * @return 业务节点
     */
    public static JsonNode responseNode(String method, String httpBody) {
        if (!StringUtils.hasText(method) || !StringUtils.hasText(httpBody)) {
            throw new BusinessException("Alipay OpenAPI response body must not be blank");
        }
        try {
            JsonNode root = OBJECT_MAPPER.readTree(httpBody);
            JsonNode responseNode = root.path(method.replace('.', '_') + "_response");
            if (responseNode.isMissingNode() || responseNode.isNull()) {
                throw new BusinessException("Alipay OpenAPI response node is missing for method: " + method);
            }
            return responseNode;
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException("Failed to parse Alipay OpenAPI response: " + ex.getMessage());
        }
    }

    /**
     * 提取字符串字段。
     *
     * @param node  业务节点
     * @param field 字段名
     * @return 文本值
     */
    public static String text(JsonNode node, String field) {
        if (node == null || !StringUtils.hasText(field)) {
            return null;
        }
        JsonNode value = node.path(field);
        return value.isMissingNode() || value.isNull() ? null : value.asText();
    }

    /**
     * 提取金额字段。
     *
     * @param node  业务节点
     * @param field 字段名
     * @return 金额
     */
    public static BigDecimal decimal(JsonNode node, String field) {
        String text = text(node, field);
        return StringUtils.hasText(text) ? new BigDecimal(text) : null;
    }

    /**
     * 提取响应元数据。
     *
     * @param node 业务节点
     * @return 元数据
     */
    public static Map<String, String> extractMetadata(JsonNode node) {
        Map<String, String> metadata = new LinkedHashMap<>();
        if (node == null) {
            return metadata;
        }
        Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> entry = fields.next();
            if (entry.getValue() == null || entry.getValue().isNull()) {
                continue;
            }
            metadata.put(
                    entry.getKey(),
                    entry.getValue().isValueNode() ? entry.getValue().asText() : entry.getValue().toString()
            );
        }
        return metadata;
    }
}
