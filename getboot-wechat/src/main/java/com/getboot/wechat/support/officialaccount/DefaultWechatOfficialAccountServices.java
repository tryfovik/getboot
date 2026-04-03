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
package com.getboot.wechat.support.officialaccount;

import com.getboot.wechat.api.officialaccount.WechatOfficialAccountNativeServices;
import me.chanjar.weixin.mp.api.WxMpService;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * 微信服务号服务访问门面默认实现。
 *
 * <p>统一封装多 appId 场景下的服务号服务读取入口。</p>
 *
 * @author qiheng
 */
public class DefaultWechatOfficialAccountServices implements WechatOfficialAccountNativeServices {

    /**
     * 按 appId 索引的服务号服务映射。
     */
    private final Map<String, WxMpService> services;

    /**
     * 创建默认服务号服务门面。
     *
     * @param services 服务号服务映射
     */
    public DefaultWechatOfficialAccountServices(Map<String, WxMpService> services) {
        this.services = Collections.unmodifiableMap(new LinkedHashMap<>(services));
    }

    /**
     * 判断指定 appId 是否存在对应服务。
     *
     * @param appId 服务号 appId
     * @return 是否存在对应服务
     */
    @Override
    public boolean contains(String appId) {
        return services.containsKey(appId);
    }

    /**
     * 获取当前全部已注册的服务号 appId。
     *
     * @return 服务号 appId 集合
     */
    @Override
    public Set<String> appIds() {
        return services.keySet();
    }

    /**
     * 获取指定 appId 的服务号原生服务，不存在时抛出异常。
     *
     * @param appId 服务号 appId
     * @return 服务号原生服务
     */
    @Override
    public WxMpService getNativeRequired(String appId) {
        WxMpService service = services.get(appId);
        Assert.notNull(service, "No WeChat official account service configured for appId: " + appId);
        return service;
    }

    /**
     * 获取只读的服务号原生服务映射。
     *
     * @return 服务号原生服务映射
     */
    @Override
    public Map<String, WxMpService> asNativeMap() {
        return services;
    }
}
