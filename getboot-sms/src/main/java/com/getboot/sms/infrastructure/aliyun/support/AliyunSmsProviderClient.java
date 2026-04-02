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
import com.aliyun.dysmsapi20170525.models.SendBatchSmsResponse;
import com.aliyun.dysmsapi20170525.models.SendBatchSmsResponseBody;
import com.aliyun.dysmsapi20170525.models.SendSmsResponse;
import com.aliyun.dysmsapi20170525.models.SendSmsResponseBody;
import com.getboot.sms.api.constant.SmsConstants;
import com.getboot.sms.api.exception.SmsException;
import com.getboot.sms.api.model.SmsProviderType;
import com.getboot.sms.api.request.SmsBatchSendItem;
import com.getboot.sms.api.request.SmsBatchSendRequest;
import com.getboot.sms.api.request.SmsSendRequest;
import com.getboot.sms.api.response.SmsBatchSendResponse;
import com.getboot.sms.api.response.SmsSendResponse;
import com.getboot.sms.spi.SmsProviderClient;
import com.getboot.sms.spi.SmsTemplateParamSerializer;

import java.util.ArrayList;
import java.util.List;

/**
 * 阿里云短信供应商客户端实现。
 *
 * @author qiheng
 */
public class AliyunSmsProviderClient implements SmsProviderClient {

    /**
     * 阿里云短信客户端。
     */
    private final Client client;

    /**
     * 模板变量序列化器。
     */
    private final SmsTemplateParamSerializer smsTemplateParamSerializer;

    /**
     * 构造阿里云短信供应商客户端。
     *
     * @param client 阿里云短信客户端
     * @param smsTemplateParamSerializer 模板变量序列化器
     */
    public AliyunSmsProviderClient(Client client, SmsTemplateParamSerializer smsTemplateParamSerializer) {
        this.client = client;
        this.smsTemplateParamSerializer = smsTemplateParamSerializer;
    }

    /**
     * 发送单条短信。
     *
     * @param request 单条短信请求
     * @return 发送结果
     */
    @Override
    public SmsSendResponse send(SmsSendRequest request) {
        try {
            com.aliyun.dysmsapi20170525.models.SendSmsRequest sendSmsRequest =
                    new com.aliyun.dysmsapi20170525.models.SendSmsRequest()
                            .setPhoneNumbers(request.getPhoneNumber())
                            .setSignName(request.getSignName())
                            .setTemplateCode(request.getTemplateCode())
                            .setTemplateParam(smsTemplateParamSerializer.serialize(request.getTemplateParams()));
            if (request.getOutId() != null) {
                sendSmsRequest.setOutId(request.getOutId());
            }
            return toSendResponse(client.sendSms(sendSmsRequest));
        } catch (SmsException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new SmsException("Failed to send SMS via Aliyun.", ex);
        }
    }

    /**
     * 发送批量短信。
     *
     * @param request 批量短信请求
     * @return 发送结果
     */
    @Override
    public SmsBatchSendResponse sendBatch(SmsBatchSendRequest request) {
        try {
            List<String> phoneNumbers = new ArrayList<>(request.getItems().size());
            List<String> signNames = new ArrayList<>(request.getItems().size());
            List<String> templateParams = new ArrayList<>(request.getItems().size());
            for (SmsBatchSendItem item : request.getItems()) {
                phoneNumbers.add(item.getPhoneNumber());
                signNames.add(item.getSignName());
                templateParams.add(smsTemplateParamSerializer.serialize(item.getTemplateParams()));
            }

            com.aliyun.dysmsapi20170525.models.SendBatchSmsRequest sendBatchSmsRequest =
                    new com.aliyun.dysmsapi20170525.models.SendBatchSmsRequest()
                            .setPhoneNumberJson(JSON.toJSONString(phoneNumbers))
                            .setSignNameJson(JSON.toJSONString(signNames))
                            .setTemplateCode(request.getTemplateCode())
                            .setTemplateParamJson(JSON.toJSONString(templateParams));
            if (request.getOutId() != null) {
                sendBatchSmsRequest.setOutId(request.getOutId());
            }

            return toBatchSendResponse(client.sendBatchSms(sendBatchSmsRequest), request.getItems().size());
        } catch (SmsException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new SmsException("Failed to send batch SMS via Aliyun.", ex);
        }
    }

    /**
     * 转换单条短信响应。
     *
     * @param response 阿里云响应
     * @return 统一响应
     */
    private SmsSendResponse toSendResponse(SendSmsResponse response) {
        SendSmsResponseBody body = response == null ? null : response.getBody();
        if (body == null) {
            throw new SmsException("Aliyun SMS response body must not be null.");
        }

        SmsSendResponse sendResponse = new SmsSendResponse();
        sendResponse.setProvider(SmsProviderType.ALIYUN);
        sendResponse.setSuccess(SmsConstants.ALIYUN_SUCCESS_CODE.equalsIgnoreCase(body.getCode()));
        sendResponse.setCode(body.getCode());
        sendResponse.setMessage(body.getMessage());
        sendResponse.setRequestId(body.getRequestId());
        sendResponse.setBizId(body.getBizId());
        if (!sendResponse.isSuccess()) {
            throw new SmsException("Aliyun SMS send failed. code=" + body.getCode() + ", message=" + body.getMessage());
        }
        return sendResponse;
    }

    /**
     * 转换批量短信响应。
     *
     * @param response 阿里云响应
     * @param recipientCount 接收人数
     * @return 统一响应
     */
    private SmsBatchSendResponse toBatchSendResponse(SendBatchSmsResponse response, int recipientCount) {
        SendBatchSmsResponseBody body = response == null ? null : response.getBody();
        if (body == null) {
            throw new SmsException("Aliyun batch SMS response body must not be null.");
        }

        SmsBatchSendResponse batchSendResponse = new SmsBatchSendResponse();
        batchSendResponse.setProvider(SmsProviderType.ALIYUN);
        batchSendResponse.setSuccess(SmsConstants.ALIYUN_SUCCESS_CODE.equalsIgnoreCase(body.getCode()));
        batchSendResponse.setCode(body.getCode());
        batchSendResponse.setMessage(body.getMessage());
        batchSendResponse.setRequestId(body.getRequestId());
        batchSendResponse.setBizId(body.getBizId());
        batchSendResponse.setRecipientCount(recipientCount);
        if (!batchSendResponse.isSuccess()) {
            throw new SmsException("Aliyun batch SMS send failed. code=" + body.getCode() + ", message=" + body.getMessage());
        }
        return batchSendResponse;
    }
}
