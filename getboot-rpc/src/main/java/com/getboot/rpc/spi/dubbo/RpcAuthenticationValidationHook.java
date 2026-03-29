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

/**
 * RPC 认证扩展校验钩子。
 *
 * <p>业务方可通过注册该类型 Bean，在默认认证通过后追加自定义校验逻辑。</p>
 *
 * @author qiheng
 */
@FunctionalInterface
public interface RpcAuthenticationValidationHook {

    /**
     * 执行扩展校验。
     *
     * @param context RPC 认证校验上下文
     */
    void validate(RpcAuthenticationValidationContext context);
}
