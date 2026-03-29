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
package com.getboot.payment.api.wechatpay.operation.coupon;

/**
 * 微信支付发券能力。
 *
 * <p>统一承接小程序发券插件与 H5 发券的前端参数构造。</p>
 *
 * @author qiheng
 */
public interface WechatPayCouponService {

    /**
     * 构造小程序发券插件参数。
     *
     * @param request 发券请求
     * @return 小程序插件拉起参数
     */
    WechatPayMiniProgramCouponLaunchResponse buildMiniProgramLaunch(
            WechatPayMiniProgramCouponLaunchRequest request);

    /**
     * 构造 H5 发券链接。
     *
     * @param request 发券请求
     * @return H5 发券链接
     */
    WechatPayH5CouponLaunchResponse buildH5Launch(WechatPayH5CouponLaunchRequest request);
}
