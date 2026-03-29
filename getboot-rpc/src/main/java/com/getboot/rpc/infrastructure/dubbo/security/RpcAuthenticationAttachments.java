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

/**
 * RPC 认证附件键常量。
 *
 * <p>统一定义 Dubbo 调用链路中传递认证信息所使用的附件键名。</p>
 *
 * @author qiheng
 */
public final class RpcAuthenticationAttachments {

    public static final String APP_ID = "getboot-rpc-app-id";

    public static final String TIMESTAMP = "getboot-rpc-timestamp";

    public static final String SIGNATURE = "getboot-rpc-signature";

    private RpcAuthenticationAttachments() {
    }
}
