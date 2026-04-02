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
package com.getboot.sms.support;

import com.getboot.sms.api.exception.SmsException;
import com.getboot.sms.api.properties.SmsProperties;
import com.getboot.sms.api.request.SmsBatchSendItem;
import com.getboot.sms.api.request.SmsBatchSendRequest;
import com.getboot.sms.api.request.SmsSendRequest;
import com.getboot.sms.api.request.SmsVerificationCodeRequest;
import com.getboot.sms.api.response.SmsBatchSendResponse;
import com.getboot.sms.api.response.SmsSendResponse;
import com.getboot.sms.spi.SmsProviderClient;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 默认短信发送门面测试。
 *
 * @author qiheng
 */
class DefaultSmsOperatorTest {

    /**
     * 验证单条短信场景签名解析与模板变量拷贝。
     */
    @Test
    void shouldResolveSceneSignAndCopySingleSendRequest() {
        CapturingSmsProviderClient smsProviderClient = new CapturingSmsProviderClient();
        SmsProperties properties = baseProperties();
        DefaultSmsOperator operator = new DefaultSmsOperator(
                smsProviderClient,
                new DefaultSmsSignResolver(properties),
                properties
        );

        Map<String, Object> templateParams = new LinkedHashMap<>();
        templateParams.put("name", "Alice");

        SmsSendRequest request = new SmsSendRequest();
        request.setScene(" notice ");
        request.setPhoneNumber(" 13800138000 ");
        request.setTemplateCode(" SMS_NOTICE_001 ");
        request.setOutId(" out-001 ");
        request.setTemplateParams(templateParams);

        SmsSendResponse response = operator.send(request);

        assertSame(smsProviderClient.sendResponse, response);
        assertEquals("13800138000", smsProviderClient.lastSendRequest.getPhoneNumber());
        assertEquals("NoticeSign", smsProviderClient.lastSendRequest.getSignName());
        assertEquals("SMS_NOTICE_001", smsProviderClient.lastSendRequest.getTemplateCode());
        assertEquals("out-001", smsProviderClient.lastSendRequest.getOutId());
        assertEquals(Map.of("name", "Alice"), smsProviderClient.lastSendRequest.getTemplateParams());

        templateParams.put("extra", "changed");
        assertFalse(smsProviderClient.lastSendRequest.getTemplateParams().containsKey("extra"));
    }

    /**
     * 验证批量短信场景签名解析。
     */
    @Test
    void shouldResolveBatchItemsWithSceneSign() {
        CapturingSmsProviderClient smsProviderClient = new CapturingSmsProviderClient();
        SmsProperties properties = baseProperties();
        DefaultSmsOperator operator = new DefaultSmsOperator(
                smsProviderClient,
                new DefaultSmsSignResolver(properties),
                properties
        );

        SmsBatchSendItem firstItem = new SmsBatchSendItem();
        firstItem.setPhoneNumber(" 13800138000 ");
        firstItem.setTemplateParams(Map.of("code", "123456"));

        SmsBatchSendItem secondItem = new SmsBatchSendItem();
        secondItem.setPhoneNumber(" 13900139000 ");
        secondItem.setSignName(" CustomSign ");
        secondItem.setTemplateParams(Map.of("code", "654321"));

        SmsBatchSendRequest request = new SmsBatchSendRequest();
        request.setScene("notice");
        request.setTemplateCode(" SMS_BATCH_001 ");
        request.setOutId(" batch-001 ");
        request.setItems(java.util.List.of(firstItem, secondItem));

        SmsBatchSendResponse response = operator.sendBatch(request);

        assertSame(smsProviderClient.batchSendResponse, response);
        assertEquals("SMS_BATCH_001", smsProviderClient.lastBatchSendRequest.getTemplateCode());
        assertEquals("batch-001", smsProviderClient.lastBatchSendRequest.getOutId());
        assertEquals(2, smsProviderClient.lastBatchSendRequest.getItems().size());
        assertEquals("NoticeSign", smsProviderClient.lastBatchSendRequest.getItems().get(0).getSignName());
        assertEquals("CustomSign", smsProviderClient.lastBatchSendRequest.getItems().get(1).getSignName());
    }

