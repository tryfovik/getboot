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
package com.getboot.payment.infrastructure.autoconfigure;

import com.getboot.payment.api.properties.PaymentProperties;
import com.getboot.payment.api.service.PaymentService;
import com.getboot.payment.api.wechatpay.WechatPayApiService;
import com.getboot.payment.api.wechatpay.combine.WechatPayCombinePaymentService;
import com.getboot.payment.api.wechatpay.extension.complaint.WechatPayComplaintService;
import com.getboot.payment.api.wechatpay.operation.WechatPayOperationService;
import com.getboot.payment.api.wechatpay.operation.businesscircle.WechatPayBusinessCircleService;
import com.getboot.payment.api.wechatpay.operation.coupon.WechatPayCouponService;
import com.getboot.payment.api.wechatpay.operation.payscore.WechatPayPayScoreService;
import com.getboot.payment.api.wechatpay.security.WechatPaySecurityService;
import com.getboot.payment.api.wechatpay.settlement.WechatPaySettlementService;
import com.getboot.payment.api.wechatpay.trade.WechatPayTradeService;
import com.getboot.payment.infrastructure.wechatpay.WechatPayApiServiceImpl;
import com.getboot.payment.infrastructure.wechatpay.WechatPayPaymentService;
import com.getboot.payment.infrastructure.wechatpay.combine.WechatPayCombinePaymentServiceImpl;
import com.getboot.payment.infrastructure.wechatpay.extension.complaint.WechatPayComplaintServiceImpl;
import com.getboot.payment.infrastructure.wechatpay.operation.WechatPayOperationServiceImpl;
import com.getboot.payment.infrastructure.wechatpay.operation.businesscircle.WechatPayBusinessCircleServiceImpl;
import com.getboot.payment.infrastructure.wechatpay.operation.coupon.WechatPayCouponServiceImpl;
import com.getboot.payment.infrastructure.wechatpay.operation.payscore.WechatPayPayScoreServiceImpl;
import com.getboot.payment.infrastructure.wechatpay.security.WechatPaySecurityServiceImpl;
import com.getboot.payment.infrastructure.wechatpay.settlement.WechatPaySettlementServiceImpl;
import com.getboot.payment.infrastructure.wechatpay.trade.WechatPayTradeServiceImpl;
import com.getboot.payment.spi.wechatpay.WechatPayRequestCustomizer;
import com.getboot.payment.support.wechatpay.DefaultWechatPayHttpGateway;
import com.getboot.payment.support.wechatpay.WechatPayHttpGateway;
import com.wechat.pay.java.core.Config;
import com.wechat.pay.java.core.RSAAutoCertificateConfig;
import com.wechat.pay.java.core.http.DefaultHttpClientBuilder;
import com.wechat.pay.java.core.http.HttpClient;
import com.wechat.pay.java.core.notification.NotificationParser;
import com.wechat.pay.java.service.billdownload.BillDownloadService;
import com.wechat.pay.java.service.cashcoupons.CashCouponsService;
import com.wechat.pay.java.service.certificate.CertificateService;
import com.wechat.pay.java.service.ecommerceprofitsharing.EcommerceProfitSharingService;
import com.wechat.pay.java.service.file.FileUploadService;
import com.wechat.pay.java.service.giftactivity.GiftActivityService;
import com.wechat.pay.java.service.marketingbankpackages.MarketingBankPackagesService;
import com.wechat.pay.java.service.marketingbankpackages.MarketingBankPackagesServiceExtension;
import com.wechat.pay.java.service.merchantexclusivecoupon.MerchantExclusiveCouponService;
import com.wechat.pay.java.service.payments.app.AppServiceExtension;
import com.wechat.pay.java.service.payments.h5.H5Service;
import com.wechat.pay.java.service.payments.jsapi.JsapiService;
import com.wechat.pay.java.service.payments.jsapi.JsapiServiceExtension;
import com.wechat.pay.java.service.payments.nativepay.NativePayService;
import com.wechat.pay.java.service.profitsharing.ProfitsharingService;
import com.wechat.pay.java.service.refund.RefundService;
import com.wechat.pay.java.service.transferbatch.TransferBatchService;
import com.wechat.pay.java.service.wexinpayscoreparking.WexinPayScoreParkingService;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.Assert;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * 微信支付官方 SDK 自动配置。
 *
 * @author qiheng
 */
