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
package com.getboot.auth.infrastructure.satoken.autoconfigure;

import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.context.SaTokenContext;
import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.StopMatchException;
import cn.dev33.satoken.reactor.filter.SaReactorFilter;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.json.JSONUtil;
import com.getboot.auth.api.accessor.CurrentUserAccessor;
import com.getboot.auth.api.properties.SaTokenWebFluxFilterProperties;
import com.getboot.auth.infrastructure.satoken.accessor.SaTokenCurrentUserAccessor;
import com.getboot.auth.infrastructure.satoken.webflux.DefaultSaTokenWebFluxAuthChecker;
import com.getboot.auth.spi.SaTokenWebFluxAuthChecker;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Sa-Token 认证自动配置。
 *
 * <p>补充认证能力层统一的当前用户访问入口。</p>
 *
 * @author qiheng
 */
@AutoConfiguration(
        afterName = {
                "cn.dev33.satoken.spring.SaTokenContextRegister",
                "cn.dev33.satoken.reactor.spring.SaTokenContextRegister"
        },
        beforeName = "cn.dev33.satoken.spring.SaBeanInject"
)
@ConditionalOnClass(StpUtil.class)
@EnableConfigurationProperties(SaTokenWebFluxFilterProperties.class)
public class SaTokenAuthAutoConfiguration {

    /**
     * 注册当前用户访问器。
     *
     * @return 当前用户访问器
     */
    @Bean
    @ConditionalOnMissingBean
    public CurrentUserAccessor currentUserAccessor() {
        return new SaTokenCurrentUserAccessor();
    }

    /**
     * 在响应式应用中优先选择 Reactor 上下文，避免双栈依赖并存时产生歧义。
     *
     * @param context Reactor 上下文
     * @return 首选上下文
     */
    @Bean("getbootPreferredSaTokenContext")
    @Primary
    @ConditionalOnWebApplication(type = Type.REACTIVE)
    @ConditionalOnBean(name = "getSaTokenContextForSpringReactor")
    public SaTokenContext preferredReactiveSaTokenContext(
            @Qualifier("getSaTokenContextForSpringReactor") SaTokenContext context) {
        return context;
    }

    /**
     * 在 Servlet 应用中优先选择 Jakarta Servlet 上下文，避免双栈依赖并存时产生歧义。
     *
     * @param context Servlet 上下文
     * @return 首选上下文
     */
    @Bean("getbootPreferredSaTokenContext")
    @Primary
    @ConditionalOnWebApplication(type = Type.SERVLET)
    @ConditionalOnBean(name = "getSaTokenContextForSpringInJakartaServlet")
    public SaTokenContext preferredServletSaTokenContext(
            @Qualifier("getSaTokenContextForSpringInJakartaServlet") SaTokenContext context) {
        return context;
    }

    /**
     * 注册默认的 WebFlux 认证校验器。
     *
     * @return 默认认证校验器
     */
    @Bean
    @ConditionalOnClass(SaReactorFilter.class)
    @ConditionalOnWebApplication(type = Type.REACTIVE)
    @ConditionalOnMissingBean
    public SaTokenWebFluxAuthChecker saTokenWebFluxAuthChecker() {
        return new DefaultSaTokenWebFluxAuthChecker();
    }

    /**
     * 注册响应式 Sa-Token 认证过滤器。
     *
     * @param properties 认证过滤配置
     * @param authChecker 认证校验器
     * @return Sa-Token 响应式过滤器
     */
    @Bean
    @ConditionalOnClass(SaReactorFilter.class)
    @ConditionalOnWebApplication(type = Type.REACTIVE)
    @ConditionalOnProperty(prefix = "getboot.auth.satoken.webflux.filter", name = "enabled", havingValue = "true")
    @ConditionalOnMissingBean(SaReactorFilter.class)
    public SaReactorFilter saTokenWebFluxFilter(SaTokenWebFluxFilterProperties properties,
                                                SaTokenWebFluxAuthChecker authChecker) {
        SaReactorFilter filter = new SaReactorFilter();
        filter.setIncludeList(sanitizePatterns(properties.getIncludePaths(), List.of("/**")));
        filter.setExcludeList(sanitizePatterns(properties.getExcludePaths(), List.of()));
        filter.setBeforeAuth(ignored -> skipOptionsRequest(properties));
        filter.setAuth(ignored -> authChecker.check());
        filter.setError(throwable -> buildAuthFailureBody(properties, throwable));
        return filter;
    }

    /**
     * 清理并回填路径匹配规则。
     *
     * @param patterns 原始规则
     * @param defaults 默认规则
     * @return 清理后的规则列表
     */
    private List<String> sanitizePatterns(List<String> patterns, List<String> defaults) {
        List<String> sanitized = patterns == null ? List.of() : patterns.stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .toList();
        return sanitized.isEmpty() ? defaults : sanitized;
    }

    /**
     * 对预检请求直接放行，避免网关入口在 CORS 协商阶段提前拦截。
     *
     * @param properties 认证过滤配置
     */
    private void skipOptionsRequest(SaTokenWebFluxFilterProperties properties) {
        String requestMethod = SaHolder.getRequest().getMethod();
        if (properties.isSkipOptionsRequest() && "OPTIONS".equalsIgnoreCase(requestMethod)) {
            throw new StopMatchException();
        }
    }

    /**
     * 构造认证失败响应体。
     *
     * @param properties 认证过滤配置
     * @param throwable 当前异常
     * @return JSON 响应体
     */
    private String buildAuthFailureBody(SaTokenWebFluxFilterProperties properties, Throwable throwable) {
        FailureDescriptor failureDescriptor = resolveFailureDescriptor(properties, throwable);
        if (StringUtils.hasText(properties.getContentType())) {
            SaHolder.getResponse().setHeader("Content-Type", properties.getContentType());
        }
        SaHolder.getResponse().setStatus(failureDescriptor.status());
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", "fail");
        body.put("code", failureDescriptor.code());
        body.put("message", failureDescriptor.message());
        return JSONUtil.toJsonStr(body);
    }

    /**
     * 解析认证失败的返回规范。
     *
     * @param properties 认证过滤配置
     * @param throwable 当前异常
     * @return 失败描述
     */
    private FailureDescriptor resolveFailureDescriptor(SaTokenWebFluxFilterProperties properties, Throwable throwable) {
        if (throwable instanceof NotPermissionException) {
            return new FailureDescriptor(
                    properties.getForbiddenStatus(),
                    properties.getForbiddenCode(),
                    properties.getForbiddenMessage()
            );
        }
        if (throwable instanceof NotLoginException) {
            return new FailureDescriptor(
                    properties.getUnauthorizedStatus(),
                    properties.getUnauthorizedCode(),
                    properties.getUnauthorizedMessage()
            );
        }
        return new FailureDescriptor(
                properties.getForbiddenStatus(),
                properties.getForbiddenCode(),
                properties.getForbiddenMessage()
        );
    }

    /**
     * 认证失败描述。
     *
     * @param status HTTP 状态码
     * @param code 业务码
     * @param message 返回消息
     */
    private record FailureDescriptor(int status, int code, String message) {
    }
}
