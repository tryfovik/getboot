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
package com.getboot.payment.api.wechatpay.operation.businesscircle;

import java.util.Map;

/**
 * 微信智慧商圈能力。
 *
 * @author qiheng
 */
public interface WechatPayBusinessCircleService {

    /**
     * 同步积分结果。
     *
     * @param requestBody 官方请求体
     */
    void syncPoints(Object requestBody);

    /**
     * 查询用户授权状态。
     *
     * @param request 查询参数
     * @return 原始响应
     */
    Map<String, Object> queryUserAuthorization(WechatPayBusinessCircleAuthorizationQueryRequest request);

    /**
     * 查询待积分状态。
     *
     * @param request 查询参数
     * @return 原始响应
     */
    Map<String, Object> queryCommitStatus(WechatPayBusinessCircleCommitStatusQueryRequest request);

    /**
     * 同步停车状态。
     *
     * @param requestBody 官方请求体
     */
    void syncParking(Object requestBody);
}
