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
package com.getboot.payment.api.wechatpay.operation;

import com.getboot.payment.api.wechatpay.operation.businesscircle.WechatPayBusinessCircleService;
import com.getboot.payment.api.wechatpay.operation.coupon.WechatPayCouponService;
import com.getboot.payment.api.wechatpay.operation.payscore.WechatPayPayScoreService;
import com.wechat.pay.java.service.cashcoupons.CashCouponsService;
import com.wechat.pay.java.service.giftactivity.GiftActivityService;
import com.wechat.pay.java.service.marketingbankpackages.MarketingBankPackagesService;
import com.wechat.pay.java.service.marketingbankpackages.MarketingBankPackagesServiceExtension;
import com.wechat.pay.java.service.merchantexclusivecoupon.MerchantExclusiveCouponService;
import com.wechat.pay.java.service.wexinpayscoreparking.WexinPayScoreParkingService;

/**
 * 微信支付运营能力入口。
 *
 * <p>统一收口微信支付运营相关官方 SDK service，避免业务侧自行分散查找。</p>
 *
 * @author qiheng
 */
public interface WechatPayOperationService {

    /**
     * 发券能力。
     *
     * <p>统一承接小程序发券插件与 H5 发券的 V2 规则签名构造。</p>
     *
     * @return 发券能力服务
     */
    WechatPayCouponService couponService();

    /**
     * 微信支付分能力。
     *
     * <p>统一承接支付分订单、退款与前端拉起参数构造。</p>
     *
     * @return 支付分能力服务
     */
    WechatPayPayScoreService payScoreService();

    /**
     * 智慧商圈能力。
     *
     * @return 智慧商圈能力服务
     */
    WechatPayBusinessCircleService businessCircleService();

    /**
     * 代金券能力。
     *
     * @return 代金券 service
     */
    CashCouponsService cashCouponsService();

    /**
     * 商家券能力。
     *
     * @return 商家券 service
     */
    MerchantExclusiveCouponService merchantExclusiveCouponService();

    /**
     * 支付有礼能力。
     *
     * @return 支付有礼 service
     */
    GiftActivityService giftActivityService();

    /**
     * 委托营销能力。
     *
     * @return 委托营销 service
     */
    MarketingBankPackagesService marketingBankPackagesService();

    /**
     * 委托营销扩展能力。
     *
     * @return 委托营销扩展 service
     */
    MarketingBankPackagesServiceExtension marketingBankPackagesServiceExtension();

    /**
     * 微信支付分停车能力。
     *
     * @return 微信支付分停车 service
     */
    WexinPayScoreParkingService payScoreParkingService();
}
