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
package com.getboot.payment.api.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 支付能力统一配置。
 *
 * @author qiheng
 */
@Data
@ConfigurationProperties(prefix = "getboot.payment")
public class PaymentProperties {

    /**
     * 是否启用支付能力。
     */
    private boolean enabled = true;

    /**
     * 支付宝渠道配置。
     */
    private Alipay alipay = new Alipay();

    /**
     * 微信支付渠道配置。
     */
    private WechatPay wechatpay = new WechatPay();

    /**
     * 支付宝渠道配置树。
     */
    @Data
    public static class Alipay {
        /**
         * 是否启用支付宝渠道。
         */
        private boolean enabled;

        /**
         * 支付宝应用 ID。
         */
        private String appId;

        /**
         * 请求协议，默认 https。
         */
        private String protocol = "https";

        /**
         * 网关域名，未配置时按生产/沙箱自动推导。
         */
        private String gatewayHost;

        /**
         * 签名算法，默认 RSA2。
         */
        private String signType = "RSA2";

        /**
         * 商户应用私钥。
         */
        private String merchantPrivateKey;

        /**
         * 支付宝平台公钥。
         */
        private String alipayPublicKey;

        /**
         * 应用公钥证书路径。
         */
        private String merchantCertPath;

        /**
         * 支付宝公钥证书路径。
         */
        private String alipayCertPath;

        /**
         * 支付宝根证书路径。
         */
        private String alipayRootCertPath;

        /**
         * 支付成功后的异步通知地址。
         */
        private String notifyUrl;

        /**
         * 支付完成后的前端返回地址。
         */
        private String returnUrl;

        /**
         * 是否启用支付宝沙箱环境。
         */
        private boolean sandbox;
    }

    /**
     * 微信支付渠道配置树。
     */
    @Data
    public static class WechatPay {
        /**
         * 是否启用微信支付渠道。
         */
        private boolean enabled;

        /**
         * 微信应用 ID。
         */
        private String appId;

        /**
         * 微信支付商户号。
         */
        private String merchantId;

        /**
         * 商户证书序列号。
         */
        private String merchantSerialNumber;

        /**
         * API v3 密钥。
         */
        private String apiV3Key;

        /**
         * API v2 密钥。
         *
         * <p>仅在发券插件、H5 发券、支付分 JSAPI 详情页等 V2 规则签名场景下使用。</p>
         */
        private String apiV2Key;

        /**
         * 商户私钥文件位置。
         */
        private String privateKeyLocation;

        /**
         * 支付成功后的异步通知地址。
         */
        private String notifyUrl;
    }
}
