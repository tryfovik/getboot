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
package com.getboot.payment.infrastructure.wechatpay.combine;

import com.getboot.exception.api.exception.BusinessException;
import com.getboot.payment.api.model.PaymentMode;
import com.getboot.payment.api.properties.PaymentProperties;
import com.getboot.payment.api.wechatpay.combine.WechatPayCombineCloseRequest;
import com.getboot.payment.api.wechatpay.combine.WechatPayCombineCreateRequest;
import com.getboot.payment.api.wechatpay.combine.WechatPayCombineNotifyRequest;
import com.getboot.payment.api.wechatpay.combine.WechatPayCombineOrderRequest;
import com.getboot.payment.api.wechatpay.combine.WechatPayCombineOrderResponse;
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
import com.wechat.pay.java.core.notification.NotificationParser;
import com.wechat.pay.java.core.notification.RequestParam;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 微信合单支付服务测试。
 *
 * @author qiheng
 */
class WechatPayCombinePaymentServiceImplTest {

    /**
     * 验证 JSAPI 合单下单流程会返回预支付参数。
     */
    @Test
    void shouldCreateJsapiCombineOrder() throws Exception {
        RecordingGateway gateway = new RecordingGateway();
        gateway.postResultInitializer = responseType -> {
            var constructor = responseType.getDeclaredConstructor();
            constructor.setAccessible(true);
            Object result = constructor.newInstance();
            setField(result, "prepay_id", "prepay-123");
            return result;
        };

        WechatPayCombinePaymentServiceImpl service = new WechatPayCombinePaymentServiceImpl(
                paymentProperties(),
                new StubNotificationParser(),
                new StubConfig(),
                gateway
        );

        var response = service.create(WechatPayCombineCreateRequest.builder()
                .mode(PaymentMode.JSAPI)
                .combineMerchantOrderNo("combine-001")
                .payerId("openid-001")
                .subOrders(List.of(
                        WechatPayCombineCreateRequest.SubOrder.builder()
                                .merchantOrderNo("sub-001")
                                .description("A")
                                .amount(new BigDecimal("10.00"))
                                .build(),
                        WechatPayCombineCreateRequest.SubOrder.builder()
                                .merchantOrderNo("sub-002")
                                .description("B")
                                .amount(new BigDecimal("20.00"))
                                .build()
                ))
                .build());

        assertEquals("/v3/combine-transactions/jsapi", gateway.lastPostPath);
        assertEquals("prepay-123", response.getPrepayId());
        assertEquals("wx-demo-app", response.getPaymentData().get("appId"));
        assertEquals("prepay_id=prepay-123", response.getPaymentData().get("package"));
        assertEquals("signed-value", response.getPaymentData().get("paySign"));
    }

    /**
     * 验证合单查单结果会映射为统一响应对象。
     */
    @Test
    void shouldQueryCombineOrder() throws Exception {
        RecordingGateway gateway = new RecordingGateway();
        gateway.getResultInitializer = responseType -> {
            var constructor = responseType.getDeclaredConstructor();
            constructor.setAccessible(true);
            Object result = constructor.newInstance();
            setField(result, "combine_appid", "wx-demo-app");
            setField(result, "combine_mchid", "1900001234");
            setField(result, "combine_out_trade_no", "combine-001");
            setField(result, "combine_state", "SUCCESS");
            setField(result, "transaction_id", "wx-tx-001");

            Object payerInfo = siblingInnerInstance(responseType, "CombinePayerInfo");
            setField(payerInfo, "openid", "openid-001");
            setField(result, "combine_payer_info", payerInfo);

            Object amount = siblingInnerInstance(responseType, "CombineAmount");
            setField(amount, "total_amount", 3000L);
            setField(amount, "payer_amount", 3000L);
            setField(amount, "currency", "CNY");
            setField(amount, "settlement_rate", 100000000L);

            Object subOrder = siblingInnerInstance(responseType, "CombineSubOrder");
            setField(subOrder, "mchid", "1900001234");
            setField(subOrder, "out_trade_no", "sub-001");
            setField(subOrder, "transaction_id", "wx-sub-001");
            setField(subOrder, "trade_type", "JSAPI");
            setField(subOrder, "trade_state", "SUCCESS");
            setField(subOrder, "amount", amount);
            setField(result, "sub_orders", List.of(subOrder));
            return result;
        };

        WechatPayCombinePaymentServiceImpl service = new WechatPayCombinePaymentServiceImpl(
                paymentProperties(),
                new StubNotificationParser(),
                new StubConfig(),
                gateway
        );

        WechatPayCombineOrderResponse response = service.queryOrder(
                WechatPayCombineOrderRequest.builder()
                        .combineMerchantOrderNo("combine-001")
                        .build()
        );

        assertTrue(gateway.lastGetPath.startsWith("/v3/combine-transactions/out-trade-no/combine-001"));
        assertEquals("SUCCESS", response.getStatus());
        assertEquals("openid-001", response.getPayerId());
        assertEquals("wx-sub-001", response.getSubOrders().get(0).getTransactionId());
        assertEquals(new BigDecimal("30.00"), response.getSubOrders().get(0).getPayerAmount());
    }