@AutoConfiguration(after = PaymentAutoConfiguration.class)
@ConditionalOnClass(RSAAutoCertificateConfig.class)
@ConditionalOnProperty(prefix = "getboot.payment.wechatpay", name = "enabled", havingValue = "true")
public class WechatPayPaymentAutoConfiguration {

    /**
     * 注册微信支付官方配置。
     *
     * @param paymentProperties 支付配置
     * @param resourceLoader    Spring 资源加载器
     * @return 官方 SDK 配置
     */
    @Bean
    @ConditionalOnMissingBean
    public RSAAutoCertificateConfig wechatPayConfig(
            PaymentProperties paymentProperties,
            ResourceLoader resourceLoader) {
        PaymentProperties.WechatPay properties = paymentProperties.getWechatpay();
        validateWechatPayProperties(properties);
        return new RSAAutoCertificateConfig.Builder()
                .merchantId(properties.getMerchantId())
                .merchantSerialNumber(properties.getMerchantSerialNumber())
                .privateKey(loadPrivateKeyContent(properties.getPrivateKeyLocation(), resourceLoader))
                .apiV3Key(properties.getApiV3Key())
                .build();
    }

    /**
     * 注册微信支付通知解析器。
     *
     * @param config 官方 SDK 配置
     * @return 通知解析器
     */
    @Bean
    @ConditionalOnMissingBean
    public NotificationParser wechatPayNotificationParser(RSAAutoCertificateConfig config) {
        return new NotificationParser(config);
    }

    /**
     * 注册已签名 HTTP 客户端。
     *
     * @param config 官方 SDK 配置
     * @return 已签名 HTTP 客户端
     */
    @Bean
    @ConditionalOnMissingBean
    public HttpClient wechatPayHttpClient(RSAAutoCertificateConfig config) {
        return new DefaultHttpClientBuilder()
                .config(config)
                .build();
    }

    /**
     * 注册微信 HTTP 网关。
     *
     * @param httpClient 已签名 HTTP 客户端
     * @return HTTP 网关
     */
    @Bean
    @ConditionalOnMissingBean
    public WechatPayHttpGateway wechatPayHttpGateway(HttpClient httpClient) {
        return new DefaultWechatPayHttpGateway(httpClient);
    }

    /**
     * 注册微信开放接口访问入口。
     *
     * @param httpGateway HTTP 网关
     * @return 开放接口访问入口
     */
    @Bean
    @ConditionalOnMissingBean
    public WechatPayApiService wechatPayApiService(WechatPayHttpGateway httpGateway) {
        return new WechatPayApiServiceImpl(httpGateway);
    }

    /**
     * 注册 JSAPI 服务。
     *
     * @param config 官方 SDK 配置
     * @return JSAPI 服务
     */
    @Bean
    @ConditionalOnMissingBean
    public JsapiService wechatPayJsapiService(RSAAutoCertificateConfig config) {
        return new JsapiService.Builder()
                .config(config)
                .build();
    }

    /**
     * 注册 JSAPI 前端拉起参数服务。
     *
     * @param config 官方 SDK 配置
     * @return JSAPI 扩展服务
     */
    @Bean
    @ConditionalOnMissingBean
    public JsapiServiceExtension wechatPayJsapiServiceExtension(RSAAutoCertificateConfig config) {
        return new JsapiServiceExtension.Builder()
                .config(config)
                .signType("RSA")
                .build();
    }

    /**
     * 注册 App 支付前端拉起参数服务。
     *
     * @param config 官方 SDK 配置
     * @return App 扩展服务
     */
    @Bean
    @ConditionalOnMissingBean
    public AppServiceExtension wechatPayAppServiceExtension(RSAAutoCertificateConfig config) {
        return new AppServiceExtension.Builder()
                .config(config)
                .build();
    }

    /**
     * 注册 H5 支付服务。
     *
     * @param config 官方 SDK 配置
     * @return H5 支付服务
     */
    @Bean
    @ConditionalOnMissingBean
    public H5Service wechatPayH5Service(RSAAutoCertificateConfig config) {
        return new H5Service.Builder()
                .config(config)
                .build();
    }

    /**
     * 注册 Native 支付服务。
     *
     * @param config 官方 SDK 配置
     * @return Native 支付服务
     */
    @Bean
    @ConditionalOnMissingBean
    public NativePayService wechatPayNativePayService(RSAAutoCertificateConfig config) {
        return new NativePayService.Builder()
                .config(config)
                .build();
    }

