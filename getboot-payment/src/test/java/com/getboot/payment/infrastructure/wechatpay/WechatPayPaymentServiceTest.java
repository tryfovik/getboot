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
package com.getboot.payment.infrastructure.wechatpay;

import com.getboot.exception.api.exception.BusinessException;
import com.getboot.payment.api.model.PaymentChannel;
import com.getboot.payment.api.model.PaymentMode;
import com.getboot.payment.api.properties.PaymentProperties;
import com.getboot.payment.api.request.PaymentCreateRequest;
import com.getboot.payment.api.request.PaymentNotifyRequest;
import com.getboot.payment.api.model.PaymentNotifyType;
import com.getboot.payment.spi.wechatpay.WechatPayRequestCustomizer;
import com.getboot.payment.spi.wechatpay.WechatPayRequestOptions;
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
import com.wechat.pay.java.core.http.JsonRequestBody;
import com.wechat.pay.java.core.http.JsonResponseBody;
import com.wechat.pay.java.core.http.ResponseBody;
import com.wechat.pay.java.service.payments.jsapi.JsapiServiceExtension;
import com.wechat.pay.java.service.payments.jsapi.model.PrepayResponse;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 微信统一支付服务测试。
 *
 * @author qiheng
 */
class WechatPayPaymentServiceTest {

    @Test
    void shouldApplySpiCustomizerToUnifiedCreateRequest() {
        RecordingHttpClient httpClient = new RecordingHttpClient();
        JsapiServiceExtension jsapiServiceExtension = newJsapiServiceExtension(new StubConfig(), httpClient);

        WechatPayRequestCustomizer customizer = new WechatPayRequestCustomizer() {
            @Override
            public void customizeCreate(PaymentCreateRequest request, WechatPayRequestOptions options) {
                options.setAppId("wx-spi-app");
                options.setNotifyUrl("https://spi.example.com/notify");
                options.setPayerId("openid-spi");
                options.setAttach("attach-spi");
                options.setGoodsTag("goods-spi");
            }
        };

        WechatPayPaymentService service = new WechatPayPaymentService(
                paymentProperties(),
                null,
                null,
                jsapiServiceExtension,
                null,
                null,
                null,
                null,
                List.of(customizer)
        );

        var response = service.create(PaymentCreateRequest.builder()
                .channel(PaymentChannel.WECHAT_PAY)
                .mode(PaymentMode.JSAPI)
                .merchantOrderNo("order-spi")
                .description("SPI 测试订单")
                .amount(new BigDecimal("12.34"))
                .build());

        String requestBody = ((JsonRequestBody) httpClient.lastRequest.getBody()).getBody();
        assertTrue(requestBody.contains("\"appid\":\"wx-spi-app\""));
        assertTrue(requestBody.contains("\"notify_url\":\"https://spi.example.com/notify\""));
        assertTrue(requestBody.contains("\"openid\":\"openid-spi\""));
        assertTrue(requestBody.contains("\"attach\":\"attach-spi\""));
        assertTrue(requestBody.contains("\"goods_tag\":\"goods-spi\""));
        assertEquals("wx-prepay-001", response.getPrepayId());
        assertEquals("wx-spi-app", response.getMetadata().get("appId"));
    }

    @Test
    void shouldCreateOrderWhenMetadataIsExplicitlyNull() {
        RecordingHttpClient httpClient = new RecordingHttpClient();
        JsapiServiceExtension jsapiServiceExtension = newJsapiServiceExtension(new StubConfig(), httpClient);

        WechatPayPaymentService service = new WechatPayPaymentService(
                paymentProperties(),
                null,
                null,
                jsapiServiceExtension,
                null,
                null,
                null,
                null
        );

        var response = service.create(PaymentCreateRequest.builder()
                .channel(PaymentChannel.WECHAT_PAY)
                .mode(PaymentMode.JSAPI)
                .merchantOrderNo("order-null-metadata")
                .description("空 metadata")
                .payerId("openid-001")
                .amount(new BigDecimal("10.00"))
                .metadata(null)
                .build());

        String requestBody = ((JsonRequestBody) httpClient.lastRequest.getBody()).getBody();
        assertTrue(requestBody.contains("\"out_trade_no\":\"order-null-metadata\""));
        assertEquals("wx-prepay-001", response.getPrepayId());
    }

    @Test
    void shouldReportMissingWechatHeadersWhenHeadersIsNull() {
        WechatPayPaymentService service = new WechatPayPaymentService(
                paymentProperties(),
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        BusinessException exception = assertThrows(BusinessException.class, () -> service.parseNotify(
                PaymentNotifyRequest.builder()
                        .channel(PaymentChannel.WECHAT_PAY)
                        .notifyType(PaymentNotifyType.PAYMENT)
                        .body("{\"id\":\"notify\"}")
                        .headers(null)
                        .build()
        ));

        assertEquals("Missing WeChat Pay notification header: Wechatpay-Serial", exception.getMessage());
    }

    private PaymentProperties paymentProperties() {
        PaymentProperties properties = new PaymentProperties();
        properties.getWechatpay().setAppId("wx-demo-app");
        properties.getWechatpay().setMerchantId("1900001234");
        properties.getWechatpay().setNotifyUrl("https://demo.example.com/notify");
        return properties;
    }

    private static final class RecordingHttpClient implements HttpClient {

        private HttpRequest lastRequest;

        @Override
        public <T> HttpResponse<T> execute(HttpRequest request, Class<T> responseType) {
            this.lastRequest = request;
            try {
                if (PrepayResponse.class.equals(responseType)) {
                    PrepayResponse response = new PrepayResponse();
                    response.setPrepayId("wx-prepay-001");
                    return httpResponse(
                            request,
                            responseType.cast(response),
                            "{\"prepay_id\":\"wx-prepay-001\"}"
                    );
                }
                throw new IllegalStateException("Unexpected response type: " + responseType.getName());
            } catch (Exception ex) {
                throw new IllegalStateException(ex);
            }
        }

        @Override
        public InputStream download(String url) {
            return new ByteArrayInputStream(new byte[0]);
        }
    }

    private static JsapiServiceExtension newJsapiServiceExtension(Config config, HttpClient httpClient) {
        try {
            Constructor<JsapiServiceExtension> constructor = JsapiServiceExtension.class.getDeclaredConstructor(
                    Config.class,
                    HttpClient.class,
                    HostName.class,
                    String.class
            );
            constructor.setAccessible(true);
            return constructor.newInstance(config, httpClient, HostName.API, "RSA");
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    private static final class StubConfig implements Config {

        @Override
        public PrivacyEncryptor createEncryptor() {
            return null;
        }

        @Override
        public PrivacyDecryptor createDecryptor() {
            return null;
        }

        @Override
        public Credential createCredential() {
            return new Credential() {
                @Override
                public String getSchema() {
                    return "WECHATPAY2-SHA256-RSA2048";
                }

                @Override
                public String getMerchantId() {
                    return "1900001234";
                }

                @Override
                public String getAuthorization(java.net.URI uri, String httpMethod, String signBody) {
                    return "test-authorization";
                }
            };
        }

        @Override
        public Validator createValidator() {
            return new Validator() {
                @Override
                public <T> boolean validate(HttpHeaders headers, String message) {
                    return true;
                }

                @Override
                public String getSerialNumber() {
                    return "serial";
                }
            };
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
