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
package com.getboot.payment.infrastructure.wechatpay.trade;

import com.getboot.exception.api.exception.BusinessException;
import com.getboot.payment.api.wechatpay.trade.WechatPayAbnormalRefundRequest;
import com.getboot.payment.api.wechatpay.trade.WechatPayAbnormalRefundResponse;
import com.getboot.payment.api.wechatpay.trade.WechatPayBillResponse;
import com.getboot.payment.api.wechatpay.trade.WechatPayFundFlowBillRequest;
import com.getboot.payment.api.wechatpay.trade.WechatPayTradeBillRequest;
import com.getboot.payment.api.wechatpay.trade.WechatPayTradeService;
import com.getboot.payment.spi.wechatpay.WechatPayRequestCustomizer;
import com.getboot.payment.spi.wechatpay.WechatPayRequestOptions;
import com.getboot.payment.support.PaymentInvoker;
import com.getboot.payment.support.wechatpay.WechatPayHttpGateway;
import com.wechat.pay.java.core.Config;
import com.wechat.pay.java.service.billdownload.BillDownloadService;
import com.wechat.pay.java.service.billdownload.model.AccountType;
import com.wechat.pay.java.service.billdownload.model.BillType;
import com.wechat.pay.java.service.billdownload.model.GetFundFlowBillRequest;
import com.wechat.pay.java.service.billdownload.model.GetTradeBillRequest;
import com.wechat.pay.java.service.billdownload.model.QueryBillEntity;
import com.wechat.pay.java.service.billdownload.model.TarType;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 微信支付交易增强默认实现。
 *
 * @author qiheng
 */
public class WechatPayTradeServiceImpl implements WechatPayTradeService {

    /**
     * 微信支付官方配置。
     */
    private final Config config;

    /**
     * 账单下载服务。
     */
    private final BillDownloadService billDownloadService;

    /**
     * 微信 HTTP 网关。
     */
    private final WechatPayHttpGateway httpGateway;

    /**
     * 请求扩展器集合。
     */
    private final List<WechatPayRequestCustomizer> requestCustomizers;

    /**
     * 构造交易增强服务。
     *
     * @param config              微信官方配置
     * @param billDownloadService 账单 service
     * @param httpGateway         微信 HTTP 网关
     */
    public WechatPayTradeServiceImpl(
            Config config,
            BillDownloadService billDownloadService,
            WechatPayHttpGateway httpGateway) {
        this(config, billDownloadService, httpGateway, List.of());
    }

    /**
     * 构造交易增强服务。
     *
     * @param config              微信官方配置
     * @param billDownloadService 账单 service
     * @param httpGateway         微信 HTTP 网关
     * @param requestCustomizers  请求扩展器
     */
    public WechatPayTradeServiceImpl(
            Config config,
            BillDownloadService billDownloadService,
            WechatPayHttpGateway httpGateway,
            List<WechatPayRequestCustomizer> requestCustomizers) {
        this.config = config;
        this.billDownloadService = billDownloadService;
        this.httpGateway = httpGateway;
        this.requestCustomizers = requestCustomizers == null ? List.of() : List.copyOf(requestCustomizers);
    }

    /**
     * 发起异常退款申请。
     *
     * @param request 异常退款请求
     * @return 异常退款响应
     */
    @Override
    public WechatPayAbnormalRefundResponse abnormalRefund(WechatPayAbnormalRefundRequest request) {
        requireText(request.getPlatformRefundNo(), "platformRefundNo must not be blank");
        requireText(request.getType(), "type must not be blank");
        WechatPayRequestOptions options = customizeAbnormalRefund(request);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("type", request.getType());
        body.put("bank_type", request.getBankType());
        if (StringUtils.hasText(request.getBankAccount())) {
            body.put("bank_account", config.createEncryptor().encrypt(request.getBankAccount()));
        }
        if (StringUtils.hasText(request.getRealName())) {
            body.put("real_name", config.createEncryptor().encrypt(request.getRealName()));
        }
        body.putAll(options.getExtraBody());

        String path = "/v3/refund/domestic/refunds/" + request.getPlatformRefundNo() + "/apply-abnormal-refund";
        PaymentInvoker.invokeVoid(
                () -> httpGateway.postWithoutResponse(path, body),
                "Failed to apply WeChat Pay abnormal refund"
        );

        return WechatPayAbnormalRefundResponse.builder()
                .platformRefundNo(request.getPlatformRefundNo())
                .accepted(true)
                .build();
    }

