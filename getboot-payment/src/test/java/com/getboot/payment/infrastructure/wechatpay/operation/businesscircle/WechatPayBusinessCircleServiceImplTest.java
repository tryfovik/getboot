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
import com.getboot.payment.support.wechatpay.WechatPayHttpGateway;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 微信智慧商圈能力测试。
 *
 * @author qiheng
 */
class WechatPayBusinessCircleServiceImplTest {

    /**
     * 验证用户授权查询路径拼装。
     */
    @Test
    void shouldBuildAuthorizationQueryPath() {
        RecordingGateway gateway = new RecordingGateway();
        WechatPayBusinessCircleServiceImpl service = new WechatPayBusinessCircleServiceImpl(gateway);

        service.queryUserAuthorization(WechatPayBusinessCircleAuthorizationQueryRequest.builder()
                .openId("openid-001")
                .appId("wx-app-001")
                .build());

        assertEquals(
                "/v3/businesscircle/user-authorizations/openid-001?appid=wx-app-001",
                gateway.lastGetPath
        );
    }

    /**
     * 验证积分同步状态查询路径拼装。
     */
    @Test
    void shouldBuildCommitStatusQueryPath() {
        RecordingGateway gateway = new RecordingGateway();
        WechatPayBusinessCircleServiceImpl service = new WechatPayBusinessCircleServiceImpl(gateway);

        service.queryCommitStatus(WechatPayBusinessCircleCommitStatusQueryRequest.builder()
                .openId("openid-001")
                .subMerchantId("1900000109")
                .brandId(1000L)
                .appId("wx-app-001")
                .build());

        assertEquals(
                "/v3/businesscircle/users/openid-001/points/commit_status"
                        + "?sub_mchid=1900000109&brandid=1000&appid=wx-app-001",
                gateway.lastGetPath
        );
    }

    /**
     * 记录智慧商圈请求路径的测试网关。
     */
    private static final class RecordingGateway implements WechatPayHttpGateway {

        /**
         * 最近一次 GET 请求路径。
         */
        private String lastGetPath;

        /**
         * 模拟 GET 请求。
         */
        @Override
        public <T> T get(String path, Class<T> responseType) {
            this.lastGetPath = path;
            return responseType.cast(Map.of("ok", true));
        }

        /**
         * 模拟 POST 请求。
         */
        @Override
        public <T> T post(String path, Object requestBody, Class<T> responseType) {
            return responseType.cast(Map.of("ok", true));
        }

        /**
         * 模拟无响应 POST 请求。
         */
        @Override
        public void postWithoutResponse(String path, Object requestBody) {
        }
    }
}
