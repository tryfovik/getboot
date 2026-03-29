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
package com.getboot.wechat.api.officialaccount;

import java.util.Set;

/**
 * 微信服务号服务访问门面。
 *
 * <p>对外提供稳定的 appId 级服务发现能力，不直接强制业务依赖底层 SDK 类型。</p>
 *
 * @author qiheng
 */
public interface WechatOfficialAccountServices {

    /**
     * 判断指定 appId 是否已配置。
     *
     * @param appId 服务号 appId
     * @return 是否存在
     */
    boolean contains(String appId);

    /**
     * 获取当前已配置的服务号 appId 集合。
     *
     * @return appId 集合
     */
    Set<String> appIds();
}
