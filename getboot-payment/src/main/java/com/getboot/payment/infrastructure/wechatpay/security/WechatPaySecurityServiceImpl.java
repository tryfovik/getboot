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

    /**
     * 微信支付自动更新证书配置。
     */
    private final RSAAutoCertificateConfig config;

    /**
     * 平台证书服务。
     */
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

    /**
     * 获取平台证书服务。
     *
     * @return 平台证书服务
     */
    @Override
    public CertificateService certificateService() {
        return certificateService;
    }

    /**
     * 下载微信支付平台证书。
     *
     * @return 平台证书列表
     */
    @Override
    public List<X509Certificate> downloadPlatformCertificates() {
        return certificateService.downloadCertificate(config.createAeadCipher());
    }

    /**
     * 加密敏感字段值。
     *
     * @param value 待加密值
     * @return 加密后的密文
     */
    @Override
    public String encryptSensitiveValue(String value) {
        return config.createEncryptor().encrypt(value);
    }

    /**
     * 获取当前微信支付平台证书序列号。
     *
     * @return 平台证书序列号
     */
    @Override
    public String currentWechatPaySerialNumber() {
        return config.createEncryptor().getWechatpaySerial();
    }
}
