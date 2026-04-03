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
package com.getboot.payment.infrastructure.wechatpay.operation.businesscircle;

import com.getboot.payment.api.wechatpay.operation.businesscircle.WechatPayBusinessCircleAuthorizationQueryRequest;
import com.getboot.payment.api.wechatpay.operation.businesscircle.WechatPayBusinessCircleCommitStatusQueryRequest;
import com.getboot.payment.api.wechatpay.operation.businesscircle.WechatPayBusinessCircleService;
import com.getboot.payment.support.wechatpay.WechatPayHttpGateway;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 微信智慧商圈能力默认实现。
 *
 * @author qiheng
 */
public class WechatPayBusinessCircleServiceImpl implements WechatPayBusinessCircleService {

    /**
     * 微信 HTTP 网关。
     */
    private final WechatPayHttpGateway httpGateway;

    /**
     * 构造智慧商圈服务。
     *
     * @param httpGateway 微信 HTTP 网关
     */
    public WechatPayBusinessCircleServiceImpl(WechatPayHttpGateway httpGateway) {
        this.httpGateway = httpGateway;
    }

    /**
     * 同步用户积分信息。
     *
     * @param requestBody 请求体
     */
    @Override
    public void syncPoints(Object requestBody) {
        httpGateway.postWithoutResponse("/v3/businesscircle/points/notify", requestBody);
    }

    /**
     * 查询用户授权状态。
     *
     * @param request 授权查询请求
     * @return 授权查询结果
     */
    @Override
    public Map<String, Object> queryUserAuthorization(WechatPayBusinessCircleAuthorizationQueryRequest request) {
        Assert.notNull(request, "request must not be null");
        Assert.hasText(request.getOpenId(), "request.openId must not be blank");
        Assert.hasText(request.getAppId(), "request.appId must not be blank");

        Map<String, Object> args = new LinkedHashMap<>();
        args.put("appid", request.getAppId());
        return getForMap(
                "/v3/businesscircle/user-authorizations/" + urlEncode(request.getOpenId())
                        + "?" + buildQueryString(args)
        );
    }

    /**
     * 查询积分提交状态。
     *
     * @param request 提交状态查询请求
     * @return 提交状态结果
     */
    @Override
    public Map<String, Object> queryCommitStatus(WechatPayBusinessCircleCommitStatusQueryRequest request) {
        Assert.notNull(request, "request must not be null");
        Assert.hasText(request.getOpenId(), "request.openId must not be blank");
        Assert.notNull(request.getBrandId(), "request.brandId must not be null");
        Assert.hasText(request.getAppId(), "request.appId must not be blank");

        Map<String, Object> args = new LinkedHashMap<>();
        if (StringUtils.hasText(request.getSubMerchantId())) {
            args.put("sub_mchid", request.getSubMerchantId());
        }
        args.put("brandid", request.getBrandId());
        args.put("appid", request.getAppId());

        return getForMap(
                "/v3/businesscircle/users/" + urlEncode(request.getOpenId()) + "/points/commit_status"
                        + "?" + buildQueryString(args)
        );
    }

    /**
     * 同步停车信息。
     *
     * @param requestBody 请求体
     */
    @Override
    public void syncParking(Object requestBody) {
        httpGateway.postWithoutResponse("/v3/businesscircle/parkings", requestBody);
    }

    @SuppressWarnings("unchecked")
    /**
     * 以 Map 形式发起 GET 请求。
     *
     * @param path 请求路径
     * @return 响应结果
     */
    private Map<String, Object> getForMap(String path) {
        return (Map<String, Object>) httpGateway.get(path, Map.class);
    }

    /**
     * 构建查询字符串。
     *
     * @param args 查询参数
     * @return 查询字符串
     */
    private String buildQueryString(Map<String, Object> args) {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, Object> entry : args.entrySet()) {
            if (entry.getValue() == null) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append('&');
            }
            builder.append(entry.getKey())
                    .append('=')
                    .append(urlEncode(String.valueOf(entry.getValue())));
        }
        return builder.toString();
    }

    /**
     * 对参数执行 URL 编码。
     *
     * @param value 原始值
     * @return 编码后的值
     */
    private String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
    }
}
