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
import com.getboot.rpc.api.resolver.RpcCallerSecretResolver;
import org.springframework.util.StringUtils;

import java.util.Optional;

/**
 * 基于配置项的 RPC 调用方密钥解析器。
 *
 * <p>从 RPC 安全配置中解析调用方应用标识对应的密钥。</p>
 *
 * @author qiheng
 */
public class PropertiesRpcCallerSecretResolver implements RpcCallerSecretResolver {

    private final RpcSecurityProperties rpcSecurityProperties;

    public PropertiesRpcCallerSecretResolver(RpcSecurityProperties rpcSecurityProperties) {
        this.rpcSecurityProperties = rpcSecurityProperties;
    }

    @Override
    public Optional<String> resolve(String callerAppId) {
        if (!StringUtils.hasText(callerAppId)) {
            return Optional.empty();
        }
        return Optional.ofNullable(rpcSecurityProperties.getAuthentication().getProvider().getCredentials().get(callerAppId))
                .filter(StringUtils::hasText);
    }
}
