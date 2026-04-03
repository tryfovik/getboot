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

import com.alipay.easysdk.payment.app.models.AlipayTradeAppPayResponse;
import com.alipay.easysdk.payment.common.models.AlipayDataDataserviceBillDownloadurlQueryResponse;
import com.alipay.easysdk.payment.common.models.AlipayTradeCancelResponse;
import com.alipay.easysdk.payment.common.models.AlipayTradeCloseResponse;
import com.alipay.easysdk.payment.common.models.AlipayTradeFastpayRefundQueryResponse;
import com.alipay.easysdk.payment.common.models.AlipayTradeQueryResponse;
import com.alipay.easysdk.payment.common.models.AlipayTradeRefundResponse;
import com.alipay.easysdk.payment.facetoface.models.AlipayTradePrecreateResponse;
import com.alipay.easysdk.payment.page.models.AlipayTradePagePayResponse;
import com.alipay.easysdk.payment.wap.models.AlipayTradeWapPayResponse;
import com.alipay.easysdk.util.generic.models.AlipayOpenApiGenericResponse;
import com.getboot.payment.api.alipay.settlement.AlipayAccountQueryRequest;
import com.getboot.payment.api.alipay.settlement.AlipayElectronicReceiptApplyRequest;
import com.getboot.payment.api.alipay.settlement.AlipayElectronicReceiptQueryRequest;
import com.getboot.payment.api.alipay.settlement.AlipayTransferQueryRequest;
import com.getboot.payment.api.alipay.settlement.AlipayTransferNotifyRequest;
import com.getboot.payment.api.alipay.settlement.AlipayTransferRequest;
import com.getboot.payment.spi.alipay.AlipayRequestCustomizer;
import com.getboot.payment.spi.alipay.AlipayRequestOptions;
import com.getboot.payment.support.alipay.AlipayGateway;
import com.getboot.payment.support.alipay.AlipayRequestContext;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 支付宝结算能力测试。
 *
 * @author qiheng
 */
class AlipaySettlementServiceImplTest {

    /**
     * 验证转账请求会应用 SPI 上下文与扩展参数。
     */
    @Test
    void shouldTransferWithSpiContext() {
        RecordingGateway gateway = new RecordingGateway();
        gateway.genericResponse = successResponse(
                "{\"alipay_fund_trans_uni_transfer_response\":{"
                        + "\"code\":\"10000\","
                        + "\"msg\":\"Success\","
                        + "\"out_biz_no\":\"transfer-001\","
                        + "\"order_id\":\"202603290001\","
                        + "\"pay_fund_order_id\":\"2026032900fund\","
                        + "\"status\":\"SUCCESS\","
                        + "\"trans_date\":\"2026-03-29 15:00:00\"},"
                        + "\"sign\":\"mock\"}"
        );

        AlipayRequestCustomizer customizer = new AlipayRequestCustomizer() {
            /**
             * 为转账请求注入测试扩展参数。
             *
             * @param request 转账请求
             * @param options 请求选项
             */
            @Override
            public void customizeTransfer(AlipayTransferRequest request, AlipayRequestOptions options) {
                options.setAppAuthToken("app-auth-token");
                options.putOptionalArg("remark", "spi-remark");
            }
        };

        AlipaySettlementServiceImpl service = new AlipaySettlementServiceImpl(gateway, List.of(customizer));
        var response = service.transfer(AlipayTransferRequest.builder()
                .transferRequestNo("transfer-001")
                .amount(new BigDecimal("88.66"))
                .orderTitle("分账结算")
                .payeeIdentity("2088123412341234")
                .payeeIdentityType("ALIPAY_USER_ID")
                .build());

        assertEquals(AlipaySettlementServiceImpl.METHOD_TRANSFER, gateway.lastMethod);
        assertEquals("transfer-001", gateway.lastBizParams.get("out_biz_no"));
        assertEquals("88.66", gateway.lastBizParams.get("trans_amount"));
        assertEquals("spi-remark", gateway.lastBizParams.get("remark"));
        assertEquals("app-auth-token", gateway.lastRequestContext.appAuthToken());
        assertEquals("202603290001", response.getPlatformTransferOrderNo());
        assertEquals("SUCCESS", response.getStatus());
    }