    /**
     * 注册退款服务。
     *
     * @param config 官方 SDK 配置
     * @return 退款服务
     */
    @Bean
    @ConditionalOnMissingBean
    public RefundService wechatPayRefundService(RSAAutoCertificateConfig config) {
        return new RefundService.Builder()
                .config(config)
                .build();
    }

    /**
     * 注册账单服务。
     *
     * @param config 官方 SDK 配置
     * @return 账单服务
     */
    @Bean
    @ConditionalOnMissingBean
    public BillDownloadService wechatPayBillDownloadService(RSAAutoCertificateConfig config) {
        return new BillDownloadService.Builder()
                .config(config)
                .build();
    }

    /**
     * 注册商家转账服务。
     *
     * @param config 官方 SDK 配置
     * @return 商家转账服务
     */
    @Bean
    @ConditionalOnMissingBean
    public TransferBatchService wechatPayTransferBatchService(RSAAutoCertificateConfig config) {
        return new TransferBatchService.Builder()
                .config(config)
                .build();
    }

    /**
     * 注册普通商户分账服务。
     *
     * @param config 官方 SDK 配置
     * @return 分账服务
     */
    @Bean
    @ConditionalOnMissingBean
    public ProfitsharingService wechatPayProfitsharingService(RSAAutoCertificateConfig config) {
        return new ProfitsharingService.Builder()
                .config(config)
                .build();
    }

    /**
     * 注册电商收付通分账服务。
     *
     * @param config 官方 SDK 配置
     * @return 电商分账服务
     */
    @Bean
    @ConditionalOnMissingBean
    public EcommerceProfitSharingService wechatPayEcommerceProfitSharingService(RSAAutoCertificateConfig config) {
        return new EcommerceProfitSharingService.Builder()
                .config(config)
                .build();
    }

    /**
     * 注册代金券服务。
     *
     * @param config 官方 SDK 配置
     * @return 代金券服务
     */
    @Bean
    @ConditionalOnMissingBean
    public CashCouponsService wechatPayCashCouponsService(RSAAutoCertificateConfig config) {
        return new CashCouponsService.Builder()
                .config(config)
                .build();
    }

    /**
     * 注册商家券服务。
     *
     * @param config 官方 SDK 配置
     * @return 商家券服务
     */
    @Bean
    @ConditionalOnMissingBean
    public MerchantExclusiveCouponService wechatPayMerchantExclusiveCouponService(RSAAutoCertificateConfig config) {
        return new MerchantExclusiveCouponService.Builder()
                .config(config)
                .build();
    }

    /**
     * 注册支付有礼服务。
     *
     * @param config 官方 SDK 配置
     * @return 支付有礼服务
     */
    @Bean
    @ConditionalOnMissingBean
    public GiftActivityService wechatPayGiftActivityService(RSAAutoCertificateConfig config) {
        return new GiftActivityService.Builder()
                .config(config)
                .build();
    }

    /**
     * 注册委托营销服务。
     *
     * @param config 官方 SDK 配置
     * @return 委托营销服务
     */
    @Bean
    @ConditionalOnMissingBean
    public MarketingBankPackagesService wechatPayMarketingBankPackagesService(RSAAutoCertificateConfig config) {
        return new MarketingBankPackagesService.Builder()
                .config(config)
                .build();
    }

    /**
     * 注册委托营销扩展服务。
     *
     * @param config 官方 SDK 配置
     * @return 委托营销扩展服务
     */
    @Bean
    @ConditionalOnMissingBean
    public MarketingBankPackagesServiceExtension wechatPayMarketingBankPackagesServiceExtension(
            RSAAutoCertificateConfig config) {
        return new MarketingBankPackagesServiceExtension.Builder()
                .config(config)
                .build();
    }

    /**
     * 注册微信支付分停车服务。
     *
     * @param config 官方 SDK 配置
     * @return 微信支付分停车服务
     */
    @Bean
    @ConditionalOnMissingBean
    public WexinPayScoreParkingService wechatPayScoreParkingService(RSAAutoCertificateConfig config) {
        return new WexinPayScoreParkingService.Builder()
                .config(config)
                .build();
    }

    /**
     * 注册平台证书服务。
     *
     * @param config 官方 SDK 配置
     * @return 平台证书服务
     */
    @Bean
    @ConditionalOnMissingBean
    public CertificateService wechatPayCertificateService(RSAAutoCertificateConfig config) {
        return new CertificateService.Builder()
                .config(config)
                .build();
    }

