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
package com.getboot.wechat.infrastructure.miniapp.support;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.api.impl.WxMaServiceImpl;
import cn.binarywang.wx.miniapp.config.impl.WxMaDefaultConfigImpl;
import com.getboot.wechat.api.properties.WechatProperties;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 微信小程序服务工厂。
 *
 * <p>负责按 appId 初始化小程序 SDK 服务实例。</p>
 *
 * @author qiheng
 */
public class WechatMiniProgramServiceFactory {

    /**
     * 批量创建微信小程序服务。
     *
     * @param appCredentialGroup 小程序应用凭证分组
     * @return 小程序服务映射，key 为 appId
     */
    public Map<String, WxMaService> createServices(WechatProperties.AppCredentialGroup appCredentialGroup) {
        Map<String, WxMaService> services = new ConcurrentHashMap<>();
        if (appCredentialGroup == null || !appCredentialGroup.hasApps()) {
            return services;
        }
        appCredentialGroup.getApps().forEach((appId, appSecret) -> services.put(appId, createService(appId, appSecret)));
        return services;
    }

    /**
     * 创建单个小程序原生服务。
     *
     * @param appId 小程序 appId
     * @param appSecret 小程序 appSecret
     * @return 小程序原生服务
     */
    private WxMaService createService(String appId, String appSecret) {
        WxMaService miniProgramService = new WxMaServiceImpl();
        WxMaDefaultConfigImpl config = new WxMaDefaultConfigImpl();
        config.setAppid(appId);
        config.setSecret(appSecret);
        config.useStableAccessToken(true);
        miniProgramService.setWxMaConfig(config);
        return miniProgramService;
    }
}
