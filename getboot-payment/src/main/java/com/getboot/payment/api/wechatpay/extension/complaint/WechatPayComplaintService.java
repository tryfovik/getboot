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
package com.getboot.payment.api.wechatpay.extension.complaint;

/**
 * 微信支付消费者投诉能力。
 *
 * @author qiheng
 */
public interface WechatPayComplaintService {

    /**
     * 上传投诉处理图片。
     *
     * @param request 上传请求
     * @return 上传响应
     */
    WechatPayComplaintImageUploadResponse uploadImage(WechatPayComplaintImageUploadRequest request);

    /**
     * 回复投诉。
     *
     * @param complaintId 投诉单号
     * @param requestBody 官方请求体
     */
    void reply(String complaintId, Object requestBody);

    /**
     * 完结投诉。
     *
     * @param complaintId 投诉单号
     * @param requestBody 官方请求体
     */
    void complete(String complaintId, Object requestBody);

    /**
     * 更新退款进度。
     *
     * @param complaintId 投诉单号
     * @param requestBody 官方请求体
     */
    void updateRefundProgress(String complaintId, Object requestBody);

    /**
     * 回复立即处理。
     *
     * @param complaintId 投诉单号
     * @param requestBody 官方请求体
     */
    void replyImmediateService(String complaintId, Object requestBody);
}
