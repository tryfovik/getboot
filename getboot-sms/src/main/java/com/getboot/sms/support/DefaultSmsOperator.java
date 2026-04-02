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
import com.getboot.sms.api.operator.SmsOperator;
import com.getboot.sms.api.properties.SmsProperties;
import com.getboot.sms.api.request.SmsBatchSendItem;
import com.getboot.sms.api.request.SmsBatchSendRequest;
import com.getboot.sms.api.request.SmsSendRequest;
import com.getboot.sms.api.request.SmsVerificationCodeRequest;
import com.getboot.sms.api.response.SmsBatchSendResponse;
import com.getboot.sms.api.response.SmsSendResponse;
import com.getboot.sms.spi.SmsProviderClient;
import com.getboot.sms.spi.SmsSignResolver;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 默认短信发送门面实现。
 *
 * @author qiheng
 */
public class DefaultSmsOperator implements SmsOperator {

    /**
     * 短信供应商客户端。
     */
    private final SmsProviderClient smsProviderClient;

    /**
     * 短信签名解析器。
     */
    private final SmsSignResolver smsSignResolver;

    /**
     * 短信模块配置。
     */
    private final SmsProperties properties;

    /**
     * 构造默认短信发送门面。
     *
     * @param smsProviderClient 短信供应商客户端
     * @param smsSignResolver 短信签名解析器
     * @param properties 短信模块配置
     */
    public DefaultSmsOperator(SmsProviderClient smsProviderClient,
                              SmsSignResolver smsSignResolver,
                              SmsProperties properties) {
        this.smsProviderClient = smsProviderClient;
        this.smsSignResolver = smsSignResolver;
        this.properties = properties;
    }

    /**
     * 发送单条短信。
     *
     * @param request 单条短信请求
     * @return 发送结果
     */
    @Override
    public SmsSendResponse send(SmsSendRequest request) {
        return smsProviderClient.send(resolveSendRequest(request));
    }

    /**
     * 发送批量短信。
     *
     * @param request 批量短信请求
     * @return 发送结果
     */
    @Override
    public SmsBatchSendResponse sendBatch(SmsBatchSendRequest request) {
        return smsProviderClient.sendBatch(resolveBatchSendRequest(request));
    }

    /**
     * 发送验证码短信。
     *
     * @param request 验证码短信请求
     * @return 发送结果
     */
    @Override
    public SmsSendResponse sendVerificationCode(SmsVerificationCodeRequest request) {
        SmsProperties.VerificationScene verificationScene = SmsSupport.requireVerificationScene(
                request == null ? null : request.getScene(),
                properties
        );

        SmsSendRequest sendRequest = new SmsSendRequest();
        sendRequest.setScene(request.getScene());
        sendRequest.setPhoneNumber(request.getPhoneNumber());
        sendRequest.setSignName(StringUtils.hasText(request.getSignName())
                ? request.getSignName()
                : verificationScene.getSignName());
        sendRequest.setTemplateCode(StringUtils.hasText(request.getTemplateCode())
                ? request.getTemplateCode()
                : verificationScene.getTemplateCode());
        sendRequest.setOutId(request.getOutId());

        Map<String, Object> templateParams = SmsSupport.copyTemplateParams(verificationScene.getExtraParams());
        templateParams.putAll(SmsSupport.copyTemplateParams(request.getTemplateParams()));
        String codeParamName = StringUtils.hasText(verificationScene.getCodeParamName())
                ? verificationScene.getCodeParamName().trim()
                : null;
        if (StringUtils.hasText(codeParamName)) {
            templateParams.put(codeParamName, request.getCode());
        }
        if (request.getExpireMinutes() != null && StringUtils.hasText(verificationScene.getExpireMinutesParamName())) {
            templateParams.put(verificationScene.getExpireMinutesParamName().trim(), request.getExpireMinutes());
        }
        sendRequest.setTemplateParams(templateParams);
        return send(sendRequest);
    }

    /**
     * 规整单条短信请求。
     *
     * @param request 原始请求
     * @return 规整后的请求
     */
    private SmsSendRequest resolveSendRequest(SmsSendRequest request) {
        SmsSendRequest resolvedRequest = new SmsSendRequest();
        resolvedRequest.setScene(request == null ? null : request.getScene());
        resolvedRequest.setPhoneNumber(SmsSupport.requirePhoneNumber(request == null ? null : request.getPhoneNumber()));
        resolvedRequest.setSignName(SmsSupport.requireSignName(smsSignResolver.resolveSignName(
                request == null ? null : request.getScene(),
                request == null ? null : request.getSignName()
        )));
        resolvedRequest.setTemplateCode(SmsSupport.requireTemplateCode(request == null ? null : request.getTemplateCode()));
        resolvedRequest.setOutId(SmsSupport.normalizeOptionalText(request == null ? null : request.getOutId()));
        resolvedRequest.setTemplateParams(SmsSupport.copyTemplateParams(request == null ? null : request.getTemplateParams()));
        return resolvedRequest;
    }

    /**
     * 规整批量短信请求。
     *
     * @param request 原始请求
     * @return 规整后的请求
     */
    private SmsBatchSendRequest resolveBatchSendRequest(SmsBatchSendRequest request) {
        SmsBatchSendRequest resolvedRequest = new SmsBatchSendRequest();
        resolvedRequest.setScene(request == null ? null : request.getScene());
        resolvedRequest.setTemplateCode(SmsSupport.requireTemplateCode(request == null ? null : request.getTemplateCode()));
        resolvedRequest.setOutId(SmsSupport.normalizeOptionalText(request == null ? null : request.getOutId()));

        List<SmsBatchSendItem> items = request == null ? null : request.getItems();
        if (items == null || items.isEmpty()) {
            throw new SmsException("SMS batch items must not be empty.");
        }

        List<SmsBatchSendItem> resolvedItems = new ArrayList<>(items.size());
        for (SmsBatchSendItem item : items) {
            SmsBatchSendItem resolvedItem = new SmsBatchSendItem();
            resolvedItem.setPhoneNumber(SmsSupport.requirePhoneNumber(item == null ? null : item.getPhoneNumber()));
            resolvedItem.setSignName(SmsSupport.requireSignName(smsSignResolver.resolveSignName(
                    resolvedRequest.getScene(),
                    item == null ? null : item.getSignName()
            )));
            resolvedItem.setTemplateParams(SmsSupport.copyTemplateParams(item == null ? null : item.getTemplateParams()));
            resolvedItems.add(resolvedItem);
        }
        resolvedRequest.setItems(resolvedItems);
        return resolvedRequest;
    }
}
