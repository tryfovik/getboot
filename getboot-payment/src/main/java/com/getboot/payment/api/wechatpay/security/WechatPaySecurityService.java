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
package com.getboot.payment.api.wechatpay.security;

import com.wechat.pay.java.service.certificate.CertificateService;

import java.security.cert.X509Certificate;
import java.util.List;

/**
 * 微信支付安全能力入口。
 *
 * @author qiheng
 */
public interface WechatPaySecurityService {

    /**
     * 获取平台证书 service。
     *
     * @return 平台证书 service
     */
    CertificateService certificateService();

    /**
     * 下载当前平台证书列表。
     *
     * @return 平台证书集合
     */
    List<X509Certificate> downloadPlatformCertificates();

    /**
     * 加密敏感字段。
     *
     * @param value 明文
     * @return 密文
     */
    String encryptSensitiveValue(String value);

    /**
     * 获取当前加密使用的平台证书序列号。
     *
     * @return 平台证书序列号
     */
    String currentWechatPaySerialNumber();
}
