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
package com.getboot.rpc.infrastructure.dubbo.autoconfigure;

import com.getboot.rpc.api.resolver.RpcCallerSecretResolver;
import com.getboot.rpc.infrastructure.dubbo.security.RpcSerializationSecurityInitializer;
import com.getboot.rpc.spi.RpcAuthenticationSigner;
import com.getboot.rpc.support.authentication.DefaultRpcAuthenticationSigner;
import com.getboot.rpc.support.authentication.PropertiesRpcCallerSecretResolver;
import com.getboot.rpc.support.authentication.RpcSecurityConfigurationValidator;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Dubbo RPC 自动配置测试。
 *
 * @author qiheng
 */
class DubboRpcAutoConfigurationTest {

    /**
     * 测试用上下文运行器。
     */
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(DubboRpcAutoConfiguration.class));

    /**
     * 验证默认情况下会注册 RPC 安全增强相关 Bean。
     */
    @Test
    void shouldRegisterDefaultRpcSecurityBeans() {
        contextRunner
                .withPropertyValues(
                        "getboot.rpc.security.serialization.enabled=false",
                        "getboot.rpc.security.authentication.provider.credentials.consumer-app=consumer-secret"
                )
                .run(context -> {
                    assertInstanceOf(DefaultRpcAuthenticationSigner.class, context.getBean(RpcAuthenticationSigner.class));
                    assertInstanceOf(PropertiesRpcCallerSecretResolver.class, context.getBean(RpcCallerSecretResolver.class));
                    assertInstanceOf(
                            RpcSecurityConfigurationValidator.class,
                            context.getBean(RpcSecurityConfigurationValidator.class)
                    );
                    assertInstanceOf(
                            RpcSerializationSecurityInitializer.class,
                            context.getBean(RpcSerializationSecurityInitializer.class)
                    );
                    assertEquals(
                            Optional.of("consumer-secret"),
                            context.getBean(RpcCallerSecretResolver.class).resolve("consumer-app")
                    );
                });
    }

    /**
     * 验证消费方凭证只配置一半时会在启动阶段失败。
     */
    @Test
    void shouldFailWhenConsumerCredentialsConfiguredPartially() {
        contextRunner
                .withPropertyValues(
                        "getboot.rpc.security.serialization.enabled=false",
                        "getboot.rpc.security.authentication.consumer.app-id=consumer-app"
                )
                .run(context -> {
                    assertNotNull(context.getStartupFailure());
                    assertTrue(
                            context.getStartupFailure().getMessage().contains(
                                    "Both getboot.rpc.security.authentication.consumer.app-id and app-secret must be configured together."
                            )
                    );
                });
    }
}