    /**
     * 验证转账查询结果映射。
     */
    @Test
    void shouldQueryTransfer() {
        RecordingGateway gateway = new RecordingGateway();
        gateway.genericResponse = successResponse(
                "{\"alipay_fund_trans_common_query_response\":{"
                        + "\"code\":\"10000\","
                        + "\"msg\":\"Success\","
                        + "\"out_biz_no\":\"transfer-002\","
                        + "\"order_id\":\"202603290002\","
                        + "\"pay_fund_order_id\":\"2026032900fund2\","
                        + "\"status\":\"SUCCESS\","
                        + "\"trans_amount\":\"66.00\","
                        + "\"order_fee\":\"0.10\","
                        + "\"pay_date\":\"2026-03-29 16:00:00\","
                        + "\"arrival_time_end\":\"2026-03-29 18:00:00\","
                        + "\"settle_serial_no\":\"settle-001\"},"
                        + "\"sign\":\"mock\"}"
        );

        AlipaySettlementServiceImpl service = new AlipaySettlementServiceImpl(gateway);
        var response = service.queryTransfer(AlipayTransferQueryRequest.builder()
                .transferRequestNo("transfer-002")
                .build());

        assertEquals(AlipaySettlementServiceImpl.METHOD_TRANSFER_QUERY, gateway.lastMethod);
        assertEquals("transfer-002", gateway.lastBizParams.get("out_biz_no"));
        assertEquals(new BigDecimal("66.00"), response.getAmount());
        assertEquals(new BigDecimal("0.10"), response.getFeeAmount());
        assertEquals("settle-001", response.getSettlementSerialNo());
    }

    /**
     * 验证账户余额查询结果映射。
     */
    @Test
    void shouldQueryAccountBalance() {
        RecordingGateway gateway = new RecordingGateway();
        gateway.genericResponse = successResponse(
                "{\"alipay_fund_account_query_response\":{"
                        + "\"code\":\"10000\","
                        + "\"msg\":\"Success\","
                        + "\"available_amount\":\"1024.55\","
                        + "\"freeze_amount\":\"12.30\"},"
                        + "\"sign\":\"mock\"}"
        );

        AlipaySettlementServiceImpl service = new AlipaySettlementServiceImpl(gateway);
        var response = service.queryAccount(AlipayAccountQueryRequest.builder()
                .alipayUserId("2088123412341234")
                .build());

        assertEquals(AlipaySettlementServiceImpl.METHOD_ACCOUNT_QUERY, gateway.lastMethod);
        assertEquals("2088123412341234", gateway.lastBizParams.get("alipay_user_id"));
        assertEquals("ACCTRANS_ACCOUNT", gateway.lastBizParams.get("account_type"));
        assertEquals(new BigDecimal("1024.55"), response.getAvailableAmount());
        assertEquals(new BigDecimal("12.30"), response.getFreezeAmount());
    }

    /**
     * 验证转账通知解析与验签透传。
     */
    @Test
    void shouldParseTransferNotify() {
        RecordingGateway gateway = new RecordingGateway();
        gateway.verifyNotifyResult = true;

        AlipaySettlementServiceImpl service = new AlipaySettlementServiceImpl(gateway);
        var response = service.parseTransferNotify(AlipayTransferNotifyRequest.builder()
                .body("out_biz_no=transfer-003&order_id=202603290003&pay_fund_order_id=2026032900fund3"
                        + "&status=SUCCESS&trans_amount=18.88&notify_time=2026-03-29+17%3A00%3A00&sign=mock")
                .build());

        assertEquals("transfer-003", response.getTransferRequestNo());
        assertEquals("202603290003", response.getPlatformTransferOrderNo());
        assertEquals("2026032900fund3", response.getPlatformPayFundOrderNo());
        assertEquals("SUCCESS", response.getStatus());
        assertTrue(response.isSuccess());
        assertEquals(new BigDecimal("18.88"), response.getAmount());
        assertEquals("2026-03-29 17:00:00", response.getEventTime());
        assertEquals("transfer-003", gateway.lastNotifyParameters.get("out_biz_no"));
    }

