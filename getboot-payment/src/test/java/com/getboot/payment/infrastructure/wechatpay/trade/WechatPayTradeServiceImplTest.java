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

    @Test
    void shouldApplySpiCustomizerToTradeBillRequest() {
        RecordingBillHttpClient httpClient = new RecordingBillHttpClient();
        BillDownloadService billDownloadService = newBillDownloadService(httpClient);

        WechatPayRequestCustomizer customizer = new WechatPayRequestCustomizer() {
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

    private static final class RecordingGateway implements WechatPayHttpGateway {

        private String lastPostWithoutResponsePath;
        private Object lastPostWithoutResponseBody;
        private RuntimeException postWithoutResponseException;

        @Override
        public <T> T get(String path, Class<T> responseType) {
            return null;
        }

        @Override
        public <T> T post(String path, Object requestBody, Class<T> responseType) {
            return null;
        }

        @Override
        public void postWithoutResponse(String path, Object requestBody) {
            if (postWithoutResponseException != null) {
                throw postWithoutResponseException;
            }
            this.lastPostWithoutResponsePath = path;
            this.lastPostWithoutResponseBody = requestBody;
        }
    }

    private static final class RecordingBillHttpClient implements HttpClient {

        private HttpRequest lastRequest;

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

        @Override
        public InputStream download(String url) {
            return new ByteArrayInputStream(new byte[0]);
        }
    }

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

    private static final class StubConfig implements Config {

        @Override
        public PrivacyEncryptor createEncryptor() {
            return new PrivacyEncryptor() {
                @Override
                public String encrypt(String plaintext) {
                    return plaintext;
                }

                @Override
                public String getWechatpaySerial() {
                    return "serial";
                }
            };
        }

        @Override
        public PrivacyDecryptor createDecryptor() {
            return null;
        }

        @Override
        public Credential createCredential() {
            return null;
        }

        @Override
        public Validator createValidator() {
            return null;
        }

        @Override
        public Signer createSigner() {
            return new Signer() {
                @Override
                public SignatureResult sign(String message) {
                    return new SignatureResult("signed-value", "serial");
                }

                @Override
                public String getAlgorithm() {
                    return "SHA256-RSA";
                }
            };
        }
    }

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
