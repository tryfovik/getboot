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
package com.getboot.auth.api.accessor;

/**
 * 当前用户访问器。
 *
 * <p>定义认证能力层统一的当前用户获取入口。</p>
 *
 * @author qiheng
 */
public interface CurrentUserAccessor {

    /**
     * 获取当前登录用户对象。
     *
     * @param userType 目标用户类型
     * @param <T> 用户对象类型
     * @return 当前登录用户
     */
    <T> T getCurrentUser(Class<T> userType);

    /**
     * 获取当前登录用户 ID。
     *
     * @return 当前登录用户 ID
     */
    Long getCurrentUserId();
}
