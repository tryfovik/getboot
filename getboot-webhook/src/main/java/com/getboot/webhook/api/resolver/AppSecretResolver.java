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
package com.getboot.webhook.api.resolver;

/**
 * 应用密钥解析器。
 *
 * <p>用于根据调用方应用标识解析对应签名密钥。</p>
 *
 * @author qiheng
 */
public interface AppSecretResolver {

    /**
     * 根据应用标识解析对应签名密钥。
     *
     * @param appKey 调用方应用标识
     * @return 调用方签名密钥
     */
    String getAppSecret(String appKey);
}
