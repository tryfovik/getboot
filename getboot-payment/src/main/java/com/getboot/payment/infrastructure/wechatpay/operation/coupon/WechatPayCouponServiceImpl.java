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
package com.getboot.payment.infrastructure.wechatpay.operation.coupon;

import com.getboot.exception.api.exception.BusinessException;
import com.getboot.payment.api.properties.PaymentProperties;
import com.getboot.payment.api.wechatpay.operation.coupon.WechatPayCouponService;
import com.getboot.payment.api.wechatpay.operation.coupon.WechatPayH5CouponLaunchRequest;
import com.getboot.payment.api.wechatpay.operation.coupon.WechatPayH5CouponLaunchResponse;
import com.getboot.payment.api.wechatpay.operation.coupon.WechatPayMiniProgramCouponLaunchRequest;
import com.getboot.payment.api.wechatpay.operation.coupon.WechatPayMiniProgramCouponLaunchResponse;
import com.getboot.payment.support.wechatpay.WechatPayV2Support;
import com.wechat.pay.java.core.util.GsonUtil;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 微信支付发券能力默认实现。
 *
 * @author qiheng
 */
public class WechatPayCouponServiceImpl implements WechatPayCouponService {

    /**
     * H5 领券基础地址。
     */
    private static final String H5_COUPON_BASE_URL = "https://action.weixin.qq.com/busifavor/getcouponinfo";

    /**
     * H5 领券地址固定后缀。
     */
    private static final String H5_COUPON_SUFFIX = "#wechat_pay&wechat_redirect";

    /**
     * 支付配置。
     */
    private final PaymentProperties paymentProperties;

    /**
     * 构造发券能力服务。
     *
     * @param paymentProperties 支付配置
     */
    public WechatPayCouponServiceImpl(PaymentProperties paymentProperties) {
        this.paymentProperties = paymentProperties;
    }

    /**
     * 构建小程序发券插件拉起参数。
     *
     * @param request 小程序发券请求
     * @return 小程序发券响应
     */
    @Override
    public WechatPayMiniProgramCouponLaunchResponse buildMiniProgramLaunch(
            WechatPayMiniProgramCouponLaunchRequest request) {
        Assert.notNull(request, "request must not be null");
        if (CollectionUtils.isEmpty(request.getCoupons())) {
            throw new BusinessException("At least one coupon is required");
        }
        if (request.getCoupons().size() > 10) {
            throw new BusinessException("Mini program coupon plugin supports at most 10 coupons");
        }

        String sendCouponMerchant = resolveSendCouponMerchant(request.getSendCouponMerchant());
        List<Map<String, Object>> pluginCoupons = new ArrayList<>(request.getCoupons().size());
        Map<String, Object> signArgs = new LinkedHashMap<>();
        signArgs.put("send_coupon_merchant", sendCouponMerchant);

        for (int index = 0; index < request.getCoupons().size(); index++) {
            WechatPayMiniProgramCouponLaunchRequest.Coupon coupon = request.getCoupons().get(index);
            Assert.hasText(coupon.getStockId(), "coupon.stockId must not be blank");
            Assert.hasText(coupon.getOutRequestNo(), "coupon.outRequestNo must not be blank");

            Map<String, Object> pluginCoupon = new LinkedHashMap<>();
            pluginCoupon.put("stock_id", coupon.getStockId());
            pluginCoupon.put("out_request_no", coupon.getOutRequestNo());
            if (StringUtils.hasText(coupon.getCouponCode())) {
                pluginCoupon.put("coupon_code", coupon.getCouponCode());
                signArgs.put("coupon_code" + index, coupon.getCouponCode());
            }
            pluginCoupons.add(pluginCoupon);
            signArgs.put("out_request_no" + index, coupon.getOutRequestNo());
            signArgs.put("stock_id" + index, coupon.getStockId());
        }

        return WechatPayMiniProgramCouponLaunchResponse.builder()
                .sendCouponMerchant(sendCouponMerchant)
                .sendCouponParamsJson(GsonUtil.toJson(pluginCoupons))
                .sign(v2Support().sign(signArgs))
                .build();
    }

    /**
     * 构建 H5 领券拉起地址。
     *
     * @param request H5 发券请求
     * @return H5 发券响应
     */
    @Override
    public WechatPayH5CouponLaunchResponse buildH5Launch(WechatPayH5CouponLaunchRequest request) {
        Assert.notNull(request, "request must not be null");
        Assert.hasText(request.getStockId(), "request.stockId must not be blank");
        Assert.hasText(request.getOutRequestNo(), "request.outRequestNo must not be blank");
        Assert.hasText(request.getOpenId(), "request.openId must not be blank");

        String sendCouponMerchant = resolveSendCouponMerchant(request.getSendCouponMerchant());
        WechatPayV2Support support = v2Support();

        Map<String, Object> signArgs = new LinkedHashMap<>();
        signArgs.put("stock_id", request.getStockId());
        signArgs.put("out_request_no", request.getOutRequestNo());
        signArgs.put("send_coupon_merchant", sendCouponMerchant);
        signArgs.put("open_id", request.getOpenId());
        if (StringUtils.hasText(request.getCouponCode())) {
            signArgs.put("coupon_code", request.getCouponCode());
        }

        String sign = support.sign(signArgs);

        Map<String, Object> queryArgs = new LinkedHashMap<>();
        queryArgs.put("stock_id", request.getStockId());
        queryArgs.put("out_request_no", request.getOutRequestNo());
        queryArgs.put("sign", sign);
        queryArgs.put("send_coupon_merchant", sendCouponMerchant);
        queryArgs.put("open_id", request.getOpenId());
        if (StringUtils.hasText(request.getCouponCode())) {
            queryArgs.put("coupon_code", request.getCouponCode());
        }

        return WechatPayH5CouponLaunchResponse.builder()
                .sign(sign)
                .url(H5_COUPON_BASE_URL + "?" + support.buildQueryString(queryArgs) + H5_COUPON_SUFFIX)
                .build();
    }

    /**
     * 解析发券商户号。
     *
     * @param sendCouponMerchant 优先商户号
     * @return 发券商户号
     */
    private String resolveSendCouponMerchant(String sendCouponMerchant) {
        if (StringUtils.hasText(sendCouponMerchant)) {
            return sendCouponMerchant;
        }
        String merchantId = paymentProperties.getWechatpay().getMerchantId();
        Assert.hasText(merchantId, "getboot.payment.wechatpay.merchant-id must not be blank");
        return merchantId;
    }

    /**
     * 创建 V2 签名支持工具。
     *
     * @return V2 签名支持工具
     */
    private WechatPayV2Support v2Support() {
        return new WechatPayV2Support(paymentProperties.getWechatpay().getApiV2Key());
    }
}
