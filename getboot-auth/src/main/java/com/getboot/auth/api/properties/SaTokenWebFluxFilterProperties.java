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
package com.getboot.auth.api.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Sa-Token WebFlux 认证过滤配置。
 *
 * <p>用于统一定义响应式入口的认证匹配规则、预检请求豁免策略和认证失败响应。</p>
 *
 * @author qiheng
 */
@ConfigurationProperties(prefix = "getboot.auth.satoken.webflux.filter")
public class SaTokenWebFluxFilterProperties {

    /**
     * 是否启用响应式认证过滤器。
     */
    private boolean enabled;

    /**
     * 需要执行认证校验的路径模式。
     */
    private List<String> includePaths = new ArrayList<>(List.of("/**"));

    /**
     * 需要跳过认证校验的路径模式。
     */
    private List<String> excludePaths = new ArrayList<>();

    /**
     * 是否跳过 OPTIONS 预检请求。
     */
    private boolean skipOptionsRequest = true;

    /**
     * 未登录时返回的 HTTP 状态码。
     */
    private int unauthorizedStatus = 401;

    /**
     * 未登录时返回的业务码。
     */
    private int unauthorizedCode = 401;

    /**
     * 未登录时返回的消息。
     */
    private String unauthorizedMessage = "Unauthorized";

    /**
     * 无权限时返回的 HTTP 状态码。
     */
    private int forbiddenStatus = 403;

    /**
     * 无权限时返回的业务码。
     */
    private int forbiddenCode = 403;

    /**
     * 无权限时返回的消息。
     */
    private String forbiddenMessage = "Forbidden";

    /**
     * 认证失败响应的内容类型。
     */
    private String contentType = "application/json;charset=UTF-8";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public List<String> getIncludePaths() {
        return includePaths;
    }

    public void setIncludePaths(List<String> includePaths) {
        this.includePaths = includePaths;
    }

    public List<String> getExcludePaths() {
        return excludePaths;
    }

    public void setExcludePaths(List<String> excludePaths) {
        this.excludePaths = excludePaths;
    }

    public boolean isSkipOptionsRequest() {
        return skipOptionsRequest;
    }

    public void setSkipOptionsRequest(boolean skipOptionsRequest) {
        this.skipOptionsRequest = skipOptionsRequest;
    }

    public int getUnauthorizedStatus() {
        return unauthorizedStatus;
    }

    public void setUnauthorizedStatus(int unauthorizedStatus) {
        this.unauthorizedStatus = unauthorizedStatus;
    }

    public int getUnauthorizedCode() {
        return unauthorizedCode;
    }

    public void setUnauthorizedCode(int unauthorizedCode) {
        this.unauthorizedCode = unauthorizedCode;
    }

    public String getUnauthorizedMessage() {
        return unauthorizedMessage;
    }

    public void setUnauthorizedMessage(String unauthorizedMessage) {
        this.unauthorizedMessage = unauthorizedMessage;
    }

    public int getForbiddenStatus() {
        return forbiddenStatus;
    }

    public void setForbiddenStatus(int forbiddenStatus) {
        this.forbiddenStatus = forbiddenStatus;
    }

    public int getForbiddenCode() {
        return forbiddenCode;
    }

    public void setForbiddenCode(int forbiddenCode) {
        this.forbiddenCode = forbiddenCode;
    }

    public String getForbiddenMessage() {
        return forbiddenMessage;
    }

    public void setForbiddenMessage(String forbiddenMessage) {
        this.forbiddenMessage = forbiddenMessage;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
}
