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
package com.getboot.wechat.api.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * 微信平台接入配置。
 *
 * <p>用于维护小程序与服务号的应用凭证。</p>
 *
 * @author qiheng
 */
@Data
@ConfigurationProperties(prefix = "getboot.wechat")
public class WechatProperties {

    /**
     * 微信小程序配置。
     */
    private AppCredentialGroup miniProgram = new AppCredentialGroup();

    /**
     * 微信公众号 / 服务号配置。
     */
    private AppCredentialGroup officialAccount = new AppCredentialGroup();

    /**
     * 判断是否至少配置了一个小程序应用。
     *
     * @return 是否存在小程序配置
     */
    public boolean hasMiniProgramApps() {
        return miniProgram != null && miniProgram.hasApps();
    }

    /**
     * 判断是否至少配置了一个服务号应用。
     *
     * @return 是否存在服务号配置
     */
    public boolean hasOfficialAccountApps() {
        return officialAccount != null && officialAccount.hasApps();
    }

    /**
     * 平台应用配置分组。
     */
    @Data
    public static class AppCredentialGroup {
        /**
         * 应用凭证列表，key 为 appId，value 为 appSecret。
         */
        private Map<String, String> apps = new HashMap<>();

        /**
         * 判断当前分组下是否存在应用配置。
         *
         * @return 是否存在应用
         */
        public boolean hasApps() {
            return apps != null && !apps.isEmpty();
        }
    }
}
