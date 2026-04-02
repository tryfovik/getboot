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
package com.getboot.sms.infrastructure.aliyun.support;

import com.alibaba.fastjson2.JSON;
import com.aliyun.dysmsapi20170525.Client;
import com.getboot.sms.api.exception.SmsException;
import com.getboot.sms.api.model.SmsProviderType;
import com.getboot.sms.api.request.SmsBatchSendItem;
import com.getboot.sms.api.request.SmsBatchSendRequest;
import com.getboot.sms.api.request.SmsSendRequest;
import com.getboot.sms.api.response.SmsBatchSendResponse;
import com.getboot.sms.api.response.SmsSendResponse;
import com.getboot.sms.spi.SmsTemplateParamSerializer;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 阿里云短信供应商客户端测试。
 *
 * @author qiheng
 */
class AliyunSmsProviderClientTest {

    /**
     * 验证阿里云单条短信发送。
     *
     * @throws Exception 测试过程中可能抛出的异常
     */
    @Test
    void shouldSendSingleSmsViaAliyunClient() throws Exception {
        Client client = mock(Client.class);
        SmsTemplateParamSerializer serializer = mock(SmsTemplateParamSerializer.class);
        when(serializer.serialize(anyMap())).thenReturn("{\"code\":\"123456\"}");
        when(client.sendSms(any(com.aliyun.dysmsapi20170525.models.SendSmsRequest.class)))
                .thenReturn(singleSendResponse("OK", "Success", "request-001", "biz-001"));

        AliyunSmsProviderClient providerClient = new AliyunSmsProviderClient(client, serializer);

        SmsSendRequest request = new SmsSendRequest();
        request.setPhoneNumber("13800138000");
        request.setSignName("NoticeSign");
        request.setTemplateCode("SMS_NOTICE_001");
        request.setOutId("out-001");
        request.setTemplateParams(Map.of("code", "123456"));

        SmsSendResponse response = providerClient.send(request);

        ArgumentCaptor<com.aliyun.dysmsapi20170525.models.SendSmsRequest> captor =
                ArgumentCaptor.forClass(com.aliyun.dysmsapi20170525.models.SendSmsRequest.class);
        verify(client).sendSms(captor.capture());
        verify(serializer).serialize(request.getTemplateParams());

        assertEquals("13800138000", captor.getValue().getPhoneNumbers());
        assertEquals("NoticeSign", captor.getValue().getSignName());
        assertEquals("SMS_NOTICE_001", captor.getValue().getTemplateCode());
        assertEquals("out-001", captor.getValue().getOutId());
        assertEquals("{\"code\":\"123456\"}", captor.getValue().getTemplateParam());

        assertEquals(SmsProviderType.ALIYUN, response.getProvider());
        assertEquals("OK", response.getCode());
        assertEquals("Success", response.getMessage());
        assertEquals("request-001", response.getRequestId());
        assertEquals("biz-001", response.getBizId());
    }

    /**
     * 验证阿里云单条短信失败时抛出异常。
     *
     * @throws Exception 测试过程中可能抛出的异常
     */
    @Test
    void shouldThrowExceptionWhenAliyunSingleSendFails() throws Exception {
        Client client = mock(Client.class);
        SmsTemplateParamSerializer serializer = mock(SmsTemplateParamSerializer.class);
        when(serializer.serialize(anyMap())).thenReturn("{\"code\":\"123456\"}");
        when(client.sendSms(any(com.aliyun.dysmsapi20170525.models.SendSmsRequest.class)))
                .thenReturn(singleSendResponse("BUSINESS_LIMIT_CONTROL", "isv.BUSINESS_LIMIT_CONTROL", "request-002", "biz-002"));

        AliyunSmsProviderClient providerClient = new AliyunSmsProviderClient(client, serializer);

        SmsSendRequest request = new SmsSendRequest();
        request.setPhoneNumber("13800138000");
        request.setSignName("NoticeSign");
        request.setTemplateCode("SMS_NOTICE_001");
        request.setTemplateParams(Map.of("code", "123456"));

        SmsException exception = assertThrows(SmsException.class, () -> providerClient.send(request));
        assertEquals(
                "Aliyun SMS send failed. code=BUSINESS_LIMIT_CONTROL, message=isv.BUSINESS_LIMIT_CONTROL",
                exception.getMessage()
        );
    }

