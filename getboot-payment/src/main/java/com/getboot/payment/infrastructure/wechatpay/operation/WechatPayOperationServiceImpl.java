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

    /**
     * 发券能力服务。
     */
    private final WechatPayCouponService couponService;

    /**
     * 支付分能力服务。
     */
    private final WechatPayPayScoreService payScoreService;

    /**
     * 智慧商圈能力服务。
     */
    private final WechatPayBusinessCircleService businessCircleService;

    /**
     * 代金券服务。
     */
    private final CashCouponsService cashCouponsService;

    /**
     * 商家券服务。
     */
    private final MerchantExclusiveCouponService merchantExclusiveCouponService;

    /**
     * 支付有礼服务。
     */
    private final GiftActivityService giftActivityService;

    /**
     * 委托营销服务。
     */
    private final MarketingBankPackagesService marketingBankPackagesService;

    /**
     * 委托营销扩展服务。
     */
    private final MarketingBankPackagesServiceExtension marketingBankPackagesServiceExtension;

    /**
     * 微信支付分停车服务。
     */
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

    /**
     * 获取发券能力服务。
     *
     * @return 发券能力服务
     */
    @Override
    public WechatPayCouponService couponService() {
        return couponService;
    }

    /**
     * 获取支付分能力服务。
     *
     * @return 支付分能力服务
     */
    @Override
    public WechatPayPayScoreService payScoreService() {
        return payScoreService;
    }

    /**
     * 获取智慧商圈能力服务。
     *
     * @return 智慧商圈能力服务
     */
    @Override
    public WechatPayBusinessCircleService businessCircleService() {
        return businessCircleService;
    }

    /**
     * 获取代金券服务。
     *
     * @return 代金券服务
     */
    @Override
    public CashCouponsService cashCouponsService() {
        return cashCouponsService;
    }

    /**
     * 获取商家券服务。
     *
     * @return 商家券服务
     */
    @Override
    public MerchantExclusiveCouponService merchantExclusiveCouponService() {
        return merchantExclusiveCouponService;
    }

    /**
     * 获取支付有礼服务。
     *
     * @return 支付有礼服务
     */
    @Override
    public GiftActivityService giftActivityService() {
        return giftActivityService;
    }

    /**
     * 获取委托营销服务。
     *
     * @return 委托营销服务
     */
    @Override
    public MarketingBankPackagesService marketingBankPackagesService() {
        return marketingBankPackagesService;
    }

    /**
     * 获取委托营销扩展服务。
     *
     * @return 委托营销扩展服务
     */
    @Override
    public MarketingBankPackagesServiceExtension marketingBankPackagesServiceExtension() {
        return marketingBankPackagesServiceExtension;
    }

    /**
     * 获取微信支付分停车服务。
     *
     * @return 微信支付分停车服务
     */
    @Override
    public WexinPayScoreParkingService payScoreParkingService() {
        return payScoreParkingService;
    }
}
