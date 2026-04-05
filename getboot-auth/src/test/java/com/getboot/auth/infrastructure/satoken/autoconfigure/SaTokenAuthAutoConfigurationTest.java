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

import cn.dev33.satoken.reactor.filter.SaReactorFilter;
import cn.dev33.satoken.reactor.spring.SaTokenContextRegister;
import cn.dev33.satoken.spring.SaBeanInject;
import cn.dev33.satoken.spring.SaBeanRegister;
import com.getboot.auth.api.accessor.CurrentUserAccessor;
import com.getboot.auth.infrastructure.satoken.accessor.SaTokenCurrentUserAccessor;
import com.getboot.auth.infrastructure.satoken.webflux.DefaultSaTokenWebFluxAuthChecker;
import com.getboot.auth.spi.SaTokenWebFluxAuthChecker;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.test.context.runner.ReactiveWebApplicationContextRunner;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Sa-Token 认证自动配置测试。
 *
 * @author qiheng
 */
class SaTokenAuthAutoConfigurationTest {

    /**
     * 测试用上下文运行器。
     */
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(SaTokenAuthAutoConfiguration.class));

    /**
     * 响应式测试用上下文运行器。
     */
    private final ReactiveWebApplicationContextRunner reactiveContextRunner = new ReactiveWebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    SaBeanRegister.class,
                    SaBeanInject.class,
                    SaTokenContextRegister.class,
                    SaTokenAuthAutoConfiguration.class
            ))
            .withPropertyValues("spring.main.web-application-type=reactive");

    /**
     * 验证默认情况下会注册当前用户访问器。
     */
    @Test
    void shouldRegisterCurrentUserAccessorByDefault() {
        contextRunner.run(context ->
                assertInstanceOf(SaTokenCurrentUserAccessor.class, context.getBean(CurrentUserAccessor.class))
        );
    }

    /**
     * 验证业务方自定义访问器时，自动配置会回退。
     */
    @Test
    void shouldBackOffWhenCustomCurrentUserAccessorProvided() {
        CurrentUserAccessor customAccessor = new CurrentUserAccessor() {
            @Override
            public <T> T getCurrentUser(Class<T> userType) {
                return null;
            }

            @Override
            public Long getCurrentUserId() {
                return -1L;
            }
        };

        contextRunner
                .withBean(CurrentUserAccessor.class, () -> customAccessor)
                .run(context -> {
                    assertSame(customAccessor, context.getBean(CurrentUserAccessor.class));
                    assertFalse(context.getBean(CurrentUserAccessor.class) instanceof SaTokenCurrentUserAccessor);
                });
    }

    /**
     * 验证默认会注册响应式认证校验器。
     */
    @Test
    void shouldRegisterDefaultWebFluxAuthChecker() {
        reactiveContextRunner.run(context ->
                assertInstanceOf(DefaultSaTokenWebFluxAuthChecker.class,
                        context.getBean(SaTokenWebFluxAuthChecker.class))
        );
    }

    /**
     * 验证未显式启用时不会注册响应式认证过滤器。
     */
    @Test
    void shouldNotRegisterWebFluxFilterWhenDisabled() {
        reactiveContextRunner.run(context -> org.assertj.core.api.Assertions.assertThat(context)
                .doesNotHaveBean(SaReactorFilter.class));
    }

    /**
     * 验证启用后会注册响应式认证过滤器。
     */
    @Test
    void shouldRegisterWebFluxFilterWhenEnabled() {
        reactiveContextRunner
                .withPropertyValues("getboot.auth.satoken.webflux.filter.enabled=true")
                .run(context -> org.assertj.core.api.Assertions.assertThat(context).hasSingleBean(SaReactorFilter.class));
    }

    /**
     * 验证业务方自定义认证校验器时，自动配置会回退。
     */
    @Test
    void shouldBackOffWhenCustomWebFluxAuthCheckerProvided() {
        SaTokenWebFluxAuthChecker customChecker = () -> {
        };

        reactiveContextRunner
                .withBean(SaTokenWebFluxAuthChecker.class, () -> customChecker)
                .run(context -> assertSame(customChecker, context.getBean(SaTokenWebFluxAuthChecker.class)));
    }
}