    /**
     * 验证合单关单流程会调用正确接口。
     */
    @Test
    void shouldCloseCombineOrder() {
        RecordingGateway gateway = new RecordingGateway();
        WechatPayCombinePaymentServiceImpl service = new WechatPayCombinePaymentServiceImpl(
                paymentProperties(),
                new StubNotificationParser(),
                new StubConfig(),
                gateway
        );

        var response = service.close(WechatPayCombineCloseRequest.builder()
                .combineMerchantOrderNo("combine-001")
                .subOrders(List.of(
                        WechatPayCombineCloseRequest.SubOrder.builder()
                                .merchantOrderNo("sub-001")
                                .build()
                ))
                .build());

        assertEquals("/v3/combine-transactions/out-trade-no/combine-001/close", gateway.lastPostWithoutResponsePath);
        assertTrue(response.isClosed());
    }

    /**
     * 验证合单通知能够被正确解析。
     */
    @Test
    void shouldParseCombineNotify() throws Exception {
        StubNotificationParser parser = new StubNotificationParser();
        parser.parseInitializer = responseType -> {
            var constructor = responseType.getDeclaredConstructor();
            constructor.setAccessible(true);
            Object result = constructor.newInstance();
            setField(result, "combine_appid", "wx-demo-app");
            setField(result, "combine_mchid", "1900001234");
            setField(result, "combine_out_trade_no", "combine-001");
            setField(result, "combine_state", "SUCCESS");
            setField(result, "transaction_id", "wx-tx-001");

            Object payerInfo = siblingInnerInstance(responseType, "CombinePayerInfo");
            setField(payerInfo, "openid", "openid-001");
            setField(result, "combine_payer_info", payerInfo);
            return result;
        };

        WechatPayCombinePaymentServiceImpl service = new WechatPayCombinePaymentServiceImpl(
                paymentProperties(),
                parser,
                new StubConfig(),
                new RecordingGateway()
        );

        var response = service.parseNotify(WechatPayCombineNotifyRequest.builder()
                .body("{\"id\":\"notify\"}")
                .headers(Map.of(
                        "Wechatpay-Serial", "serial",
                        "Wechatpay-Timestamp", "1",
                        "Wechatpay-Nonce", "nonce",
                        "Wechatpay-Signature", "signature"
                ))
                .build());

        assertTrue(response.isSuccess());
        assertEquals("combine-001", response.getCombineMerchantOrderNo());
        assertEquals("openid-001", response.getPayerId());
        assertNotNull(parser.lastRequestParam);
    }