    /**
     * 验证验证码场景模板拼装。
     */
    @Test
    void shouldBuildVerificationCodeRequestFromSceneConfig() {
        CapturingSmsProviderClient smsProviderClient = new CapturingSmsProviderClient();
        SmsProperties properties = baseProperties();

        SmsProperties.VerificationScene verificationScene = new SmsProperties.VerificationScene();
        verificationScene.setSignName("VerifySign");
        verificationScene.setTemplateCode("SMS_LOGIN_001");
        verificationScene.setCodeParamName("verifyCode");
        verificationScene.setExpireMinutesParamName("ttl");
        verificationScene.setExtraParams(Map.of("product", "getboot"));
        properties.getVerificationScenes().put("login", verificationScene);

        DefaultSmsOperator operator = new DefaultSmsOperator(
                smsProviderClient,
                new DefaultSmsSignResolver(properties),
                properties
        );

        SmsVerificationCodeRequest request = new SmsVerificationCodeRequest();
        request.setScene("login");
        request.setPhoneNumber("13800138000");
        request.setCode("123456");
        request.setExpireMinutes(5);
        request.setTemplateParams(Map.of("channel", "app"));

        SmsSendResponse response = operator.sendVerificationCode(request);

        assertSame(smsProviderClient.sendResponse, response);
        assertEquals("VerifySign", smsProviderClient.lastSendRequest.getSignName());
        assertEquals("SMS_LOGIN_001", smsProviderClient.lastSendRequest.getTemplateCode());
        assertEquals(
                Map.of("product", "getboot", "channel", "app", "verifyCode", "123456", "ttl", 5),
                smsProviderClient.lastSendRequest.getTemplateParams()
        );
    }

    /**
     * 验证批量短信空接收项校验。
     */
    @Test
    void shouldRejectEmptyBatchItems() {
        SmsProperties properties = baseProperties();
        DefaultSmsOperator operator = new DefaultSmsOperator(
                new CapturingSmsProviderClient(),
                new DefaultSmsSignResolver(properties),
                properties
        );

        SmsBatchSendRequest request = new SmsBatchSendRequest();
        request.setScene("notice");
        request.setTemplateCode("SMS_BATCH_001");

        SmsException exception = assertThrows(SmsException.class, () -> operator.sendBatch(request));
        assertEquals("SMS batch items must not be empty.", exception.getMessage());
    }

    /**
     * 构造基础短信配置。
     *
     * @return 短信配置
     */
    private SmsProperties baseProperties() {
        SmsProperties properties = new SmsProperties();
        properties.setDefaultSignName("DefaultSign");
        properties.setSceneSignNames(Map.of("notice", "NoticeSign"));
        return properties;
    }

    /**
     * 供测试使用的捕获型供应商客户端。
     */
    private static class CapturingSmsProviderClient implements SmsProviderClient {

        /**
         * 默认单发响应。
         */
        private final SmsSendResponse sendResponse = new SmsSendResponse();

        /**
         * 默认批量响应。
         */
        private final SmsBatchSendResponse batchSendResponse = new SmsBatchSendResponse();

        /**
         * 最近一次单发请求。
         */
        private SmsSendRequest lastSendRequest;

        /**
         * 最近一次批量请求。
         */
        private SmsBatchSendRequest lastBatchSendRequest;

        /**
         * 记录单发请求。
         *
         * @param request 单发请求
         * @return 预设响应
         */
        @Override
        public SmsSendResponse send(SmsSendRequest request) {
            this.lastSendRequest = request;
            return sendResponse;
        }

        /**
         * 记录批量请求。
         *
         * @param request 批量请求
         * @return 预设响应
         */
        @Override
        public SmsBatchSendResponse sendBatch(SmsBatchSendRequest request) {
            this.lastBatchSendRequest = request;
            return batchSendResponse;
        }
    }
}
