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
package com.getboot.payment.infrastructure.alipay.settlement;

import com.alipay.easysdk.util.generic.models.AlipayOpenApiGenericResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.getboot.exception.api.exception.BusinessException;
import com.getboot.payment.api.alipay.settlement.AlipayAccountQueryRequest;
import com.getboot.payment.api.alipay.settlement.AlipayAccountQueryResponse;
import com.getboot.payment.api.alipay.settlement.AlipayElectronicReceiptApplyRequest;
import com.getboot.payment.api.alipay.settlement.AlipayElectronicReceiptApplyResponse;
import com.getboot.payment.api.alipay.settlement.AlipayElectronicReceiptQueryRequest;
import com.getboot.payment.api.alipay.settlement.AlipayElectronicReceiptQueryResponse;
import com.getboot.payment.api.alipay.settlement.AlipaySettlementService;
import com.getboot.payment.api.alipay.settlement.AlipayTransferQueryRequest;
import com.getboot.payment.api.alipay.settlement.AlipayTransferQueryResponse;
import com.getboot.payment.api.alipay.settlement.AlipayTransferNotifyRequest;
import com.getboot.payment.api.alipay.settlement.AlipayTransferNotifyResponse;
import com.getboot.payment.api.alipay.settlement.AlipayTransferRequest;
import com.getboot.payment.api.alipay.settlement.AlipayTransferResponse;
import com.getboot.payment.spi.alipay.AlipayRequestCustomizer;
import com.getboot.payment.spi.alipay.AlipayRequestOptions;
import com.getboot.payment.support.PaymentInvoker;
import com.getboot.payment.support.alipay.AlipayGateway;
import com.getboot.payment.support.alipay.AlipayNotifySupport;
import com.getboot.payment.support.alipay.AlipayOpenApiResponseSupport;
import com.getboot.payment.support.alipay.AlipayRequestSupport;
import com.getboot.payment.support.alipay.AlipayResponseSupport;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 支付宝结算能力实现。
 *
 * @author qiheng
 */
public class AlipaySettlementServiceImpl implements AlipaySettlementService {

    /**
     * 单笔转账方法名。
     */
    static final String METHOD_TRANSFER = "alipay.fund.trans.uni.transfer";

    /**
     * 转账查询方法名。
     */
    static final String METHOD_TRANSFER_QUERY = "alipay.fund.trans.common.query";

    /**
     * 账户查询方法名。
     */
    static final String METHOD_ACCOUNT_QUERY = "alipay.fund.account.query";

    /**
     * 电子回单申请方法名。
     */
    static final String METHOD_ELECTRONIC_RECEIPT_APPLY = "alipay.data.bill.ereceipt.apply";

    /**
     * 电子回单查询方法名。
     */
    static final String METHOD_ELECTRONIC_RECEIPT_QUERY = "alipay.data.bill.ereceipt.query";

    /**
     * 支付宝 SDK 网关。
     */
    private final AlipayGateway gateway;

    /**
     * 请求扩展器列表。
     */
    private final List<AlipayRequestCustomizer> requestCustomizers;

    /**
     * 构造结算能力服务。
     *
     * @param gateway SDK 网关
     */
    public AlipaySettlementServiceImpl(AlipayGateway gateway) {
        this(gateway, List.of());
    }

    /**
     * 构造结算能力服务。
     *
     * @param gateway            SDK 网关
     * @param requestCustomizers 请求扩展器
     */
    public AlipaySettlementServiceImpl(
            AlipayGateway gateway,
            List<AlipayRequestCustomizer> requestCustomizers) {
        this.gateway = gateway;
        this.requestCustomizers = requestCustomizers == null ? List.of() : List.copyOf(requestCustomizers);
    }

