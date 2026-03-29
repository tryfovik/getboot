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
package com.getboot.payment.infrastructure.wechatpay.operation;

import com.getboot.payment.api.wechatpay.operation.businesscircle.WechatPayBusinessCircleService;
import com.getboot.payment.api.wechatpay.operation.coupon.WechatPayCouponService;
import com.getboot.payment.api.wechatpay.operation.payscore.WechatPayPayScoreService;
import com.getboot.payment.api.wechatpay.operation.WechatPayOperationService;
import com.wechat.pay.java.service.cashcoupons.CashCouponsService;
import com.wechat.pay.java.service.giftactivity.GiftActivityService;
import com.wechat.pay.java.service.marketingbankpackages.MarketingBankPackagesService;
import com.wechat.pay.java.service.marketingbankpackages.MarketingBankPackagesServiceExtension;
import com.wechat.pay.java.service.merchantexclusivecoupon.MerchantExclusiveCouponService;
import com.wechat.pay.java.service.wexinpayscoreparking.WexinPayScoreParkingService;

/**
 * 微信支付运营能力默认实现。
 *
 * @author qiheng
 */
public class WechatPayOperationServiceImpl implements WechatPayOperationService {

    private final WechatPayCouponService couponService;
    private final WechatPayPayScoreService payScoreService;
    private final WechatPayBusinessCircleService businessCircleService;
    private final CashCouponsService cashCouponsService;
    private final MerchantExclusiveCouponService merchantExclusiveCouponService;
    private final GiftActivityService giftActivityService;
    private final MarketingBankPackagesService marketingBankPackagesService;
    private final MarketingBankPackagesServiceExtension marketingBankPackagesServiceExtension;
    private final WexinPayScoreParkingService payScoreParkingService;

    /**
     * 构造运营能力服务。
     *
     * @param couponService                     发券能力服务
     * @param payScoreService                   支付分能力服务
     * @param businessCircleService             智慧商圈能力服务
     * @param cashCouponsService                 代金券 service
     * @param merchantExclusiveCouponService     商家券 service
     * @param giftActivityService                支付有礼 service
     * @param marketingBankPackagesService       委托营销 service
     * @param marketingBankPackagesServiceExtension 委托营销扩展 service
     * @param payScoreParkingService             微信支付分停车 service
     */
    public WechatPayOperationServiceImpl(
            WechatPayCouponService couponService,
            WechatPayPayScoreService payScoreService,
            WechatPayBusinessCircleService businessCircleService,
            CashCouponsService cashCouponsService,
            MerchantExclusiveCouponService merchantExclusiveCouponService,
            GiftActivityService giftActivityService,
            MarketingBankPackagesService marketingBankPackagesService,
            MarketingBankPackagesServiceExtension marketingBankPackagesServiceExtension,
            WexinPayScoreParkingService payScoreParkingService) {
        this.couponService = couponService;
        this.payScoreService = payScoreService;
        this.businessCircleService = businessCircleService;
        this.cashCouponsService = cashCouponsService;
        this.merchantExclusiveCouponService = merchantExclusiveCouponService;
        this.giftActivityService = giftActivityService;
        this.marketingBankPackagesService = marketingBankPackagesService;
        this.marketingBankPackagesServiceExtension = marketingBankPackagesServiceExtension;
        this.payScoreParkingService = payScoreParkingService;
    }

    @Override
    public WechatPayCouponService couponService() {
        return couponService;
    }

    @Override
    public WechatPayPayScoreService payScoreService() {
        return payScoreService;
    }

    @Override
    public WechatPayBusinessCircleService businessCircleService() {
        return businessCircleService;
    }

    @Override
    public CashCouponsService cashCouponsService() {
        return cashCouponsService;
    }

    @Override
    public MerchantExclusiveCouponService merchantExclusiveCouponService() {
        return merchantExclusiveCouponService;
    }

    @Override
    public GiftActivityService giftActivityService() {
        return giftActivityService;
    }

    @Override
    public MarketingBankPackagesService marketingBankPackagesService() {
        return marketingBankPackagesService;
    }

    @Override
    public MarketingBankPackagesServiceExtension marketingBankPackagesServiceExtension() {
        return marketingBankPackagesServiceExtension;
    }

    @Override
    public WexinPayScoreParkingService payScoreParkingService() {
        return payScoreParkingService;
    }
}
