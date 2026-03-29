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
package com.getboot.wechat.support.miniapp;

import cn.binarywang.wx.miniapp.api.WxMaService;
import com.getboot.wechat.api.miniapp.WechatMiniProgramNativeServices;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * 微信小程序服务访问门面默认实现。
 *
 * <p>统一封装多 appId 场景下的小程序服务读取入口。</p>
 *
 * @author qiheng
 */
public class DefaultWechatMiniProgramServices implements WechatMiniProgramNativeServices {

    private final Map<String, WxMaService> services;

    public DefaultWechatMiniProgramServices(Map<String, WxMaService> services) {
        this.services = Collections.unmodifiableMap(new LinkedHashMap<>(services));
    }

    @Override
    public boolean contains(String appId) {
        return services.containsKey(appId);
    }

    @Override
    public Set<String> appIds() {
        return services.keySet();
    }

    @Override
    public WxMaService getNativeRequired(String appId) {
        WxMaService service = services.get(appId);
        Assert.notNull(service, "No WeChat mini program service configured for appId: " + appId);
        return service;
    }

    @Override
    public Map<String, WxMaService> asNativeMap() {
        return services;
    }
}