    @Override
    public AlipayTransferResponse transfer(AlipayTransferRequest request) {
        if (request == null) {
            throw new BusinessException("request must not be null");
        }
        requireText(request.getTransferRequestNo(), "transferRequestNo must not be blank");
        requireAmount(request.getAmount(), "amount must not be null");
        requireText(request.getOrderTitle(), "orderTitle must not be blank");
        requireText(request.getPayeeIdentity(), "payeeIdentity must not be blank");
        requireText(request.getPayeeIdentityType(), "payeeIdentityType must not be blank");
        AlipayRequestOptions options = buildTransferOptions(request);

        Map<String, Object> bizParams = new LinkedHashMap<>();
        bizParams.put("out_biz_no", request.getTransferRequestNo());
        bizParams.put("trans_amount", request.getAmount().toPlainString());
        bizParams.put("biz_scene", request.getBusinessScene());
        bizParams.put("product_code", request.getProductCode());
        bizParams.put("order_title", request.getOrderTitle());
        putIfText(bizParams, "remark", request.getRemark());

        Map<String, Object> payeeInfo = new LinkedHashMap<>();
        payeeInfo.put("identity", request.getPayeeIdentity());
        payeeInfo.put("identity_type", request.getPayeeIdentityType());
        putIfText(payeeInfo, "name", request.getPayeeName());
        bizParams.put("payee_info", payeeInfo);
        bizParams.putAll(options.getOptionalArgs());

        AlipayOpenApiGenericResponse response = PaymentInvoker.invoke(
                () -> gateway.execute(
                        METHOD_TRANSFER,
                        new LinkedHashMap<>(),
                        bizParams,
                        AlipayRequestSupport.resolveContext(options)
                ),
                "Failed to transfer by Alipay"
        );
        AlipayOpenApiResponseSupport.ensureSuccess(response, "Failed to transfer by Alipay");
        JsonNode responseNode = AlipayOpenApiResponseSupport.responseNode(METHOD_TRANSFER, response.getHttpBody());
        return AlipayTransferResponse.builder()
                .transferRequestNo(AlipayOpenApiResponseSupport.text(responseNode, "out_biz_no"))
                .platformTransferOrderNo(AlipayOpenApiResponseSupport.text(responseNode, "order_id"))
                .platformPayFundOrderNo(AlipayOpenApiResponseSupport.text(responseNode, "pay_fund_order_id"))
                .status(AlipayOpenApiResponseSupport.text(responseNode, "status"))
                .transferTime(AlipayOpenApiResponseSupport.text(responseNode, "trans_date"))
                .metadata(AlipayOpenApiResponseSupport.extractMetadata(responseNode))
                .build();
    }

    @Override
    public AlipayTransferNotifyResponse parseTransferNotify(AlipayTransferNotifyRequest request) {
        if (request == null) {
            throw new BusinessException("request must not be null");
        }
        requireText(request.getBody(), "body must not be blank");

        Map<String, String> parameters = AlipayNotifySupport.parseFormBody(request.getBody());
        boolean verified = PaymentInvoker.invoke(
                () -> gateway.verifyNotify(parameters),
                "Failed to verify Alipay transfer notify"
        );
        if (!verified) {
            throw new BusinessException("Failed to verify Alipay transfer notify");
        }
        String status = AlipayResponseSupport.firstNonBlank(parameters.get("status"), parameters.get("biz_status"));
        return AlipayTransferNotifyResponse.builder()
                .transferRequestNo(parameters.get("out_biz_no"))
                .platformTransferOrderNo(parameters.get("order_id"))
                .platformPayFundOrderNo(parameters.get("pay_fund_order_id"))
                .status(status)
                .success("SUCCESS".equalsIgnoreCase(status))
                .amount(AlipayResponseSupport.toBigDecimal(parameters.get("trans_amount")))
                .eventTime(AlipayResponseSupport.firstNonBlank(
                        parameters.get("pay_date"),
                        parameters.get("trans_date"),
                        parameters.get("gmt_trans"),
                        parameters.get("notify_time")
                ))
                .failReason(AlipayResponseSupport.firstNonBlank(
                        parameters.get("fail_reason"),
                        parameters.get("error_message"),
                        parameters.get("error_code")
                ))
                .metadata(AlipayNotifySupport.buildMetadata(parameters))
                .build();
    }

