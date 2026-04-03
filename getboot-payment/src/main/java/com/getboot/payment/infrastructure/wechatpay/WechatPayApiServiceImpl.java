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
package com.getboot.payment.infrastructure.wechatpay;

import com.getboot.payment.api.wechatpay.WechatPayApiService;
import com.getboot.payment.support.wechatpay.WechatPayHttpGateway;

/**
 * 微信支付开放接口默认实现。
 *
 * @author qiheng
 */
public class WechatPayApiServiceImpl implements WechatPayApiService {

    /**
     * 微信支付 HTTP 网关。
     */
    private final WechatPayHttpGateway httpGateway;

    /**
     * 构造开放接口访问实现。
     *
     * @param httpGateway 官方 HTTP 网关
     */
    public WechatPayApiServiceImpl(WechatPayHttpGateway httpGateway) {
        this.httpGateway = httpGateway;
    }

    /**
     * 发起 GET 请求。
     *
     * @param path 接口路径
     * @param responseType 响应类型
     * @param <T> 响应泛型
     * @return 响应对象
     */
    @Override
    public <T> T get(String path, Class<T> responseType) {
        return httpGateway.get(path, responseType);
    }

    /**
     * 发起 POST 请求。
     *
     * @param path 接口路径
     * @param requestBody 请求体
     * @param responseType 响应类型
     * @param <T> 响应泛型
     * @return 响应对象
     */
    @Override
    public <T> T post(String path, Object requestBody, Class<T> responseType) {
        return httpGateway.post(path, requestBody, responseType);
    }

    /**
     * 发起无需解析响应体的 POST 请求。
     *
     * @param path 接口路径
     * @param requestBody 请求体
     */
    @Override
    public void postWithoutResponse(String path, Object requestBody) {
        httpGateway.postWithoutResponse(path, requestBody);
    }
}
