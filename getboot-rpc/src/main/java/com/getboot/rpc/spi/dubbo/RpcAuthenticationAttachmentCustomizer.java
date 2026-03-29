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

import java.util.Map;

/**
 * RPC 认证附件定制器。
 *
 * <p>业务方可通过注册该类型 Bean，在消费端发起调用前补充或调整认证附件。</p>
 *
 * @author qiheng
 */
@FunctionalInterface
public interface RpcAuthenticationAttachmentCustomizer {

    /**
     * 定制认证附件。
     *
     * @param attachments 当前待写入的附件
     * @param invocation Dubbo 调用信息
     * @param rpcSecurityProperties RPC 安全配置
     */
    void customize(
            Map<String, String> attachments,
            Invocation invocation,
            RpcSecurityProperties rpcSecurityProperties);
}
