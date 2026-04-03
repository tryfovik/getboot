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
package com.getboot.rpc.support.authentication;

import com.getboot.rpc.api.properties.RpcSecurityProperties;
import org.springframework.beans.factory.InitializingBean;

/**
 * RPC 安全配置校验器。
 *
 * <p>用于在启动时校验 RPC 认证相关配置是否完整且合法。</p>
 *
 * @author qiheng
 */
public class RpcSecurityConfigurationValidator implements InitializingBean {

    /**
     * RPC 安全配置。
     */
    private final RpcSecurityProperties rpcSecurityProperties;

    /**
     * 创建 RPC 安全配置校验器。
     *
     * @param rpcSecurityProperties RPC 安全配置
     */
    public RpcSecurityConfigurationValidator(RpcSecurityProperties rpcSecurityProperties) {
        this.rpcSecurityProperties = rpcSecurityProperties;
    }

    /**
     * 在 Bean 初始化完成后校验 RPC 安全配置。
     */
    @Override
    public void afterPropertiesSet() {
        RpcSecurityProperties.Authentication authentication = rpcSecurityProperties.getAuthentication();
        if (authentication.getAllowedClockSkewSeconds() < 0) {
            throw new IllegalStateException("getboot.rpc.security.authentication.allowed-clock-skew-seconds must be greater than or equal to 0.");
        }
        if (authentication.getConsumer().hasAnyConfiguredValue() && !authentication.getConsumer().isConfigured()) {
            throw new IllegalStateException("Both getboot.rpc.security.authentication.consumer.app-id and app-secret must be configured together.");
        }
    }
}
