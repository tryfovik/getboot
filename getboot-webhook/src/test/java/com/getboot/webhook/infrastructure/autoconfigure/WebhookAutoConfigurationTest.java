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

import com.getboot.idempotency.api.model.IdempotencyRecord;
import com.getboot.idempotency.spi.IdempotencyStore;
import com.getboot.limiter.api.limiter.RateLimiter;
import com.getboot.webhook.api.processor.WebhookRequestProcessor;
import com.getboot.webhook.api.properties.WebhookSecurityProperties;
import com.getboot.webhook.api.resolver.AppSecretResolver;
import com.getboot.webhook.infrastructure.servlet.filter.CachingRequestBodyFilter;
import com.getboot.webhook.support.processor.DefaultWebhookRequestProcessor;
import com.getboot.webhook.support.resolver.PropertiesAppSecretResolver;
import com.getboot.webhook.support.validator.WebhookRequestValidator;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.core.Ordered;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Webhook 自动配置测试。
 *
 * @author qiheng
 */
class WebhookAutoConfigurationTest {

    /**
     * 测试用上下文运行器。
     */
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(WebhookAutoConfiguration.class));

    /**
     * 验证安全能力开启且底层 Bean 就绪时会注册默认处理器链。
     */
    @Test
    void shouldRegisterWebhookProcessorChainWhenDependenciesPresent() {
        contextRunner
                .withPropertyValues(
                        "getboot.webhook.security.enabled=true",
                        "getboot.webhook.security.credentials[0].name=demo",
                        "getboot.webhook.security.credentials[0].app-key=demo-app",
                        "getboot.webhook.security.credentials[0].app-secret= demo-secret "
                )
                .withBean(RateLimiter.class, () -> (key, limit, windowSize) -> true)
                .withBean(IdempotencyStore.class, InMemoryIdempotencyStore::new)
                .run(context -> {
                    assertInstanceOf(PropertiesAppSecretResolver.class, context.getBean(AppSecretResolver.class));
                    assertInstanceOf(WebhookRequestValidator.class, context.getBean(WebhookRequestValidator.class));
                    assertInstanceOf(DefaultWebhookRequestProcessor.class, context.getBean(WebhookRequestProcessor.class));

                    @SuppressWarnings("unchecked")
                    FilterRegistrationBean<CachingRequestBodyFilter> registrationBean =
                            context.getBean("cachingRequestBodyFilter", FilterRegistrationBean.class);
                    assertInstanceOf(CachingRequestBodyFilter.class, registrationBean.getFilter());
                    assertEquals(Ordered.HIGHEST_PRECEDENCE, registrationBean.getOrder());

                    WebhookSecurityProperties properties = context.getBean(WebhookSecurityProperties.class);
                    assertTrue(properties.hasCredentials());
                    assertEquals("demo-app", properties.getCredentials().get(0).getAppKey());
                    assertEquals("demo-secret", context.getBean(AppSecretResolver.class).getAppSecret("demo-app"));
                });
    }

    /**
     * 验证缺少限流器或幂等存储时不会注册默认处理器，但基础校验链仍会存在。
     */
    @Test
    void shouldSkipWebhookProcessorWhenDependenciesMissing() {
        contextRunner
                .withPropertyValues(
                        "getboot.webhook.security.enabled=true",
                        "getboot.webhook.security.credentials[0].app-key=demo-app",
                        "getboot.webhook.security.credentials[0].app-secret=demo-secret"
                )
                .run(context -> {
                    assertTrue(context.containsBean("appSecretResolver"));
                    assertTrue(context.containsBean("webhookRequestValidator"));
                    assertTrue(context.containsBean("cachingRequestBodyFilter"));
                    assertFalse(context.containsBean("webhookRequestProcessor"));
                });
    }

    /**
     * 验证关闭安全能力时不会注册任何默认 Webhook Bean。
     */
    @Test
    void shouldSkipAllWebhookBeansWhenSecurityDisabled() {
        contextRunner
                .withPropertyValues("getboot.webhook.security.enabled=false")
                .run(context -> {
                    assertFalse(context.containsBean("appSecretResolver"));
                    assertFalse(context.containsBean("webhookRequestValidator"));
                    assertFalse(context.containsBean("cachingRequestBodyFilter"));
                    assertFalse(context.containsBean("webhookRequestProcessor"));
                });
    }

    /**
     * 测试用内存幂等存储。
     */
    private static final class InMemoryIdempotencyStore implements IdempotencyStore {

        /**
         * 当前测试不依赖历史记录。
         *
         * @param key 幂等键
         * @return 始终为空
         */
        @Override
        public IdempotencyRecord get(String key) {
            return null;
        }

        /**
         * 当前测试只关注 Bean 装配。
         *
         * @param key 幂等键
         * @param ttl 保留时间
         * @return 始终成功
         */
        @Override
        public boolean markProcessing(String key, Duration ttl) {
            return true;
        }

        /**
         * 当前测试不校验持久化结果。
         *
         * @param key 幂等键
         * @param result 结果
         * @param ttl 保留时间
         */
        @Override
        public void markCompleted(String key, Object result, Duration ttl) {
        }

        /**
         * 当前测试不校验删除行为。
         *
         * @param key 幂等键
         */
        @Override
        public void delete(String key) {
        }
    }
}