    /**
     * 验证电子回单申请会应用 SPI 上下文。
     */
    @Test
    void shouldApplyElectronicReceiptWithSpiContext() {
        RecordingGateway gateway = new RecordingGateway();
        gateway.genericResponse = successResponse(
                "{\"alipay_data_bill_ereceipt_apply_response\":{"
                        + "\"code\":\"10000\","
                        + "\"msg\":\"Success\","
                        + "\"file_id\":\"receipt-file-001\"},"
                        + "\"sign\":\"mock\"}"
        );

        AlipayRequestCustomizer customizer = new AlipayRequestCustomizer() {
            @Override
            public void customizeApplyElectronicReceipt(
                    AlipayElectronicReceiptApplyRequest request,
                    AlipayRequestOptions options) {
                options.setAppAuthToken("receipt-app-auth-token");
                options.putOptionalArg("biz_scene", "DIRECT_TRANSFER");
            }
        };

        AlipaySettlementServiceImpl service = new AlipaySettlementServiceImpl(gateway, List.of(customizer));
        var response = service.applyElectronicReceipt(AlipayElectronicReceiptApplyRequest.builder()
                .billUserId("2088123412341234")
                .type("FUND_DETAIL")
                .key("2026032900fund3")
                .build());

        assertEquals(AlipaySettlementServiceImpl.METHOD_ELECTRONIC_RECEIPT_APPLY, gateway.lastMethod);
        assertEquals("2088123412341234", gateway.lastBizParams.get("bill_user_id"));
        assertEquals("FUND_DETAIL", gateway.lastBizParams.get("type"));
        assertEquals("2026032900fund3", gateway.lastBizParams.get("key"));
        assertEquals("DIRECT_TRANSFER", gateway.lastBizParams.get("biz_scene"));
        assertEquals("receipt-app-auth-token", gateway.lastRequestContext.appAuthToken());
        assertEquals("receipt-file-001", response.getFileId());
    }

    /**
     * 验证电子回单查询结果映射。
     */
    @Test
    void shouldQueryElectronicReceipt() {
        RecordingGateway gateway = new RecordingGateway();
        gateway.genericResponse = successResponse(
                "{\"alipay_data_bill_ereceipt_query_response\":{"
                        + "\"code\":\"10000\","
                        + "\"msg\":\"Success\","
                        + "\"status\":\"FINISHED\","
                        + "\"download_url\":\"https://download.example.com/receipt-001.pdf\"},"
                        + "\"sign\":\"mock\"}"
        );

        AlipaySettlementServiceImpl service = new AlipaySettlementServiceImpl(gateway);
        var response = service.queryElectronicReceipt(AlipayElectronicReceiptQueryRequest.builder()
                .fileId("receipt-file-001")
                .build());

        assertEquals(AlipaySettlementServiceImpl.METHOD_ELECTRONIC_RECEIPT_QUERY, gateway.lastMethod);
        assertEquals("receipt-file-001", gateway.lastBizParams.get("file_id"));
        assertEquals("FINISHED", response.getStatus());
        assertTrue(response.isDownloadReady());
        assertEquals("https://download.example.com/receipt-001.pdf", response.getDownloadUrl());
    }

    /**
     * 验证电子回单未生成完成时的状态处理。
     */
    @Test
    void shouldQueryElectronicReceiptWithoutDownloadUrl() {
        RecordingGateway gateway = new RecordingGateway();
        gateway.genericResponse = successResponse(
                "{\"alipay_data_bill_ereceipt_query_response\":{"
                        + "\"code\":\"10000\","
                        + "\"msg\":\"Success\","
                        + "\"status\":\"PROCESSING\","
                        + "\"error_message\":\"回单生成中\"},"
                        + "\"sign\":\"mock\"}"
        );

        AlipaySettlementServiceImpl service = new AlipaySettlementServiceImpl(gateway);
        var response = service.queryElectronicReceipt(AlipayElectronicReceiptQueryRequest.builder()
                .fileId("receipt-file-002")
                .build());

        assertEquals("PROCESSING", response.getStatus());
        assertFalse(response.isDownloadReady());
        assertEquals("回单生成中", response.getErrorMessage());
    }

