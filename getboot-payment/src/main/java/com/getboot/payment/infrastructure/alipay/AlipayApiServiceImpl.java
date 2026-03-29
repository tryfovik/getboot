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
package com.getboot.payment.infrastructure.alipay;

import com.alipay.easysdk.util.generic.models.AlipayOpenApiGenericResponse;
import com.getboot.exception.api.exception.BusinessException;
import com.getboot.payment.api.alipay.AlipayApiRequest;
import com.getboot.payment.api.alipay.AlipayApiResponse;
import com.getboot.payment.api.alipay.AlipayApiService;
import com.getboot.payment.support.PaymentInvoker;
import com.getboot.payment.support.alipay.AlipayGateway;
import com.getboot.payment.support.alipay.AlipayRequestSupport;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;

/**
 * 支付宝开放接口兜底访问实现。
 *
 * @author qiheng
 */
public class AlipayApiServiceImpl implements AlipayApiService {

    /**
     * 支付宝 SDK 网关。
     */
    private final AlipayGateway gateway;

    /**
     * 构造支付宝开放接口服务。
     *
     * @param gateway SDK 网关
     */
    public AlipayApiServiceImpl(AlipayGateway gateway) {
        this.gateway = gateway;
    }

    @Override
    public AlipayApiResponse execute(AlipayApiRequest request) {
        if (request == null || !StringUtils.hasText(request.getMethod())) {
            throw new BusinessException("method must not be blank");
        }
        AlipayOpenApiGenericResponse response = PaymentInvoker.invoke(
                () -> gateway.execute(
                        request.getMethod(),
                        request.getTextParams() == null ? new LinkedHashMap<>() : request.getTextParams(),
                        request.getBizParams() == null ? new LinkedHashMap<>() : request.getBizParams(),
                        AlipayRequestSupport.resolveContext(request.getMetadata())
                ),
                "Failed to execute Alipay OpenAPI"
        );
        return AlipayApiResponse.builder()
                .httpBody(response.getHttpBody())
                .code(response.getCode())
                .msg(response.getMsg())
                .subCode(response.getSubCode())
                .subMsg(response.getSubMsg())
                .build();
    }
}
