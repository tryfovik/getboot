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
package com.getboot.payment.infrastructure.wechatpay.security;

import com.getboot.payment.api.wechatpay.security.WechatPaySecurityService;
import com.wechat.pay.java.core.RSAAutoCertificateConfig;
import com.wechat.pay.java.service.certificate.CertificateService;

import java.security.cert.X509Certificate;
import java.util.List;

/**
 * 微信支付安全能力默认实现。
 *
 * @author qiheng
 */
public class WechatPaySecurityServiceImpl implements WechatPaySecurityService {

    private final RSAAutoCertificateConfig config;
    private final CertificateService certificateService;

    /**
     * 构造安全能力服务。
     *
     * @param config             微信官方配置
     * @param certificateService 平台证书 service
     */
    public WechatPaySecurityServiceImpl(
            RSAAutoCertificateConfig config,
            CertificateService certificateService) {
        this.config = config;
        this.certificateService = certificateService;
    }

    @Override
    public CertificateService certificateService() {
        return certificateService;
    }

    @Override
    public List<X509Certificate> downloadPlatformCertificates() {
        return certificateService.downloadCertificate(config.createAeadCipher());
    }

    @Override
    public String encryptSensitiveValue(String value) {
        return config.createEncryptor().encrypt(value);
    }

    @Override
    public String currentWechatPaySerialNumber() {
        return config.createEncryptor().getWechatpaySerial();
    }
}
