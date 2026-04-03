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
package com.getboot.wechat.infrastructure.autoconfigure;

import cn.binarywang.wx.miniapp.api.WxMaService;
import com.getboot.wechat.api.miniapp.WechatMiniProgramNativeServices;
import com.getboot.wechat.api.officialaccount.WechatOfficialAccountNativeServices;
import com.getboot.wechat.api.properties.WechatProperties;
import com.getboot.wechat.infrastructure.miniapp.support.WechatMiniProgramServiceFactory;
import com.getboot.wechat.infrastructure.officialaccount.support.WechatOfficialAccountServiceFactory;
import com.getboot.wechat.support.miniapp.DefaultWechatMiniProgramServices;
import com.getboot.wechat.support.officialaccount.DefaultWechatOfficialAccountServices;
import me.chanjar.weixin.common.redis.RedisTemplateWxRedisOps;
import me.chanjar.weixin.mp.api.WxMpService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Map;

/**
 * 微信能力自动配置类。
 *
 * <p>用于初始化小程序与服务号服务实例，并按需接入 Redis 存储。</p>
 *
 * @author qiheng
 */
@AutoConfiguration
@EnableConfigurationProperties(WechatProperties.class)
public class WechatAutoConfiguration {

    /**
     * 微信能力配置。
     */
    private final WechatProperties wechatProperties;

    /**
     * 创建微信自动配置。
     *
     * @param wechatProperties 微信能力配置
     */
    public WechatAutoConfiguration(WechatProperties wechatProperties) {
        this.wechatProperties = wechatProperties;
    }

    /**
     * 注册微信 Redis 存储适配器。
     *
     * @param redisTemplate Redis 字符串模板
     * @return Redis 操作适配器
     */
    @Bean
    @ConditionalOnBean(StringRedisTemplate.class)
    @ConditionalOnMissingBean
    public RedisTemplateWxRedisOps redisTemplateWxRedisOps(StringRedisTemplate redisTemplate) {
        return new RedisTemplateWxRedisOps(redisTemplate);
    }

    /**
     * 注册小程序服务工厂。
     *
     * @return 小程序服务工厂
     */
    @Bean
    @ConditionalOnMissingBean
    public WechatMiniProgramServiceFactory wechatMiniProgramServiceFactory() {
        return new WechatMiniProgramServiceFactory();
    }

    /**
     * 注册服务号服务工厂。
     *
     * @return 服务号服务工厂
     */
    @Bean
    @ConditionalOnMissingBean
    public WechatOfficialAccountServiceFactory wechatOfficialAccountServiceFactory() {
        return new WechatOfficialAccountServiceFactory();
    }

    /**
     * 初始化微信小程序服务。
     *
     * @param serviceFactory 小程序服务工厂
     * @return 小程序服务映射，key 为 appId
     */
    @Bean
    @ConditionalOnMissingBean(name = "wxMaServices")
    public Map<String, WxMaService> wxMaServices(WechatMiniProgramServiceFactory serviceFactory) {
        return serviceFactory.createServices(wechatProperties.getMiniProgram());
    }

    /**
     * 注册微信小程序原生服务门面。
     *
     * @param wxMaServices 小程序服务映射
     * @return 小程序原生服务门面
     */
    @Bean
    @ConditionalOnMissingBean
    public WechatMiniProgramNativeServices wechatMiniProgramServices(Map<String, WxMaService> wxMaServices) {
        return new DefaultWechatMiniProgramServices(wxMaServices);
    }

    /**
     * 初始化微信服务号服务。
     *
     * <p>优先使用 Redis 存储 token；如果业务方未提供 Redis，则回退到内存存储。</p>
     *
     * @param redisOpsProvider Redis 操作适配器提供方
     * @param serviceFactory 服务号服务工厂
     * @return 服务号服务映射，key 为 appId
     */
    @Bean
    @ConditionalOnMissingBean(name = "wxMpServices")
    public Map<String, WxMpService> wxMpServices(
            ObjectProvider<RedisTemplateWxRedisOps> redisOpsProvider,
            WechatOfficialAccountServiceFactory serviceFactory) {
        return serviceFactory.createServices(
                wechatProperties.getOfficialAccount(),
                redisOpsProvider.getIfAvailable()
        );
    }

    /**
     * 注册微信服务号原生服务门面。
     *
     * @param wxMpServices 服务号服务映射
     * @return 服务号原生服务门面
     */
    @Bean
    @ConditionalOnMissingBean
    public WechatOfficialAccountNativeServices wechatOfficialAccountServices(Map<String, WxMpService> wxMpServices) {
        return new DefaultWechatOfficialAccountServices(wxMpServices);
    }
}