    @Override
    public AlipayTransferQueryResponse queryTransfer(AlipayTransferQueryRequest request) {
        if (request == null) {
            throw new BusinessException("request must not be null");
        }
        if (!StringUtils.hasText(request.getTransferRequestNo()) && !StringUtils.hasText(request.getPlatformTransferOrderNo())) {
            throw new BusinessException("transferRequestNo or platformTransferOrderNo must not both be blank");
        }
        AlipayRequestOptions options = buildTransferQueryOptions(request);

        Map<String, Object> bizParams = new LinkedHashMap<>();
        putIfText(bizParams, "out_biz_no", request.getTransferRequestNo());
        putIfText(bizParams, "order_id", request.getPlatformTransferOrderNo());
        putIfText(bizParams, "biz_scene", request.getBusinessScene());
        putIfText(bizParams, "product_code", request.getProductCode());
        bizParams.putAll(options.getOptionalArgs());

        AlipayOpenApiGenericResponse response = PaymentInvoker.invoke(
                () -> gateway.execute(
                        METHOD_TRANSFER_QUERY,
                        new LinkedHashMap<>(),
                        bizParams,
                        AlipayRequestSupport.resolveContext(options)
                ),
                "Failed to query Alipay transfer"
        );
        AlipayOpenApiResponseSupport.ensureSuccess(response, "Failed to query Alipay transfer");
        JsonNode responseNode = AlipayOpenApiResponseSupport.responseNode(METHOD_TRANSFER_QUERY, response.getHttpBody());
        return AlipayTransferQueryResponse.builder()
                .transferRequestNo(AlipayOpenApiResponseSupport.text(responseNode, "out_biz_no"))
                .platformTransferOrderNo(AlipayOpenApiResponseSupport.text(responseNode, "order_id"))
                .platformPayFundOrderNo(AlipayOpenApiResponseSupport.text(responseNode, "pay_fund_order_id"))
                .status(AlipayOpenApiResponseSupport.text(responseNode, "status"))
                .amount(AlipayOpenApiResponseSupport.decimal(responseNode, "trans_amount"))
                .feeAmount(AlipayOpenApiResponseSupport.decimal(responseNode, "order_fee"))
                .transferTime(AlipayOpenApiResponseSupport.text(responseNode, "pay_date"))
                .arrivalTimeEnd(AlipayOpenApiResponseSupport.text(responseNode, "arrival_time_end"))
                .failReason(AlipayResponseSupport.firstNonBlank(
                        AlipayOpenApiResponseSupport.text(responseNode, "fail_reason"),
                        AlipayOpenApiResponseSupport.text(responseNode, "error_code")
                ))
                .settlementSerialNo(AlipayOpenApiResponseSupport.text(responseNode, "settle_serial_no"))
                .metadata(AlipayOpenApiResponseSupport.extractMetadata(responseNode))
                .build();
    }

    @Override
    public AlipayAccountQueryResponse queryAccount(AlipayAccountQueryRequest request) {
        if (request == null) {
            throw new BusinessException("request must not be null");
        }
        requireText(request.getAlipayUserId(), "alipayUserId must not be blank");
        requireText(request.getAccountType(), "accountType must not be blank");
        AlipayRequestOptions options = buildAccountQueryOptions(request);

        Map<String, Object> bizParams = new LinkedHashMap<>();
        bizParams.put("alipay_user_id", request.getAlipayUserId());
        bizParams.put("account_type", request.getAccountType());
        bizParams.putAll(options.getOptionalArgs());

        AlipayOpenApiGenericResponse response = PaymentInvoker.invoke(
                () -> gateway.execute(
                        METHOD_ACCOUNT_QUERY,
                        new LinkedHashMap<>(),
                        bizParams,
                        AlipayRequestSupport.resolveContext(options)
                ),
                "Failed to query Alipay account balance"
        );
        AlipayOpenApiResponseSupport.ensureSuccess(response, "Failed to query Alipay account balance");
        JsonNode responseNode = AlipayOpenApiResponseSupport.responseNode(METHOD_ACCOUNT_QUERY, response.getHttpBody());
        return AlipayAccountQueryResponse.builder()
                .alipayUserId(request.getAlipayUserId())
                .accountType(request.getAccountType())
                .availableAmount(AlipayOpenApiResponseSupport.decimal(responseNode, "available_amount"))
                .freezeAmount(AlipayOpenApiResponseSupport.decimal(responseNode, "freeze_amount"))
                .metadata(AlipayOpenApiResponseSupport.extractMetadata(responseNode))
                .build();
    }

    @Override
    public AlipayElectronicReceiptApplyResponse applyElectronicReceipt(AlipayElectronicReceiptApplyRequest request) {
        if (request == null) {
            throw new BusinessException("request must not be null");
        }
        requireText(request.getType(), "type must not be blank");
        requireText(request.getKey(), "key must not be blank");
        AlipayRequestOptions options = buildElectronicReceiptApplyOptions(request);

        Map<String, Object> bizParams = new LinkedHashMap<>();
        putIfText(bizParams, "bill_user_id", request.getBillUserId());
        bizParams.put("type", request.getType());
        bizParams.put("key", request.getKey());
        bizParams.putAll(options.getOptionalArgs());

        AlipayOpenApiGenericResponse response = PaymentInvoker.invoke(
                () -> gateway.execute(
                        METHOD_ELECTRONIC_RECEIPT_APPLY,
                        new LinkedHashMap<>(),
                        bizParams,
                        AlipayRequestSupport.resolveContext(options)
                ),
                "Failed to apply Alipay electronic receipt"
        );
        AlipayOpenApiResponseSupport.ensureSuccess(response, "Failed to apply Alipay electronic receipt");
        JsonNode responseNode = AlipayOpenApiResponseSupport.responseNode(
                METHOD_ELECTRONIC_RECEIPT_APPLY,
                response.getHttpBody()
        );
        return AlipayElectronicReceiptApplyResponse.builder()
                .fileId(AlipayOpenApiResponseSupport.text(responseNode, "file_id"))
                .metadata(AlipayOpenApiResponseSupport.extractMetadata(responseNode))
                .build();
    }

