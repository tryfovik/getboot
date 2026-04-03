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
import com.getboot.payment.api.wechatpay.trade.WechatPayFundFlowBillRequest;
import com.getboot.payment.api.wechatpay.trade.WechatPayTradeBillRequest;
import com.getboot.payment.spi.wechatpay.WechatPayRequestCustomizer;
import com.getboot.payment.spi.wechatpay.WechatPayRequestOptions;
import com.getboot.payment.support.wechatpay.WechatPayHttpGateway;
import com.wechat.pay.java.core.Config;
import com.wechat.pay.java.core.auth.Credential;
import com.wechat.pay.java.core.auth.Validator;
import com.wechat.pay.java.core.cipher.PrivacyDecryptor;
import com.wechat.pay.java.core.cipher.PrivacyEncryptor;
import com.wechat.pay.java.core.cipher.SignatureResult;
import com.wechat.pay.java.core.cipher.Signer;
import com.wechat.pay.java.core.http.HostName;
import com.wechat.pay.java.core.http.HttpClient;
import com.wechat.pay.java.core.http.HttpHeaders;
import com.wechat.pay.java.core.http.HttpRequest;
import com.wechat.pay.java.core.http.HttpResponse;
import com.wechat.pay.java.core.http.JsonResponseBody;
import com.wechat.pay.java.core.http.ResponseBody;
import com.wechat.pay.java.service.billdownload.BillDownloadService;
import com.wechat.pay.java.service.billdownload.model.QueryBillEntity;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 微信交易增强服务测试。
 *
 * @author qiheng
 */
class WechatPayTradeServiceImplTest {

    /**
     * 验证交易账单查询会应用 SPI 子商户号扩展。
     */
    @Test
    void shouldApplySpiCustomizerToTradeBillRequest() {
        RecordingBillHttpClient httpClient = new RecordingBillHttpClient();
        BillDownloadService billDownloadService = newBillDownloadService(httpClient);

        WechatPayRequestCustomizer customizer = new WechatPayRequestCustomizer() {
            /**
             * 为交易账单请求注入测试扩展参数。
             *
             * @param request 账单请求
             * @param options 请求选项
             */
            @Override
            public void customizeTradeBill(WechatPayTradeBillRequest request, WechatPayRequestOptions options) {
                options.setSubMerchantId("1900009999");
            }
        };

        WechatPayTradeServiceImpl service = new WechatPayTradeServiceImpl(
                new StubConfig(),
                billDownloadService,
                new RecordingGateway(),
                List.of(customizer)
        );

        var result = service.queryTradeBill(WechatPayTradeBillRequest.builder()
                .billDate("2026-03-29")
                .billType("ALL")
                .build());

        String requestUrl = httpClient.lastRequest.getUrl().toString();
        assertTrue(requestUrl.contains("sub_mchid=1900009999"));
        assertEquals("https://bill.example.com/trade.csv", result.getDownloadUrl());
    }

