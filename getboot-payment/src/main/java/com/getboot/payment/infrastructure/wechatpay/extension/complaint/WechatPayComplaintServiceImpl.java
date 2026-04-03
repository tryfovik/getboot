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
package com.getboot.payment.infrastructure.wechatpay.extension.complaint;

import com.getboot.exception.api.exception.BusinessException;
import com.getboot.payment.api.wechatpay.extension.complaint.WechatPayComplaintImageUploadRequest;
import com.getboot.payment.api.wechatpay.extension.complaint.WechatPayComplaintImageUploadResponse;
import com.getboot.payment.api.wechatpay.extension.complaint.WechatPayComplaintService;
import com.getboot.payment.support.wechatpay.WechatPayHttpGateway;
import com.wechat.pay.java.service.file.FileUploadService;
import com.wechat.pay.java.service.file.model.FileUploadResponse;
import org.springframework.util.Assert;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 微信支付消费者投诉能力默认实现。
 *
 * @author qiheng
 */
public class WechatPayComplaintServiceImpl implements WechatPayComplaintService {

    /**
     * 图片上传服务。
     */
    private final FileUploadService fileUploadService;

    /**
     * 微信 HTTP 网关。
     */
    private final WechatPayHttpGateway httpGateway;

    /**
     * 构造消费者投诉服务。
     *
     * @param fileUploadService 图片上传服务
     * @param httpGateway       微信 HTTP 网关
     */
    public WechatPayComplaintServiceImpl(
            FileUploadService fileUploadService,
            WechatPayHttpGateway httpGateway) {
        this.fileUploadService = fileUploadService;
        this.httpGateway = httpGateway;
    }

    /**
     * 上传投诉处理图片。
     *
     * @param request 图片上传请求
     * @return 图片上传响应
     */
    @Override
    public WechatPayComplaintImageUploadResponse uploadImage(WechatPayComplaintImageUploadRequest request) {
        Assert.notNull(request, "request must not be null");
        Assert.hasText(request.getFilename(), "request.filename must not be blank");
        Assert.hasText(request.getContentType(), "request.contentType must not be blank");
        Assert.hasText(request.getFilePath(), "request.filePath must not be blank");

        try {
            FileUploadResponse response = fileUploadService.uploadImage(
                    request.getFilename(),
                    request.getContentType(),
                    request.getFilePath()
            );
            return WechatPayComplaintImageUploadResponse.builder()
                    .mediaId(response.getMediaId())
                    .build();
        } catch (Exception ex) {
            throw new BusinessException("Failed to upload WeChat Pay complaint image", ex);
        }
    }

    /**
     * 回复投诉。
     *
     * @param complaintId 投诉单号
     * @param requestBody 回复请求体
     */
    @Override
    public void reply(String complaintId, Object requestBody) {
        httpGateway.postWithoutResponse(
                "/v3/merchant-service/complaints-v2/" + urlEncodeComplaintId(complaintId) + "/response",
                requestBody
        );
    }

    /**
     * 完结投诉。
     *
     * @param complaintId 投诉单号
     * @param requestBody 完结请求体
     */
    @Override
    public void complete(String complaintId, Object requestBody) {
        httpGateway.postWithoutResponse(
                "/v3/merchant-service/complaints-v2/" + urlEncodeComplaintId(complaintId) + "/complete",
                requestBody
        );
    }

    /**
     * 更新退款进度。
     *
     * @param complaintId 投诉单号
     * @param requestBody 退款进度请求体
     */
    @Override
    public void updateRefundProgress(String complaintId, Object requestBody) {
        httpGateway.postWithoutResponse(
                "/v3/merchant-service/complaints-v2/" + urlEncodeComplaintId(complaintId)
                        + "/update-refund-progress",
                requestBody
        );
    }

    /**
     * 回复立即处理服务请求。
     *
     * @param complaintId 投诉单号
     * @param requestBody 回复请求体
     */
    @Override
    public void replyImmediateService(String complaintId, Object requestBody) {
        httpGateway.postWithoutResponse(
                "/v3/merchant-service/complaints-v2/" + urlEncodeComplaintId(complaintId)
                        + "/response-immediate-service",
                requestBody
        );
    }

    /**
     * 对投诉单号进行 URL 编码。
     *
     * @param complaintId 投诉单号
     * @return 编码后的投诉单号
     */
    private String urlEncodeComplaintId(String complaintId) {
        Assert.hasText(complaintId, "complaintId must not be blank");
        return URLEncoder.encode(complaintId, StandardCharsets.UTF_8).replace("+", "%20");
    }
}