    /**
     * 构造成功的泛化响应。
     *
     * @param httpBody 原始响应体
     * @return 泛化响应
     */
    private AlipayOpenApiGenericResponse successResponse(String httpBody) {
        AlipayOpenApiGenericResponse response = new AlipayOpenApiGenericResponse();
        response.code = "10000";
        response.msg = "Success";
        response.httpBody = httpBody;
        return response;
    }

    /**
     * 记录支付宝结算网关调用信息的测试桩。
     */
    private static final class RecordingGateway implements AlipayGateway {

        /**
         * 泛化响应。
         */
        private AlipayOpenApiGenericResponse genericResponse;

        /**
         * 最近一次调用方法。
         */
        private String lastMethod;

        /**
         * 最近一次文本参数。
         */
        private Map<String, String> lastTextParams = new LinkedHashMap<>();

        /**
         * 最近一次业务参数。
         */
        private Map<String, Object> lastBizParams = new LinkedHashMap<>();

        /**
         * 最近一次请求上下文。
         */
        private AlipayRequestContext lastRequestContext;

        /**
         * 验签结果。
         */
        private boolean verifyNotifyResult;

        /**
         * 最近一次通知参数。
         */
        private Map<String, String> lastNotifyParameters = new LinkedHashMap<>();

        /**
         * 模拟 APP 支付调用。
         *
         * @param subject 订单标题
         * @param outTradeNo 商户订单号
         * @param totalAmount 订单金额
         * @param notifyUrl 通知地址
         * @param optionalArgs 可选参数
         * @param requestContext 调用上下文
         * @return APP 支付响应
         */
        @Override
        public AlipayTradeAppPayResponse appPay(
                String subject,
                String outTradeNo,
                String totalAmount,
                String notifyUrl,
                Map<String, Object> optionalArgs,
                AlipayRequestContext requestContext) {
            return new AlipayTradeAppPayResponse();
        }

        /**
         * 模拟页面支付调用。
         *
         * @param subject 订单标题
         * @param outTradeNo 商户订单号
         * @param totalAmount 订单金额
         * @param returnUrl 返回地址
         * @param notifyUrl 通知地址
         * @param optionalArgs 可选参数
         * @param requestContext 调用上下文
         * @return 页面支付响应
         */
        @Override
        public AlipayTradePagePayResponse pagePay(
                String subject,
                String outTradeNo,
                String totalAmount,
                String returnUrl,
                String notifyUrl,
                Map<String, Object> optionalArgs,
                AlipayRequestContext requestContext) {
            return new AlipayTradePagePayResponse();
        }

        /**
         * 模拟 WAP 支付调用。
         *
         * @param subject 订单标题
         * @param outTradeNo 商户订单号
         * @param totalAmount 订单金额
         * @param quitUrl 退出地址
         * @param returnUrl 返回地址
         * @param notifyUrl 通知地址
         * @param optionalArgs 可选参数
         * @param requestContext 调用上下文
         * @return WAP 支付响应
         */
        @Override
        public AlipayTradeWapPayResponse wapPay(
                String subject,
                String outTradeNo,
                String totalAmount,
                String quitUrl,
                String returnUrl,
                String notifyUrl,
                Map<String, Object> optionalArgs,
                AlipayRequestContext requestContext) {
            return new AlipayTradeWapPayResponse();
        }

        /**
         * 模拟预下单调用。
         *
         * @param subject 订单标题
         * @param outTradeNo 商户订单号
         * @param totalAmount 订单金额
         * @param notifyUrl 通知地址
         * @param optionalArgs 可选参数
         * @param requestContext 调用上下文
         * @return 预下单响应
         */
        @Override
        public AlipayTradePrecreateResponse preCreate(
                String subject,
                String outTradeNo,
                String totalAmount,
                String notifyUrl,
                Map<String, Object> optionalArgs,
                AlipayRequestContext requestContext) {
            return new AlipayTradePrecreateResponse();
        }

