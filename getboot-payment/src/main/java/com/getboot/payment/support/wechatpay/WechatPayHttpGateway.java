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
package com.getboot.payment.support.wechatpay;

/**
 * 微信支付官方 HTTP 网关。
 *
 * <p>用于承接官方 SDK 尚未提供高级 service 封装的接口调用。</p>
 *
 * @author qiheng
 */
public interface WechatPayHttpGateway {

    /**
     * 发起 GET 请求。
     *
     * @param path         API 路径
     * @param responseType 响应类型
     * @param <T>          响应泛型
     * @return 响应对象
     */
    <T> T get(String path, Class<T> responseType);

    /**
     * 发起 POST 请求。
     *
     * @param path         API 路径
     * @param requestBody  请求体
     * @param responseType 响应类型
     * @param <T>          响应泛型
     * @return 响应对象
     */
    <T> T post(String path, Object requestBody, Class<T> responseType);

    /**
     * 发起无需读取响应体的 POST 请求。
     *
     * @param path        API 路径
     * @param requestBody 请求体
     */
    void postWithoutResponse(String path, Object requestBody);
}
