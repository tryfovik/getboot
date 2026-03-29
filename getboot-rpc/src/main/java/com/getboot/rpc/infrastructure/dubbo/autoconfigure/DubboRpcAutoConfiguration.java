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

import com.getboot.rpc.api.properties.RpcSecurityProperties;
import com.getboot.rpc.api.properties.RpcTraceProperties;
import com.getboot.rpc.api.resolver.RpcCallerSecretResolver;
import com.getboot.rpc.infrastructure.dubbo.security.RpcSerializationSecurityInitializer;
import com.getboot.rpc.spi.RpcAuthenticationSigner;
import com.getboot.rpc.support.authentication.DefaultRpcAuthenticationSigner;
import com.getboot.rpc.support.authentication.PropertiesRpcCallerSecretResolver;
import com.getboot.rpc.support.authentication.RpcSecurityConfigurationValidator;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * RPC 安全增强自动配置。
 *
 * @author qiheng
 */
@AutoConfiguration
@ConditionalOnClass(name = "org.apache.dubbo.rpc.Filter")
@EnableConfigurationProperties({RpcSecurityProperties.class, RpcTraceProperties.class})
public class DubboRpcAutoConfiguration {

    /**
     * 注册默认 RPC 认证签名器。
     *
     * @return RPC 认证签名器
     */
    @Bean
    @ConditionalOnMissingBean
    public RpcAuthenticationSigner rpcAuthenticationSigner() {
        return new DefaultRpcAuthenticationSigner();
    }

    /**
     * 注册调用方密钥解析器。
     *
     * @param rpcSecurityProperties RPC 安全配置
     * @return 调用方密钥解析器
     */
    @Bean
    @ConditionalOnMissingBean(RpcCallerSecretResolver.class)
    public RpcCallerSecretResolver rpcCallerSecretResolver(RpcSecurityProperties rpcSecurityProperties) {
        return new PropertiesRpcCallerSecretResolver(rpcSecurityProperties);
    }

    /**
     * 注册 RPC 安全配置校验器。
     *
     * @param rpcSecurityProperties RPC 安全配置
     * @return 配置校验器
     */
    @Bean
    @ConditionalOnMissingBean
    public RpcSecurityConfigurationValidator rpcSecurityConfigurationValidator(
            RpcSecurityProperties rpcSecurityProperties) {
        return new RpcSecurityConfigurationValidator(rpcSecurityProperties);
    }

    /**
     * 注册 Dubbo 序列化安全初始化器。
     *
     * @param rpcSecurityProperties RPC 安全配置
     * @return 序列化安全初始化器
     */
    @Bean
    @ConditionalOnMissingBean
    public RpcSerializationSecurityInitializer rpcSerializationSecurityInitializer(
            RpcSecurityProperties rpcSecurityProperties) {
        return new RpcSerializationSecurityInitializer(rpcSecurityProperties);
    }
}