    /**
     * 验证 SPI 扩展器能够覆盖合单下单参数。
     */
    @Test
    void shouldApplySpiCustomizerToCombineCreateRequest() {
        RecordingGateway gateway = new RecordingGateway();
        gateway.postResultInitializer = responseType -> {
            var constructor = responseType.getDeclaredConstructor();
            constructor.setAccessible(true);
            Object result = constructor.newInstance();
            setField(result, "prepay_id", "prepay-spi");
            return result;
        };

        WechatPayRequestCustomizer customizer = new WechatPayRequestCustomizer() {
            @Override
            public void customizeCombineCreate(
                    WechatPayCombineCreateRequest request,
                    WechatPayRequestOptions options) {
                options.setAppId("wx-spi-app");
                options.setNotifyUrl("https://spi.example.com/notify");
                options.setPayerId("openid-spi");
                options.putExtraBody("combine_remark", "spi");
            }
        };

        WechatPayCombinePaymentServiceImpl service = new WechatPayCombinePaymentServiceImpl(
                paymentProperties(),
                new StubNotificationParser(),
                new StubConfig(),
                gateway,
                List.of(customizer)
        );

        var response = service.create(WechatPayCombineCreateRequest.builder()
                .mode(PaymentMode.JSAPI)
                .combineMerchantOrderNo("combine-spi")
                .subOrders(List.of(
                        WechatPayCombineCreateRequest.SubOrder.builder()
                                .merchantOrderNo("sub-001")
                                .description("A")
                                .amount(new BigDecimal("10.00"))
                                .build(),
                        WechatPayCombineCreateRequest.SubOrder.builder()
                                .merchantOrderNo("sub-002")
                                .description("B")
                                .amount(new BigDecimal("20.00"))
                                .build()
                ))
                .build());

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) gateway.lastPostRequestBody;
        @SuppressWarnings("unchecked")
        Map<String, Object> payerInfo = (Map<String, Object>) body.get("combine_payer_info");
        assertEquals("wx-spi-app", body.get("combine_appid"));
        assertEquals("https://spi.example.com/notify", body.get("notify_url"));
        assertEquals("openid-spi", payerInfo.get("openid"));
        assertEquals("spi", body.get("combine_remark"));
        assertEquals("wx-spi-app", response.getPaymentData().get("appId"));
    }

    /**
     * 验证显式传入空元数据时仍可完成合单下单。
     */
    @Test
    void shouldCreateCombineOrderWhenMetadataIsExplicitlyNull() {
        RecordingGateway gateway = new RecordingGateway();
        gateway.postResultInitializer = responseType -> {
            var constructor = responseType.getDeclaredConstructor();
            constructor.setAccessible(true);
            Object result = constructor.newInstance();
            setField(result, "prepay_id", "prepay-null");
            return result;
        };

        WechatPayCombinePaymentServiceImpl service = new WechatPayCombinePaymentServiceImpl(
                paymentProperties(),
                new StubNotificationParser(),
                new StubConfig(),
                gateway
        );

        var response = service.create(WechatPayCombineCreateRequest.builder()
                .mode(PaymentMode.JSAPI)
                .combineMerchantOrderNo("combine-null")
                .payerId("openid-001")
                .metadata(null)
                .subOrders(List.of(
                        WechatPayCombineCreateRequest.SubOrder.builder()
                                .merchantOrderNo("sub-001")
                                .description("A")
                                .amount(new BigDecimal("10.00"))
                                .metadata(null)
                                .build(),
                        WechatPayCombineCreateRequest.SubOrder.builder()
                                .merchantOrderNo("sub-002")
                                .description("B")
                                .amount(new BigDecimal("20.00"))
                                .metadata(null)
                                .build()
                ))
                .build());

        assertEquals("prepay-null", response.getPrepayId());
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) gateway.lastPostRequestBody;
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> subOrders = (List<Map<String, Object>>) body.get("sub_orders");
        assertEquals("sub-001", subOrders.get(0).get("out_trade_no"));
    }

    /**
     * 验证缺少微信通知请求头时会返回明确错误。
     */
    @Test
    void shouldReportMissingWechatHeadersWhenCombineHeadersIsNull() {
        WechatPayCombinePaymentServiceImpl service = new WechatPayCombinePaymentServiceImpl(
                paymentProperties(),
                new StubNotificationParser(),
                new StubConfig(),
                new RecordingGateway()
        );

        BusinessException exception = assertThrows(BusinessException.class, () -> service.parseNotify(
                WechatPayCombineNotifyRequest.builder()
                        .body("{\"id\":\"notify\"}")
                        .headers(null)
                        .build()
        ));

        assertEquals("Missing WeChat Pay notification header: Wechatpay-Serial", exception.getMessage());
    }

    /**
     * 构造测试使用的支付配置。
     *
     * @return 测试支付配置
     */
    private PaymentProperties paymentProperties() {
        PaymentProperties properties = new PaymentProperties();
        PaymentProperties.WechatPay wechatPay = properties.getWechatpay();
        wechatPay.setAppId("wx-demo-app");
        wechatPay.setMerchantId("1900001234");
        wechatPay.setNotifyUrl("https://demo.example.com/notify");
        return properties;
    }

    /**
     * 创建与目标类型同级的内部类实例。
     *
     * @param nestedType 内部类类型
     * @param simpleName 内部类简单名
     * @return 内部类实例
     * @throws Exception 反射失败
     */
    private static Object siblingInnerInstance(Class<?> nestedType, String simpleName) throws Exception {
        Class<?> outerType = nestedType.getDeclaringClass();
        for (Class<?> innerClass : outerType.getDeclaredClasses()) {
            if (innerClass.getSimpleName().equals(simpleName)) {
                var constructor = innerClass.getDeclaredConstructor();
                constructor.setAccessible(true);
                return constructor.newInstance();
            }
        }
        throw new IllegalStateException("Inner class not found: " + simpleName);
    }

    /**
     * 通过反射设置字段值。
     *
     * @param target 目标对象
     * @param fieldName 字段名称
     * @param value 字段值
     * @throws Exception 反射失败
     */
    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    /**
     * 记录微信网关调用信息的测试桩。
     */
    private static final class RecordingGateway implements WechatPayHttpGateway {

        /**
         * 最近一次 GET 请求路径。
         */
        private String lastGetPath;

        /**
         * 最近一次 POST 请求路径。
         */
        private String lastPostPath;

        /**
         * 最近一次无响应 POST 请求路径。
         */
        private String lastPostWithoutResponsePath;

        /**
         * 最近一次 POST 请求体。
         */
        private Object lastPostRequestBody;

        /**
         * 最近一次无响应 POST 请求体。
         */
        private Object lastPostWithoutResponseBody;

        /**
         * GET 响应初始化器。
         */
        private Initializer getResultInitializer;

        /**
         * POST 响应初始化器。
         */
        private Initializer postResultInitializer;

        /**
         * 模拟执行 GET 请求。
         *
         * @param path 请求路径
         * @param responseType 响应类型
         * @param <T> 响应泛型
         * @return 模拟响应
         */
        @Override
        public <T> T get(String path, Class<T> responseType) {
            this.lastGetPath = path;
            return responseType.cast(initialize(getResultInitializer, responseType));
        }

        /**
         * 模拟执行 POST 请求。
         *
         * @param path 请求路径
         * @param requestBody 请求体
         * @param responseType 响应类型
         * @param <T> 响应泛型
         * @return 模拟响应
         */
        @Override
        public <T> T post(String path, Object requestBody, Class<T> responseType) {
            this.lastPostPath = path;
            this.lastPostRequestBody = requestBody;
            return responseType.cast(initialize(postResultInitializer, responseType));
        }

        /**
         * 模拟执行无响应 POST 请求。
         *
         * @param path 请求路径
         * @param requestBody 请求体
         */
        @Override
        public void postWithoutResponse(String path, Object requestBody) {
            this.lastPostWithoutResponsePath = path;
            this.lastPostWithoutResponseBody = requestBody;
        }

        /**
         * 初始化测试响应对象。
         *
         * @param initializer 响应初始化器
         * @param responseType 响应类型
         * @return 初始化后的响应对象
         */
        private Object initialize(Initializer initializer, Class<?> responseType) {
            try {
                if (initializer != null) {
                    return initializer.init(responseType);
                }
                var constructor = responseType.getDeclaredConstructor();
                constructor.setAccessible(true);
                return constructor.newInstance();
            } catch (Exception ex) {
                throw new IllegalStateException(ex);
            }
        }
    }

    /**
     * 记录通知解析输入的测试解析器。
     */
    private static final class StubNotificationParser extends NotificationParser {

        /**
         * 解析结果初始化器。
         */
        private Initializer parseInitializer;

        /**
         * 最近一次解析请求参数。
         */
        private RequestParam lastRequestParam;

        /**
         * 构造测试通知解析器。
         */
        private StubNotificationParser() {
            super(Map.of(), Map.of());
        }

        /**
         * 模拟解析微信通知。
         *
         * @param requestParam 请求参数
         * @param decryptObjectClass 解密目标类型
         * @param <T> 响应泛型
         * @return 模拟解析结果
         */
        @Override
        public <T> T parse(RequestParam requestParam, Class<T> decryptObjectClass) {
            this.lastRequestParam = requestParam;
            try {
                if (parseInitializer != null) {
                    return decryptObjectClass.cast(parseInitializer.init(decryptObjectClass));
                }
                var constructor = decryptObjectClass.getDeclaredConstructor();
                constructor.setAccessible(true);
                return decryptObjectClass.cast(constructor.newInstance());
            } catch (Exception ex) {
                throw new IllegalStateException(ex);
            }
        }
    }

    /**
     * 生成固定签名结果的测试配置。
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
                 * 返回测试签名算法名称。
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
     * 测试响应初始化器。
     */
    @FunctionalInterface
    private interface Initializer {

        /**
         * 初始化指定类型实例。
         *
         * @param type 目标类型
         * @return 初始化结果
         * @throws Exception 初始化失败
         */
        Object init(Class<?> type) throws Exception;
    }
}