    @Override
    public AlipayElectronicReceiptQueryResponse queryElectronicReceipt(AlipayElectronicReceiptQueryRequest request) {
        if (request == null) {
            throw new BusinessException("request must not be null");
        }
        requireText(request.getFileId(), "fileId must not be blank");
        AlipayRequestOptions options = buildElectronicReceiptQueryOptions(request);

        Map<String, Object> bizParams = new LinkedHashMap<>();
        bizParams.put("file_id", request.getFileId());
        bizParams.putAll(options.getOptionalArgs());

        AlipayOpenApiGenericResponse response = PaymentInvoker.invoke(
                () -> gateway.execute(
                        METHOD_ELECTRONIC_RECEIPT_QUERY,
                        new LinkedHashMap<>(),
                        bizParams,
                        AlipayRequestSupport.resolveContext(options)
                ),
                "Failed to query Alipay electronic receipt"
        );
        AlipayOpenApiResponseSupport.ensureSuccess(response, "Failed to query Alipay electronic receipt");
        JsonNode responseNode = AlipayOpenApiResponseSupport.responseNode(
                METHOD_ELECTRONIC_RECEIPT_QUERY,
                response.getHttpBody()
        );
        String downloadUrl = AlipayOpenApiResponseSupport.text(responseNode, "download_url");
        return AlipayElectronicReceiptQueryResponse.builder()
                .fileId(request.getFileId())
                .status(AlipayOpenApiResponseSupport.text(responseNode, "status"))
                .downloadReady(StringUtils.hasText(downloadUrl))
                .downloadUrl(downloadUrl)
                .errorMessage(AlipayOpenApiResponseSupport.text(responseNode, "error_message"))
                .metadata(AlipayOpenApiResponseSupport.extractMetadata(responseNode))
                .build();
    }

    private AlipayRequestOptions buildTransferOptions(AlipayTransferRequest request) {
        AlipayRequestOptions options = newRequestOptions(request.getMetadata());
        for (AlipayRequestCustomizer customizer : requestCustomizers) {
            customizer.customizeTransfer(request, options);
        }
        return options;
    }

    private AlipayRequestOptions buildTransferQueryOptions(AlipayTransferQueryRequest request) {
        AlipayRequestOptions options = newRequestOptions(request.getMetadata());
        for (AlipayRequestCustomizer customizer : requestCustomizers) {
            customizer.customizeTransferQuery(request, options);
        }
        return options;
    }

    private AlipayRequestOptions buildAccountQueryOptions(AlipayAccountQueryRequest request) {
        AlipayRequestOptions options = newRequestOptions(request.getMetadata());
        for (AlipayRequestCustomizer customizer : requestCustomizers) {
            customizer.customizeAccountQuery(request, options);
        }
        return options;
    }

    private AlipayRequestOptions buildElectronicReceiptApplyOptions(AlipayElectronicReceiptApplyRequest request) {
        AlipayRequestOptions options = newRequestOptions(request.getMetadata());
        for (AlipayRequestCustomizer customizer : requestCustomizers) {
            customizer.customizeApplyElectronicReceipt(request, options);
        }
        return options;
    }

    private AlipayRequestOptions buildElectronicReceiptQueryOptions(AlipayElectronicReceiptQueryRequest request) {
        AlipayRequestOptions options = newRequestOptions(request.getMetadata());
        for (AlipayRequestCustomizer customizer : requestCustomizers) {
            customizer.customizeQueryElectronicReceipt(request, options);
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

    private void requireAmount(BigDecimal value, String message) {
        if (value == null) {
            throw new BusinessException(message);
        }
    }

    private void putIfText(Map<String, Object> target, String key, String value) {
        if (target != null && StringUtils.hasText(key) && StringUtils.hasText(value)) {
            target.put(key, value);
        }
    }
}
