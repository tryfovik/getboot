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

import com.getboot.rpc.spi.RpcAuthenticationSigner;

/**
 * 默认 RPC 认证签名器。
 *
 * <p>复用 GetBoot 当前的 HMAC-SHA256 签名算法实现。</p>
 *
 * @author qiheng
 */
public class DefaultRpcAuthenticationSigner implements RpcAuthenticationSigner {

    /**
     * 生成 RPC 请求签名。
     *
     * @param appId 应用标识
     * @param appSecret 应用密钥
     * @param serviceName 服务名
     * @param methodName 方法名
     * @param timestamp 时间戳
     * @return 请求签名
     */
    @Override
    public String sign(String appId, String appSecret, String serviceName, String methodName, long timestamp) {
        return RpcRequestSigner.sign(appId, appSecret, serviceName, methodName, timestamp);
    }

    /**
     * 比较两个 RPC 请求签名是否一致。
     *
     * @param expectedSignature 期望签名
     * @param actualSignature 实际签名
     * @return 签名一致时返回 {@code true}
     */
    @Override
    public boolean matches(String expectedSignature, String actualSignature) {
        return RpcRequestSigner.matches(expectedSignature, actualSignature);
    }
}
