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
 * RPC 链路透传附件键常量。
 *
 * <p>统一定义 Dubbo 调用链路中传递 TraceId 所使用的附件键名。</p>
 *
 * @author qiheng
 */
public final class RpcTraceAttachments {

    public static final String TRACE_ID = "getboot-trace-id";

    private RpcTraceAttachments() {
    }
}
