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
package com.getboot.webhook.infrastructure.autoconfigure;

import com.getboot.limiter.api.limiter.RateLimiter;
import com.getboot.limiter.infrastructure.autoconfigure.LimiterAutoConfiguration;
import com.getboot.webhook.api.processor.WebhookRequestProcessor;
import com.getboot.webhook.api.properties.WebhookSecurityProperties;
import com.getboot.webhook.api.resolver.AppSecretResolver;
import com.getboot.webhook.infrastructure.servlet.filter.CachingRequestBodyFilter;
import com.getboot.webhook.spi.WebhookRequestValidationHook;
import com.getboot.webhook.support.processor.DefaultWebhookRequestProcessor;
import com.getboot.webhook.support.resolver.PropertiesAppSecretResolver;
import com.getboot.webhook.support.validator.WebhookRequestValidator;
import com.getboot.idempotency.spi.IdempotencyStore;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;

/**
 * Webhook 安全处理自动配置。
 *
 * <p>负责注册请求体缓存过滤器、应用密钥解析器、请求校验器与统一处理器。</p>
 *
 * @author qiheng
 */
@AutoConfiguration
@AutoConfigureAfter({
        RedisAutoConfiguration.class,
        LimiterAutoConfiguration.class
})
@EnableConfigurationProperties(WebhookSecurityProperties.class)
@ConditionalOnProperty(prefix = "getboot.webhook.security", name = "enabled", havingValue = "true")
public class WebhookAutoConfiguration {

    /**
     * Webhook 安全配置。
     */
    private final WebhookSecurityProperties webhookSecurityProperties;

    /**
     * 创建 Webhook 自动配置。
     *
     * @param webhookSecurityProperties Webhook 安全配置
     */
    public WebhookAutoConfiguration(WebhookSecurityProperties webhookSecurityProperties) {
        this.webhookSecurityProperties = webhookSecurityProperties;
    }

    /**
     * 注册应用密钥解析器。
     *
     * @return 基于配置的应用密钥解析器
     */
    @Bean
    @ConditionalOnMissingBean(AppSecretResolver.class)
    public AppSecretResolver appSecretResolver() {
        return new PropertiesAppSecretResolver(webhookSecurityProperties);
    }

    /**
     * 注册 Webhook 请求校验器。
     *
     * @param appSecretResolver 应用密钥解析器
     * @param validationHooks 扩展校验钩子集合
     * @return Webhook 请求校验器
     */
    @Bean
    @ConditionalOnMissingBean
    public WebhookRequestValidator webhookRequestValidator(
            AppSecretResolver appSecretResolver,
            ObjectProvider<WebhookRequestValidationHook> validationHooks) {
        return new WebhookRequestValidator(appSecretResolver, validationHooks.orderedStream().toList());
    }

    /**
     * 注册请求体缓存过滤器。
     *
     * <p>该过滤器会以最高优先级执行，保证后续验签逻辑能够重复读取请求体。</p>
     *
     * @return 请求体缓存过滤器注册对象
     */
    @Bean
    @ConditionalOnMissingBean(name = "cachingRequestBodyFilter")
    public FilterRegistrationBean<CachingRequestBodyFilter> cachingRequestBodyFilter() {
        FilterRegistrationBean<CachingRequestBodyFilter> registrationBean =
                new FilterRegistrationBean<>();

        registrationBean.setFilter(new CachingRequestBodyFilter());
        registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        registrationBean.addUrlPatterns("/*");

        return registrationBean;
    }

    /**
     * 注册 Webhook 请求处理器。
     *
     * @param webhookRequestValidator 请求校验器
     * @param rateLimiter 限流器
     * @param idempotencyStore 幂等存储
     * @return Webhook 请求处理器
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean({RateLimiter.class, IdempotencyStore.class})
    public WebhookRequestProcessor webhookRequestProcessor(
            WebhookRequestValidator webhookRequestValidator,
            RateLimiter rateLimiter,
            IdempotencyStore idempotencyStore
    ) {
        return new DefaultWebhookRequestProcessor(
                webhookRequestValidator,
                rateLimiter,
                idempotencyStore
        );
    }
}
