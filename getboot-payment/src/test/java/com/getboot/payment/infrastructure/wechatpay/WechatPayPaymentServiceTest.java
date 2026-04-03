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

    /**
     * 验证统一下单请求会应用 SPI 扩展参数。
     */
    @Test
    void shouldApplySpiCustomizerToUnifiedCreateRequest() {
        RecordingHttpClient httpClient = new RecordingHttpClient();
        JsapiServiceExtension jsapiServiceExtension = newJsapiServiceExtension(new StubConfig(), httpClient);

        WechatPayRequestCustomizer customizer = new WechatPayRequestCustomizer() {
            /**
             * 为统一下单请求注入测试扩展参数。
             *
             * @param request 创建请求
             * @param options 请求选项
             */
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

    /**
     * 验证显式传入空元数据时仍可下单。
     */
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

    /**
     * 验证缺少微信通知请求头时会返回明确错误。
     */
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

    /**
     * 构造测试使用的支付配置。
     *
     * @return 支付配置
     */
    private PaymentProperties paymentProperties() {
        PaymentProperties properties = new PaymentProperties();
        properties.getWechatpay().setAppId("wx-demo-app");
        properties.getWechatpay().setMerchantId("1900001234");
        properties.getWechatpay().setNotifyUrl("https://demo.example.com/notify");
        return properties;
    }

    /**
     * 记录统一下单 HTTP 请求的测试客户端。
     */
    private static final class RecordingHttpClient implements HttpClient {

        /**
         * 最近一次 HTTP 请求。
         */
        private HttpRequest lastRequest;

        /**
         * 模拟执行统一下单请求。
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

        /**
         * 模拟下载文件。
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
     * 通过反射构造 JSAPI 扩展服务。
     *
     * @param config 微信配置
     * @param httpClient HTTP 客户端
     * @return JSAPI 扩展服务
     */
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

    /**
     * 提供固定认证与签名能力的测试配置。
     */
    private static final class StubConfig implements Config {

        /**
         * 返回空加密器。
         *
         * @return 空加密器
         */
        @Override
        public PrivacyEncryptor createEncryptor() {
            return null;
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
         * 返回固定凭证。
         *
         * @return 固定凭证
         */
        @Override
        public Credential createCredential() {
            return new Credential() {
                /**
                 * 返回认证模式。
                 *
                 * @return 认证模式
                 */
                @Override
                public String getSchema() {
                    return "WECHATPAY2-SHA256-RSA2048";
                }

                /**
                 * 返回商户号。
                 *
                 * @return 商户号
                 */
                @Override
                public String getMerchantId() {
                    return "1900001234";
                }

                /**
                 * 返回固定授权头。
                 *
                 * @param uri 请求地址
                 * @param httpMethod 请求方法
                 * @param signBody 签名原文
                 * @return 固定授权头
                 */
                @Override
                public String getAuthorization(java.net.URI uri, String httpMethod, String signBody) {
                    return "test-authorization";
                }
            };
        }

        /**
         * 返回固定校验器。
         *
         * @return 固定校验器
         */
        @Override
        public Validator createValidator() {
            return new Validator() {
                /**
                 * 固定返回验签通过。
                 *
                 * @param headers 响应头
                 * @param message 验签内容
                 * @param <T> 泛型占位
                 * @return 始终为 true
                 */
                @Override
                public <T> boolean validate(HttpHeaders headers, String message) {
                    return true;
                }

                /**
                 * 返回固定证书序列号。
                 *
                 * @return 序列号
                 */
                @Override
                public String getSerialNumber() {
                    return "serial";
                }
            };
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