    /**
     * 验证阿里云批量短信发送。
     *
     * @throws Exception 测试过程中可能抛出的异常
     */
    @Test
    void shouldSendBatchSmsViaAliyunClient() throws Exception {
        Client client = mock(Client.class);
        SmsTemplateParamSerializer serializer = mock(SmsTemplateParamSerializer.class);
        when(serializer.serialize(anyMap()))
                .thenReturn("{\"code\":\"123456\"}", "{\"code\":\"654321\"}");
        when(client.sendBatchSms(any(com.aliyun.dysmsapi20170525.models.SendBatchSmsRequest.class)))
                .thenReturn(batchSendResponse("OK", "Success", "request-003", "biz-003"));

        AliyunSmsProviderClient providerClient = new AliyunSmsProviderClient(client, serializer);

        SmsBatchSendItem firstItem = new SmsBatchSendItem();
        firstItem.setPhoneNumber("13800138000");
        firstItem.setSignName("NoticeSign");
        firstItem.setTemplateParams(Map.of("code", "123456"));

        SmsBatchSendItem secondItem = new SmsBatchSendItem();
        secondItem.setPhoneNumber("13900139000");
        secondItem.setSignName("AlertSign");
        secondItem.setTemplateParams(Map.of("code", "654321"));

        SmsBatchSendRequest request = new SmsBatchSendRequest();
        request.setTemplateCode("SMS_BATCH_001");
        request.setOutId("batch-001");
        request.setItems(List.of(firstItem, secondItem));

        SmsBatchSendResponse response = providerClient.sendBatch(request);

        ArgumentCaptor<com.aliyun.dysmsapi20170525.models.SendBatchSmsRequest> captor =
                ArgumentCaptor.forClass(com.aliyun.dysmsapi20170525.models.SendBatchSmsRequest.class);
        verify(client).sendBatchSms(captor.capture());
        verify(serializer, times(2)).serialize(anyMap());

        assertEquals(List.of("13800138000", "13900139000"), JSON.parseArray(captor.getValue().getPhoneNumberJson(), String.class));
        assertEquals(List.of("NoticeSign", "AlertSign"), JSON.parseArray(captor.getValue().getSignNameJson(), String.class));
        assertEquals(
                List.of("{\"code\":\"123456\"}", "{\"code\":\"654321\"}"),
                JSON.parseArray(captor.getValue().getTemplateParamJson(), String.class)
        );
        assertEquals("SMS_BATCH_001", captor.getValue().getTemplateCode());
        assertEquals("batch-001", captor.getValue().getOutId());

        assertEquals(SmsProviderType.ALIYUN, response.getProvider());
        assertEquals("OK", response.getCode());
        assertEquals("Success", response.getMessage());
        assertEquals("request-003", response.getRequestId());
        assertEquals("biz-003", response.getBizId());
        assertEquals(2, response.getRecipientCount());
    }

    /**
     * 构造单条短信响应。
     *
     * @param code 状态码
     * @param message 响应信息
     * @param requestId 请求 ID
     * @param bizId 业务 ID
     * @return 阿里云单条短信响应
     */
    private com.aliyun.dysmsapi20170525.models.SendSmsResponse singleSendResponse(String code,
                                                                                   String message,
                                                                                   String requestId,
                                                                                   String bizId) {
        com.aliyun.dysmsapi20170525.models.SendSmsResponseBody body =
                new com.aliyun.dysmsapi20170525.models.SendSmsResponseBody()
                        .setCode(code)
                        .setMessage(message)
                        .setRequestId(requestId)
                        .setBizId(bizId);
        return new com.aliyun.dysmsapi20170525.models.SendSmsResponse().setBody(body);
    }

    /**
     * 构造批量短信响应。
     *
     * @param code 状态码
     * @param message 响应信息
     * @param requestId 请求 ID
     * @param bizId 业务 ID
     * @return 阿里云批量短信响应
     */
    private com.aliyun.dysmsapi20170525.models.SendBatchSmsResponse batchSendResponse(String code,
                                                                                       String message,
                                                                                       String requestId,
                                                                                       String bizId) {
        com.aliyun.dysmsapi20170525.models.SendBatchSmsResponseBody body =
                new com.aliyun.dysmsapi20170525.models.SendBatchSmsResponseBody()
                        .setCode(code)
                        .setMessage(message)
                        .setRequestId(requestId)
                        .setBizId(bizId);
        return new com.aliyun.dysmsapi20170525.models.SendBatchSmsResponse().setBody(body);
    }
}