    /**
     * 注册媒体文件上传服务。
     *
     * @param config 官方 SDK 配置
     * @return 文件上传服务
     */
    @Bean
    @ConditionalOnMissingBean
    public FileUploadService wechatPayFileUploadService(RSAAutoCertificateConfig config) {
        return new FileUploadService.Builder()
                .config(config)
                .build();
    }

    /**
     * 注册统一微信支付服务。
     *
     * @param paymentProperties     支付配置
     * @param notificationParser    通知解析器
     * @param jsapiService          JSAPI 服务
     * @param jsapiServiceExtension JSAPI 扩展服务
     * @param appServiceExtension   App 扩展服务
     * @param h5Service             H5 服务
     * @param nativePayService      Native 服务
     * @param refundService         退款服务
     * @param requestCustomizers    请求扩展器
     * @return 统一支付服务
     */
    @Bean
    @ConditionalOnMissingBean(WechatPayPaymentService.class)
    public PaymentService wechatPayPaymentService(
            PaymentProperties paymentProperties,
            NotificationParser notificationParser,
            JsapiService jsapiService,
            JsapiServiceExtension jsapiServiceExtension,
            AppServiceExtension appServiceExtension,
            H5Service h5Service,
            NativePayService nativePayService,
            RefundService refundService,
            ObjectProvider<WechatPayRequestCustomizer> requestCustomizers) {
        return new WechatPayPaymentService(
                paymentProperties,
                notificationParser,
                jsapiService,
                jsapiServiceExtension,
                appServiceExtension,
                h5Service,
                nativePayService,
                refundService,
                requestCustomizers.orderedStream().toList()
        );
    }

    /**
     * 注册微信合单支付服务。
     *
     * @param paymentProperties  支付配置
     * @param notificationParser 通知解析器
     * @param config             微信官方配置
     * @param httpGateway        微信 HTTP 网关
     * @param requestCustomizers 请求扩展器
     * @return 微信合单支付服务
     */
    @Bean
    @ConditionalOnMissingBean
    public WechatPayCombinePaymentService wechatPayCombinePaymentService(
            PaymentProperties paymentProperties,
            NotificationParser notificationParser,
            Config config,
            WechatPayHttpGateway httpGateway,
            ObjectProvider<WechatPayRequestCustomizer> requestCustomizers) {
        return new WechatPayCombinePaymentServiceImpl(
                paymentProperties,
                notificationParser,
                config,
                httpGateway,
                requestCustomizers.orderedStream().toList()
        );
    }

    /**
     * 注册微信交易增强服务。
     *
     * @param config              微信官方配置
     * @param billDownloadService 账单服务
     * @param httpGateway         微信 HTTP 网关
     * @param requestCustomizers  请求扩展器
     * @return 微信交易增强服务
     */
    @Bean
    @ConditionalOnMissingBean
    public WechatPayTradeService wechatPayTradeService(
            Config config,
            BillDownloadService billDownloadService,
            WechatPayHttpGateway httpGateway,
            ObjectProvider<WechatPayRequestCustomizer> requestCustomizers) {
        return new WechatPayTradeServiceImpl(
                config,
                billDownloadService,
                httpGateway,
                requestCustomizers.orderedStream().toList()
        );
    }

    /**
     * 注册微信发券能力服务。
     *
     * @param paymentProperties 支付配置
     * @return 微信发券能力服务
     */
    @Bean
    @ConditionalOnMissingBean
    public WechatPayCouponService wechatPayCouponService(PaymentProperties paymentProperties) {
        return new WechatPayCouponServiceImpl(paymentProperties);
    }

    /**
     * 注册微信支付分能力服务。
     *
     * @param paymentProperties 支付配置
     * @param httpGateway       微信 HTTP 网关
     * @return 微信支付分能力服务
     */
    @Bean
    @ConditionalOnMissingBean
    public WechatPayPayScoreService wechatPayPayScoreService(
            PaymentProperties paymentProperties,
            WechatPayHttpGateway httpGateway) {
        return new WechatPayPayScoreServiceImpl(paymentProperties, httpGateway);
    }

    /**
     * 注册微信智慧商圈能力服务。
     *
     * @param httpGateway 微信 HTTP 网关
     * @return 微信智慧商圈能力服务
     */
    @Bean
    @ConditionalOnMissingBean
    public WechatPayBusinessCircleService wechatPayBusinessCircleService(WechatPayHttpGateway httpGateway) {
        return new WechatPayBusinessCircleServiceImpl(httpGateway);
    }

