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
package com.getboot.payment.api.model;

/**
 * 支付方式枚举。
 *
 * <p>同一个渠道可支持多种支付方式，例如微信同时支持 JSAPI、APP、H5、Native。</p>
 *
 * @author qiheng
 */
public enum PaymentMode {

    /**
     * 公众号支付。
     */
    JSAPI,

    /**
     * 小程序支付。
     */
    MINI_PROGRAM,

    /**
     * App 支付。
     */
    APP,

    /**
     * H5 支付。
     */
    H5,

    /**
     * Native 扫码支付。
     */
    NATIVE,

    /**
     * PC 页面支付。
     */
    PAGE,

    /**
     * 手机网页支付。
     */
    WAP
}
