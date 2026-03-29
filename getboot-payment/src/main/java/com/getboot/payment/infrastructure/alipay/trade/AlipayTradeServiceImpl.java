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
package com.getboot.payment.infrastructure.alipay.trade;

import com.getboot.exception.api.exception.BusinessException;
import com.getboot.payment.api.alipay.trade.AlipayTradeBillRequest;
import com.getboot.payment.api.alipay.trade.AlipayTradeBillResponse;
import com.getboot.payment.api.alipay.trade.AlipayTradeCancelRequest;
import com.getboot.payment.api.alipay.trade.AlipayTradeCancelResponse;
import com.getboot.payment.api.alipay.trade.AlipayTradeService;
import com.getboot.payment.spi.alipay.AlipayRequestCustomizer;
import com.getboot.payment.spi.alipay.AlipayRequestOptions;
import com.getboot.payment.support.PaymentInvoker;
import com.getboot.payment.support.alipay.AlipayGateway;
import com.getboot.payment.support.alipay.AlipayRequestSupport;
import com.getboot.payment.support.alipay.AlipayResponseSupport;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * 支付宝交易增强能力实现。
 *
 * @author qiheng
 */
public class AlipayTradeServiceImpl implements AlipayTradeService {

    /**
     * 支付宝 SDK 网关。
     */
    private final AlipayGateway gateway;

    /**
     * 请求扩展器列表。
     */
    private final List<AlipayRequestCustomizer> requestCustomizers;

    /**
     * 构造交易增强服务。
     *
     * @param gateway SDK 网关
     */
    public AlipayTradeServiceImpl(AlipayGateway gateway) {
        this(gateway, List.of());
    }

    /**
     * 构造交易增强服务。
     *
     * @param gateway            SDK 网关
     * @param requestCustomizers 请求扩展器
     */
    public AlipayTradeServiceImpl(
            AlipayGateway gateway,
            List<AlipayRequestCustomizer> requestCustomizers) {
        this.gateway = gateway;
        this.requestCustomizers = requestCustomizers == null ? List.of() : List.copyOf(requestCustomizers);
    }

    @Override
    public AlipayTradeBillResponse downloadBill(AlipayTradeBillRequest request) {
        if (request == null) {
            throw new BusinessException("request must not be null");
        }
        requireText(request.getBillType(), "billType must not be blank");
        requireText(request.getBillDate(), "billDate must not be blank");
        AlipayRequestOptions options = buildBillOptions(request);
        var response = PaymentInvoker.invoke(
                () -> gateway.downloadBill(
                        request.getBillType(),
                        request.getBillDate(),
                        options.getOptionalArgs(),
                        AlipayRequestSupport.resolveContext(options)
                ),
                "Failed to query Alipay bill download url"
        );
        AlipayResponseSupport.ensureSuccess(response, "Failed to query Alipay bill download url");
        return AlipayTradeBillResponse.builder()
                .billType(request.getBillType())
                .billDate(request.getBillDate())
                .billDownloadUrl(response.getBillDownloadUrl())
                .metadata(AlipayResponseSupport.extractMetadata(response))
                .build();
    }

    @Override
    public AlipayTradeCancelResponse cancel(AlipayTradeCancelRequest request) {
        if (request == null) {
            throw new BusinessException("request must not be null");
        }
        requireText(request.getMerchantOrderNo(), "merchantOrderNo must not be blank");
        AlipayRequestOptions options = buildCancelOptions(request);
        var response = PaymentInvoker.invoke(
                () -> gateway.cancel(
                        request.getMerchantOrderNo(),
                        options.getOptionalArgs(),
                        AlipayRequestSupport.resolveContext(options)
                ),
                "Failed to cancel Alipay order"
        );
        AlipayResponseSupport.ensureSuccess(response, "Failed to cancel Alipay order");
        return AlipayTradeCancelResponse.builder()
                .merchantOrderNo(response.getOutTradeNo())
                .platformOrderNo(response.getTradeNo())
                .retryFlag(response.getRetryFlag())
                .action(response.getAction())
                .refundTime(response.getGmtRefundPay())
                .metadata(AlipayResponseSupport.extractMetadata(response))
                .build();
    }

    private AlipayRequestOptions buildBillOptions(AlipayTradeBillRequest request) {
        AlipayRequestOptions options = newRequestOptions(request.getMetadata());
        for (AlipayRequestCustomizer customizer : requestCustomizers) {
            customizer.customizeDownloadBill(request, options);
        }
        return options;
    }

    private AlipayRequestOptions buildCancelOptions(AlipayTradeCancelRequest request) {
        AlipayRequestOptions options = newRequestOptions(request.getMetadata());
        for (AlipayRequestCustomizer customizer : requestCustomizers) {
            customizer.customizeCancel(request, options);
        }
        return options;
    }

    private AlipayRequestOptions newRequestOptions(Map<String, String> metadata) {
        AlipayRequestOptions options = new AlipayRequestOptions();
        options.setAppAuthToken(AlipayRequestSupport.text(metadata, AlipayRequestSupport.APP_AUTH_TOKEN));
        options.setAuthToken(AlipayRequestSupport.text(metadata, AlipayRequestSupport.AUTH_TOKEN));
        options.setRoute(AlipayRequestSupport.text(metadata, AlipayRequestSupport.ROUTE));
        return options;
    }

    private void requireText(String value, String message) {
        if (!StringUtils.hasText(value)) {
            throw new BusinessException(message);
        }
    }
}
