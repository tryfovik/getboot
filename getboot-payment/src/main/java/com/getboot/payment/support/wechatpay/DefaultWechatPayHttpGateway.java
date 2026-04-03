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

import com.getboot.exception.api.exception.BusinessException;
import com.wechat.pay.java.core.http.HttpClient;
import com.wechat.pay.java.core.http.HttpHeaders;
import com.wechat.pay.java.core.http.JsonRequestBody;
import com.wechat.pay.java.core.util.GsonUtil;

/**
 * 微信支付官方 HTTP 网关默认实现。
 *
 * @author qiheng
 */
public class DefaultWechatPayHttpGateway implements WechatPayHttpGateway {

    /**
     * 微信支付官方 API 基础地址。
     */
    private static final String BASE_URL = "https://api.mch.weixin.qq.com";

    /**
     * 已签名 HTTP 客户端。
     */
    private final HttpClient httpClient;

    /**
     * 构造默认网关。
     *
     * @param httpClient 官方已签名 HTTP 客户端
     */
    public DefaultWechatPayHttpGateway(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    /**
     * 发起 GET 请求。
     *
     * @param path API 路径
     * @param responseType 响应类型
     * @param <T> 响应泛型
     * @return 响应对象
     */
    @Override
    public <T> T get(String path, Class<T> responseType) {
        try {
            return httpClient.get(new HttpHeaders(), BASE_URL + path, responseType).getServiceResponse();
        } catch (Exception ex) {
            throw new BusinessException("Failed to invoke WeChat Pay GET API: " + path, ex);
        }
    }

    /**
     * 发起 POST 请求。
     *
     * @param path API 路径
     * @param requestBody 请求体
     * @param responseType 响应类型
     * @param <T> 响应泛型
     * @return 响应对象
     */
    @Override
    public <T> T post(String path, Object requestBody, Class<T> responseType) {
        try {
            JsonRequestBody body = new JsonRequestBody.Builder()
                    .body(GsonUtil.toJson(requestBody))
                    .build();
            return httpClient.post(new HttpHeaders(), BASE_URL + path, body, responseType).getServiceResponse();
        } catch (Exception ex) {
            throw new BusinessException("Failed to invoke WeChat Pay POST API: " + path, ex);
        }
    }

    /**
     * 发起无需解析响应体的 POST 请求。
     *
     * @param path API 路径
     * @param requestBody 请求体
     */
    @Override
    public void postWithoutResponse(String path, Object requestBody) {
        post(path, requestBody, Void.class);
    }
}
