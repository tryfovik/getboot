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
package com.getboot.auth.spi;

/**
 * Sa-Token WebFlux 认证校验器。
 *
 * <p>业务方可通过实现该扩展点，自定义响应式入口的登录态、角色或权限校验逻辑。</p>
 *
 * @author qiheng
 */
public interface SaTokenWebFluxAuthChecker {

    /**
     * 执行当前请求的认证校验。
     */
    void check();
}