    /**
     * 查询交易账单下载地址。
     *
     * @param request 交易账单请求
     * @return 账单响应
     */
    @Override
    public WechatPayBillResponse queryTradeBill(WechatPayTradeBillRequest request) {
        requireText(request.getBillDate(), "billDate must not be blank");
        WechatPayRequestOptions options = customizeTradeBill(request);
        GetTradeBillRequest tradeBillRequest = new GetTradeBillRequest();
        tradeBillRequest.setBillDate(request.getBillDate());
        if (StringUtils.hasText(request.getBillType())) {
            tradeBillRequest.setBillType(BillType.valueOf(request.getBillType()));
        }
        if (StringUtils.hasText(request.getTarType())) {
            tradeBillRequest.setTarType(TarType.valueOf(request.getTarType()));
        }
        tradeBillRequest.setSubMchid(resolveText(options.getSubMerchantId(), request.getSubMerchantId()));

        QueryBillEntity response = PaymentInvoker.invoke(
                () -> billDownloadService.getTradeBill(tradeBillRequest),
                "Failed to query WeChat Pay trade bill"
        );
        return mapBillResponse(request.getBillDate(), response);
    }

    /**
     * 查询资金账单下载地址。
     *
     * @param request 资金账单请求
     * @return 账单响应
     */
    @Override
    public WechatPayBillResponse queryFundFlowBill(WechatPayFundFlowBillRequest request) {
        requireText(request.getBillDate(), "billDate must not be blank");
        customizeFundFlowBill(request);
        GetFundFlowBillRequest fundFlowBillRequest = new GetFundFlowBillRequest();
        fundFlowBillRequest.setBillDate(request.getBillDate());
        if (StringUtils.hasText(request.getAccountType())) {
            fundFlowBillRequest.setAccountType(AccountType.valueOf(request.getAccountType()));
        }
        if (StringUtils.hasText(request.getTarType())) {
            fundFlowBillRequest.setTarType(TarType.valueOf(request.getTarType()));
        }

        QueryBillEntity response = PaymentInvoker.invoke(
                () -> billDownloadService.getFundFlowBill(fundFlowBillRequest),
                "Failed to query WeChat Pay fund flow bill"
        );
        return mapBillResponse(request.getBillDate(), response);
    }

    /**
     * 映射账单查询响应。
     *
     * @param billDate 账单日期
     * @param response 官方响应
     * @return 账单响应
     */
    private WechatPayBillResponse mapBillResponse(String billDate, QueryBillEntity response) {
        return WechatPayBillResponse.builder()
                .billDate(billDate)
                .downloadUrl(response.getDownloadUrl())
                .hashType(response.getHashType() == null ? null : response.getHashType().name())
                .hashValue(response.getHashValue())
                .build();
    }

    /**
     * 校验文本参数不为空。
     *
     * @param value 待校验值
     * @param message 校验失败消息
     */
    private void requireText(String value, String message) {
        if (!StringUtils.hasText(value)) {
            throw new BusinessException(message);
        }
    }

    /**
     * 解析优先值与回退值。
     *
     * @param preferred 优先值
     * @param fallback 回退值
     * @return 最终值
     */
    private String resolveText(String preferred, String fallback) {
        return StringUtils.hasText(preferred) ? preferred : fallback;
    }

    /**
     * 执行交易账单请求定制。
     *
     * @param request 交易账单请求
     * @return 请求选项
     */
    private WechatPayRequestOptions customizeTradeBill(WechatPayTradeBillRequest request) {
        WechatPayRequestOptions options = new WechatPayRequestOptions();
        for (WechatPayRequestCustomizer requestCustomizer : requestCustomizers) {
            requestCustomizer.customizeTradeBill(request, options);
        }
        return options;
    }

    /**
     * 执行资金账单请求定制。
     *
     * @param request 资金账单请求
     */
    private void customizeFundFlowBill(WechatPayFundFlowBillRequest request) {
        WechatPayRequestOptions options = new WechatPayRequestOptions();
        for (WechatPayRequestCustomizer requestCustomizer : requestCustomizers) {
            requestCustomizer.customizeFundFlowBill(request, options);
        }
    }

    /**
     * 执行异常退款请求定制。
     *
     * @param request 异常退款请求
     * @return 请求选项
     */
    private WechatPayRequestOptions customizeAbnormalRefund(WechatPayAbnormalRefundRequest request) {
        WechatPayRequestOptions options = new WechatPayRequestOptions();
        for (WechatPayRequestCustomizer requestCustomizer : requestCustomizers) {
            requestCustomizer.customizeAbnormalRefund(request, options);
        }
        return options;
    }
}
