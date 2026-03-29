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
package com.getboot.rpc.spi;

/**
 * RPC 认证签名器。
 *
 * <p>业务方可通过注册该类型 Bean，自定义 RPC 请求的签名与验签算法。</p>
 *
 * @author qiheng
 */
public interface RpcAuthenticationSigner {

    /**
     * 生成签名。
     *
     * @param appId 应用标识
     * @param appSecret 应用密钥
     * @param serviceName 服务名
     * @param methodName 方法名
     * @param timestamp 时间戳
     * @return 签名结果
     */
    String sign(String appId, String appSecret, String serviceName, String methodName, long timestamp);

    /**
     * 比较签名。
     *
     * @param expectedSignature 期望签名
     * @param actualSignature 实际签名
     * @return 是否匹配
     */
    boolean matches(String expectedSignature, String actualSignature);
}
