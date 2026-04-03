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
package com.getboot.wechat.infrastructure.officialaccount.support;

import me.chanjar.weixin.common.redis.RedisTemplateWxRedisOps;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.api.impl.WxMpServiceImpl;
import me.chanjar.weixin.mp.config.impl.WxMpDefaultConfigImpl;
import me.chanjar.weixin.mp.config.impl.WxMpRedisConfigImpl;
import com.getboot.wechat.api.properties.WechatProperties;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 微信服务号服务工厂。
 *
 * <p>负责按 appId 初始化服务号 SDK 服务实例，并按需接入 Redis token 存储。</p>
 *
 * @author qiheng
 */
public class WechatOfficialAccountServiceFactory {

    /**
     * 服务号 token 在 Redis 中的 key 前缀。
     */
    private static final String OFFICIAL_ACCOUNT_TOKEN_PREFIX = "official_account:";

    /**
     * 批量创建微信服务号服务。
     *
     * @param appCredentialGroup 服务号应用凭证分组
     * @param redisOps Redis 操作适配器
     * @return 服务号服务映射，key 为 appId
     */
    public Map<String, WxMpService> createServices(
            WechatProperties.AppCredentialGroup appCredentialGroup,
            RedisTemplateWxRedisOps redisOps) {
        Map<String, WxMpService> services = new ConcurrentHashMap<>();
        if (appCredentialGroup == null || !appCredentialGroup.hasApps()) {
            return services;
        }
        appCredentialGroup.getApps().forEach((appId, appSecret) -> services.put(appId, createService(appId, appSecret, redisOps)));
        return services;
    }

    /**
     * 创建单个服务号原生服务。
     *
     * @param appId 服务号 appId
     * @param appSecret 服务号 appSecret
     * @param redisOps Redis 操作适配器
     * @return 服务号原生服务
     */
    private WxMpService createService(String appId, String appSecret, RedisTemplateWxRedisOps redisOps) {
        WxMpService officialAccountService = new WxMpServiceImpl();
        if (redisOps != null) {
            WxMpRedisConfigImpl config = new WxMpRedisConfigImpl(redisOps, OFFICIAL_ACCOUNT_TOKEN_PREFIX);
            config.setAppId(appId);
            config.setSecret(appSecret);
            config.useStableAccessToken(true);
            officialAccountService.setWxMpConfigStorage(config);
            return officialAccountService;
        }

        WxMpDefaultConfigImpl config = new WxMpDefaultConfigImpl();
        config.setAppId(appId);
        config.setSecret(appSecret);
        config.useStableAccessToken(true);
        officialAccountService.setWxMpConfigStorage(config);
        return officialAccountService;
    }
}
