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
package com.getboot.rpc.api.resolver;

import java.util.Optional;

/**
 * RPC 调用方密钥解析器。
 *
 * <p>用于根据调用方应用标识获取签名密钥。</p>
 *
 * @author qiheng
 */
public interface RpcCallerSecretResolver {

    Optional<String> resolve(String callerAppId);
}
