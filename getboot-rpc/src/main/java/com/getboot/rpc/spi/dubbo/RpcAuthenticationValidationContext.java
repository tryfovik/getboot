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
package com.getboot.rpc.spi.dubbo;

import com.getboot.rpc.api.properties.RpcSecurityProperties;
import org.apache.dubbo.rpc.Invocation;

/**
 * RPC 认证校验上下文。
 *
 * <p>用于向扩展校验器暴露当前 RPC 认证过程中的核心上下文信息。</p>
 *
 * @param invocation Dubbo 调用信息
 * @param callerAppId 调用方应用标识
 * @param timestamp 请求时间戳
 * @param signature 请求签名
 * @param rpcSecurityProperties RPC 安全配置
 * @author qiheng
 */
public record RpcAuthenticationValidationContext(
        Invocation invocation,
        String callerAppId,
        long timestamp,
        String signature,
        RpcSecurityProperties rpcSecurityProperties) {
}