    /**
     * 验证异常退款请求会应用 SPI 扩展参数。
     */
    @Test
    void shouldApplySpiCustomizerToAbnormalRefundRequest() {
        RecordingGateway gateway = new RecordingGateway();

        WechatPayRequestCustomizer customizer = new WechatPayRequestCustomizer() {
            @Override
            public void customizeAbnormalRefund(
                    WechatPayAbnormalRefundRequest request,
                    WechatPayRequestOptions options) {
                options.putExtraBody("transfer_remark", "spi");
            }
        };

        WechatPayTradeServiceImpl service = new WechatPayTradeServiceImpl(
                new StubConfig(),
                null,
                gateway,
                List.of(customizer)
        );

        var response = service.abnormalRefund(WechatPayAbnormalRefundRequest.builder()
                .platformRefundNo("refund-001")
                .type("MERCHANT_BANK_CARD")
                .build());

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) gateway.lastPostWithoutResponseBody;
        assertEquals(
                "/v3/refund/domestic/refunds/refund-001/apply-abnormal-refund",
                gateway.lastPostWithoutResponsePath
        );
        assertEquals("spi", body.get("transfer_remark"));
        assertTrue(response.isAccepted());
    }

    /**
     * 验证资金账单查询会拼装正确参数。
     */
    @Test
    void shouldQueryFundFlowBill() {
        RecordingBillHttpClient httpClient = new RecordingBillHttpClient();
        BillDownloadService billDownloadService = newBillDownloadService(httpClient);
        WechatPayTradeServiceImpl service = new WechatPayTradeServiceImpl(
                new StubConfig(),
                billDownloadService,
                new RecordingGateway()
        );

        var result = service.queryFundFlowBill(WechatPayFundFlowBillRequest.builder()
                .billDate("2026-03-29")
                .accountType("BASIC")
                .build());

        String requestUrl = httpClient.lastRequest.getUrl().toString();
        assertTrue(requestUrl.contains("/v3/bill/fundflowbill"));
        assertTrue(requestUrl.contains("account_type=BASIC"));
        assertEquals("https://bill.example.com/trade.csv", result.getDownloadUrl());
    }

    /**
     * 验证网关异常会被包装为业务异常。
     */
    @Test
    void shouldWrapGatewayExceptionWhenApplyingAbnormalRefund() {
        RecordingGateway gateway = new RecordingGateway();
        gateway.postWithoutResponseException = new IllegalStateException("gateway down");

        WechatPayTradeServiceImpl service = new WechatPayTradeServiceImpl(
                new StubConfig(),
                null,
                gateway
        );

        BusinessException exception = assertThrows(BusinessException.class, () -> service.abnormalRefund(
                WechatPayAbnormalRefundRequest.builder()
                        .platformRefundNo("refund-001")
                        .type("MERCHANT_BANK_CARD")
                        .build()
        ));

        assertEquals("Failed to apply WeChat Pay abnormal refund: gateway down", exception.getMessage());
        assertSame(gateway.postWithoutResponseException, exception.getCause());
    }

    /**
     * 记录微信 HTTP 网关调用信息的测试桩。
     */
    private static final class RecordingGateway implements WechatPayHttpGateway {

        /**
         * 最近一次无响应 POST 请求路径。
         */
        private String lastPostWithoutResponsePath;

        /**
         * 最近一次无响应 POST 请求体。
         */
        private Object lastPostWithoutResponseBody;

        /**
         * 无响应 POST 请求异常。
         */
        private RuntimeException postWithoutResponseException;

        /**
         * 模拟 GET 请求。
         *
         * @param path 请求路径
         * @param responseType 响应类型
         * @param <T> 响应泛型
         * @return 空响应
         */
        @Override
        public <T> T get(String path, Class<T> responseType) {
            return null;
        }

        /**
         * 模拟 POST 请求。
         *
         * @param path 请求路径
         * @param requestBody 请求体
         * @param responseType 响应类型
         * @param <T> 响应泛型
         * @return 空响应
         */
        @Override
        public <T> T post(String path, Object requestBody, Class<T> responseType) {
            return null;
        }

        /**
         * 模拟无响应 POST 请求。
         *
         * @param path 请求路径
         * @param requestBody 请求体
         */
        @Override
        public void postWithoutResponse(String path, Object requestBody) {
            if (postWithoutResponseException != null) {
                throw postWithoutResponseException;
            }
            this.lastPostWithoutResponsePath = path;
            this.lastPostWithoutResponseBody = requestBody;
        }
    }

    /**
     * 记录账单下载请求的测试 HTTP 客户端。
     */
    private static final class RecordingBillHttpClient implements HttpClient {

        /**
         * 最近一次 HTTP 请求。
         */
        private HttpRequest lastRequest;

        /**
         * 模拟执行账单下载查询请求。
         *
         * @param request HTTP 请求
         * @param responseType 响应类型
         * @param <T> 响应泛型
         * @return 模拟响应
         */
        @Override
        public <T> HttpResponse<T> execute(HttpRequest request, Class<T> responseType) {
            this.lastRequest = request;
            try {
                QueryBillEntity entity = new QueryBillEntity();
                entity.setDownloadUrl("https://bill.example.com/trade.csv");
                return httpResponse(
                        request,
                        responseType.cast(entity),
                        "{\"download_url\":\"https://bill.example.com/trade.csv\"}"
                );
            } catch (Exception ex) {
                throw new IllegalStateException(ex);
            }
        }

        /**
         * 模拟下载账单文件。
         *
         * @param url 下载地址
         * @return 空输入流
         */
        @Override
        public InputStream download(String url) {
            return new ByteArrayInputStream(new byte[0]);
        }
    }

    /**
     * 通过反射构造账单下载服务。
     *
     * @param httpClient HTTP 客户端
     * @return 账单下载服务
     */
    private static BillDownloadService newBillDownloadService(HttpClient httpClient) {
        try {
            Constructor<BillDownloadService> constructor = BillDownloadService.class.getDeclaredConstructor(
                    HttpClient.class,
                    HostName.class,
                    PrivacyDecryptor.class
            );
            constructor.setAccessible(true);
            PrivacyDecryptor decryptor = ciphertext -> ciphertext;
            return constructor.newInstance(httpClient, HostName.API, decryptor);
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * 提供固定签名能力的测试配置。
     */
    private static final class StubConfig implements Config {

        /**
         * 返回透传加密器。
         *
         * @return 透传加密器
         */
        @Override
        public PrivacyEncryptor createEncryptor() {
            return new PrivacyEncryptor() {
                /**
                 * 透传加密原文。
                 *
                 * @param plaintext 原文
                 * @return 原文
                 */
                @Override
                public String encrypt(String plaintext) {
                    return plaintext;
                }

                /**
                 * 返回测试证书序列号。
                 *
                 * @return 证书序列号
                 */
                @Override
                public String getWechatpaySerial() {
                    return "serial";
                }
            };
        }

        /**
         * 返回空解密器。
         *
         * @return 空解密器
         */
        @Override
        public PrivacyDecryptor createDecryptor() {
            return null;
        }

        /**
         * 返回空凭证。
         *
         * @return 空凭证
         */
        @Override
        public Credential createCredential() {
            return null;
        }

        /**
         * 返回空校验器。
         *
         * @return 空校验器
         */
        @Override
        public Validator createValidator() {
            return null;
        }

        /**
         * 返回固定签名器。
         *
         * @return 固定签名器
         */
        @Override
        public Signer createSigner() {
            return new Signer() {
                /**
                 * 返回固定签名结果。
                 *
                 * @param message 待签名内容
                 * @return 固定签名结果
                 */
                @Override
                public SignatureResult sign(String message) {
                    return new SignatureResult("signed-value", "serial");
                }

                /**
                 * 返回签名算法名称。
                 *
                 * @return 算法名称
                 */
                @Override
                public String getAlgorithm() {
                    return "SHA256-RSA";
                }
            };
        }
    }

    /**
     * 构造测试用 HTTP 响应。
     *
     * @param request 原始请求
     * @param serviceResponse 服务响应
     * @param body JSON 响应体
     * @param <T> 响应泛型
     * @return HTTP 响应
     * @throws Exception 反射失败
     */
    @SuppressWarnings("unchecked")
    private static <T> HttpResponse<T> httpResponse(HttpRequest request, T serviceResponse, String body) throws Exception {
        Constructor<?> constructor = HttpResponse.class.getDeclaredConstructors()[0];
        constructor.setAccessible(true);
        HttpHeaders headers = new HttpHeaders();
        ResponseBody responseBody = new JsonResponseBody.Builder().body(body).build();
        if (constructor.getParameterCount() == 4) {
            return (HttpResponse<T>) constructor.newInstance(request, headers, responseBody, serviceResponse);
        }
        return (HttpResponse<T>) constructor.newInstance(request, headers, responseBody, serviceResponse, null);
    }
}