        /**
         * 模拟订单查询调用。
         *
         * @param outTradeNo 商户订单号
         * @param optionalArgs 可选参数
         * @param requestContext 调用上下文
         * @return 订单查询响应
         */
        @Override
        public AlipayTradeQueryResponse query(
                String outTradeNo,
                Map<String, Object> optionalArgs,
                AlipayRequestContext requestContext) {
            return new AlipayTradeQueryResponse();
        }

        /**
         * 模拟退款调用。
         *
         * @param outTradeNo 商户订单号
         * @param refundAmount 退款金额
         * @param notifyUrl 通知地址
         * @param optionalArgs 可选参数
         * @param requestContext 调用上下文
         * @return 退款响应
         */
        @Override
        public AlipayTradeRefundResponse refund(
                String outTradeNo,
                String refundAmount,
                String notifyUrl,
                Map<String, Object> optionalArgs,
                AlipayRequestContext requestContext) {
            return new AlipayTradeRefundResponse();
        }

        /**
         * 模拟退款查询调用。
         *
         * @param outTradeNo 商户订单号
         * @param outRequestNo 退款请求号
         * @param optionalArgs 可选参数
         * @param requestContext 调用上下文
         * @return 退款查询响应
         */
        @Override
        public AlipayTradeFastpayRefundQueryResponse queryRefund(
                String outTradeNo,
                String outRequestNo,
                Map<String, Object> optionalArgs,
                AlipayRequestContext requestContext) {
            return new AlipayTradeFastpayRefundQueryResponse();
        }

        /**
         * 模拟关单调用。
         *
         * @param outTradeNo 商户订单号
         * @param optionalArgs 可选参数
         * @param requestContext 调用上下文
         * @return 关单响应
         */
        @Override
        public AlipayTradeCloseResponse close(
                String outTradeNo,
                Map<String, Object> optionalArgs,
                AlipayRequestContext requestContext) {
            return new AlipayTradeCloseResponse();
        }

        /**
         * 模拟撤销调用。
         *
         * @param outTradeNo 商户订单号
         * @param optionalArgs 可选参数
         * @param requestContext 调用上下文
         * @return 撤销响应
         */
        @Override
        public AlipayTradeCancelResponse cancel(
                String outTradeNo,
                Map<String, Object> optionalArgs,
                AlipayRequestContext requestContext) {
            return new AlipayTradeCancelResponse();
        }

        /**
         * 模拟账单下载地址查询。
         *
         * @param billType 账单类型
         * @param billDate 账单日期
         * @param optionalArgs 可选参数
         * @param requestContext 调用上下文
         * @return 账单响应
         */
        @Override
        public AlipayDataDataserviceBillDownloadurlQueryResponse downloadBill(
                String billType,
                String billDate,
                Map<String, Object> optionalArgs,
                AlipayRequestContext requestContext) {
            return new AlipayDataDataserviceBillDownloadurlQueryResponse();
        }

        /**
         * 模拟通知验签。
         *
         * @param parameters 通知参数
         * @return 验签结果
         */
        @Override
        public boolean verifyNotify(Map<String, String> parameters) {
            this.lastNotifyParameters = new LinkedHashMap<>(parameters);
            return verifyNotifyResult;
        }

        /**
         * 模拟带上下文的泛化调用。
         *
         * @param method 方法名
         * @param textParams 文本参数
         * @param bizParams 业务参数
         * @param requestContext 调用上下文
         * @return 泛化响应
         */
        @Override
        public AlipayOpenApiGenericResponse execute(
                String method,
                Map<String, String> textParams,
                Map<String, Object> bizParams,
                AlipayRequestContext requestContext) {
            this.lastMethod = method;
            this.lastTextParams = new LinkedHashMap<>(textParams);
            this.lastBizParams = new LinkedHashMap<>(bizParams);
            this.lastRequestContext = requestContext;
            return genericResponse;
        }
    }
}
