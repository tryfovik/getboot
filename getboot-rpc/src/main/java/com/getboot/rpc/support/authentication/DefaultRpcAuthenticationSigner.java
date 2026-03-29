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

    @Override
    public String sign(String appId, String appSecret, String serviceName, String methodName, long timestamp) {
        return RpcRequestSigner.sign(appId, appSecret, serviceName, methodName, timestamp);
    }

    @Override
    public boolean matches(String expectedSignature, String actualSignature) {
        return RpcRequestSigner.matches(expectedSignature, actualSignature);
    }
}
