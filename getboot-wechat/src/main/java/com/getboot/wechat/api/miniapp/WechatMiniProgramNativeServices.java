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
package com.getboot.wechat.api.miniapp;

import cn.binarywang.wx.miniapp.api.WxMaService;

import java.util.Map;

/**
 * 微信小程序原生 SDK 访问门面。
 *
 * <p>仅在业务必须直接使用微信 SDK 能力时注入该接口。</p>
 *
 * @author qiheng
 */
public interface WechatMiniProgramNativeServices extends WechatMiniProgramServices {

    /**
     * 获取指定 appId 的小程序原生服务，不存在时抛出异常。
     *
     * @param appId 小程序 appId
     * @return 原生小程序服务
     */
    WxMaService getNativeRequired(String appId);

    /**
     * 获取全部原生小程序服务。
     *
     * @return appId -> 原生服务映射
     */
    Map<String, WxMaService> asNativeMap();
}
