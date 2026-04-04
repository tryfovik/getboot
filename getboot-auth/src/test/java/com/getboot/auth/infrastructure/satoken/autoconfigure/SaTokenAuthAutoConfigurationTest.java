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

import com.getboot.auth.api.accessor.CurrentUserAccessor;
import com.getboot.auth.infrastructure.satoken.accessor.SaTokenCurrentUserAccessor;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

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
}
