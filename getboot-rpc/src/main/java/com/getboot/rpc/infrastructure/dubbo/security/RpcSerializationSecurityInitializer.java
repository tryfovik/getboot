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
package com.getboot.rpc.infrastructure.dubbo.security;

import com.getboot.rpc.api.properties.RpcSecurityProperties;
import org.apache.dubbo.common.utils.SerializeCheckStatus;
import org.apache.dubbo.common.utils.SerializeSecurityManager;
import org.apache.dubbo.rpc.model.FrameworkModel;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.StringUtils;

import java.util.Locale;

/**
 * RPC 序列化安全初始化器。
 *
 * <p>用于在启动阶段应用 Dubbo 序列化安全校验配置。</p>
 *
 * @author qiheng
 */
public class RpcSerializationSecurityInitializer implements InitializingBean {

    /**
     * RPC 安全配置。
     */
    private final RpcSecurityProperties rpcSecurityProperties;

    /**
     * 创建 RPC 序列化安全初始化器。
     *
     * @param rpcSecurityProperties RPC 安全配置
     */
    public RpcSerializationSecurityInitializer(RpcSecurityProperties rpcSecurityProperties) {
        this.rpcSecurityProperties = rpcSecurityProperties;
    }

    /**
     * 在 Bean 初始化完成后应用 Dubbo 序列化安全配置。
     */
    @Override
    public void afterPropertiesSet() {
        RpcSecurityProperties.Serialization serialization = rpcSecurityProperties.getSerialization();
        if (!serialization.isEnabled()) {
            return;
        }

        SerializeSecurityManager serializeSecurityManager = FrameworkModel.defaultModel()
                .getBeanFactory()
                .getOrRegisterBean(SerializeSecurityManager.class);
        serializeSecurityManager.setCheckStatus(
                SerializeCheckStatus.valueOf(serialization.getCheckStatus().toUpperCase(Locale.ROOT))
        );
        serializeSecurityManager.setCheckSerializable(serialization.isCheckSerializable());
        serialization.getAllowedPrefixes().stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .forEach(serializeSecurityManager::addToAlwaysAllowed);
    }
}