    /**
     * 注册微信运营能力服务。
     *
     * @param couponService                         发券能力服务
     * @param payScoreService                       支付分能力服务
     * @param businessCircleService                 智慧商圈能力服务
     * @param cashCouponsService                    代金券服务
     * @param merchantExclusiveCouponService        商家券服务
     * @param giftActivityService                   支付有礼服务
     * @param marketingBankPackagesService          委托营销服务
     * @param marketingBankPackagesServiceExtension 委托营销扩展服务
     * @param payScoreParkingService                微信支付分停车服务
     * @return 微信运营能力服务
     */
    @Bean
    @ConditionalOnMissingBean
    public WechatPayOperationService wechatPayOperationService(
            WechatPayCouponService couponService,
            WechatPayPayScoreService payScoreService,
            WechatPayBusinessCircleService businessCircleService,
            CashCouponsService cashCouponsService,
            MerchantExclusiveCouponService merchantExclusiveCouponService,
            GiftActivityService giftActivityService,
            MarketingBankPackagesService marketingBankPackagesService,
            MarketingBankPackagesServiceExtension marketingBankPackagesServiceExtension,
            WexinPayScoreParkingService payScoreParkingService) {
        return new WechatPayOperationServiceImpl(
                couponService,
                payScoreService,
                businessCircleService,
                cashCouponsService,
                merchantExclusiveCouponService,
                giftActivityService,
                marketingBankPackagesService,
                marketingBankPackagesServiceExtension,
                payScoreParkingService
        );
    }

    /**
     * 注册微信结算能力服务。
     *
     * @param transferBatchService          商家转账服务
     * @param profitsharingService          普通商户分账服务
     * @param ecommerceProfitSharingService 电商分账服务
     * @return 微信结算能力服务
     */
    @Bean
    @ConditionalOnMissingBean
    public WechatPaySettlementService wechatPaySettlementService(
            TransferBatchService transferBatchService,
            ProfitsharingService profitsharingService,
            EcommerceProfitSharingService ecommerceProfitSharingService) {
        return new WechatPaySettlementServiceImpl(
                transferBatchService,
                profitsharingService,
                ecommerceProfitSharingService
        );
    }

    /**
     * 注册微信安全能力服务。
     *
     * @param config             微信官方配置
     * @param certificateService 平台证书服务
     * @return 微信安全能力服务
     */
    @Bean
    @ConditionalOnMissingBean
    public WechatPaySecurityService wechatPaySecurityService(
            RSAAutoCertificateConfig config,
            CertificateService certificateService) {
        return new WechatPaySecurityServiceImpl(config, certificateService);
    }

    /**
     * 注册微信消费者投诉能力服务。
     *
     * @param fileUploadService 图片上传服务
     * @param httpGateway       微信 HTTP 网关
     * @return 消费者投诉能力服务
     */
    @Bean
    @ConditionalOnMissingBean
    public WechatPayComplaintService wechatPayComplaintService(
            FileUploadService fileUploadService,
            WechatPayHttpGateway httpGateway) {
        return new WechatPayComplaintServiceImpl(fileUploadService, httpGateway);
    }

    private static void validateWechatPayProperties(PaymentProperties.WechatPay properties) {
        Assert.hasText(properties.getAppId(), "getboot.payment.wechatpay.app-id must not be blank");
        Assert.hasText(properties.getMerchantId(), "getboot.payment.wechatpay.merchant-id must not be blank");
        Assert.hasText(
                properties.getMerchantSerialNumber(),
                "getboot.payment.wechatpay.merchant-serial-number must not be blank"
        );
        Assert.hasText(properties.getApiV3Key(), "getboot.payment.wechatpay.api-v3-key must not be blank");
        Assert.hasText(
                properties.getPrivateKeyLocation(),
                "getboot.payment.wechatpay.private-key-location must not be blank"
        );
        Assert.hasText(properties.getNotifyUrl(), "getboot.payment.wechatpay.notify-url must not be blank");
    }

    private static String loadPrivateKeyContent(String location, ResourceLoader resourceLoader) {
        Resource resource = resourceLoader.getResource(location);
        try (InputStream inputStream = resource.getInputStream()) {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to load WeChat Pay private key from " + location, ex);
        }
    }
}
