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
package com.getboot.auth.infrastructure.satoken.webflux;

import cn.dev33.satoken.stp.StpUtil;
import com.getboot.auth.spi.SaTokenWebFluxAuthChecker;

/**
 * 默认的 Sa-Token WebFlux 认证校验器。
 *
 * <p>默认仅校验当前请求是否已登录，由业务项目按需覆盖为更细粒度的角色或权限检查。</p>
 *
 * @author qiheng
 */
public class DefaultSaTokenWebFluxAuthChecker implements SaTokenWebFluxAuthChecker {

    /**
     * 校验当前请求是否已登录。
     */
    @Override
    public void check() {
        StpUtil.checkLogin();
    }
}
