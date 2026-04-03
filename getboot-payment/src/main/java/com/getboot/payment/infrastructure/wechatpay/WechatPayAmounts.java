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
package com.getboot.payment.infrastructure.wechatpay;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 微信支付金额转换工具。
 *
 * @author qiheng
 */
public final class WechatPayAmounts {

    /**
     * 工具类不允许实例化。
     */
    private WechatPayAmounts() {
    }

    /**
     * 元转分。
     *
     * @param amount 元金额
     * @return 分金额
     */
    public static Integer toFen(BigDecimal amount) {
        if (amount == null) {
            return null;
        }
        return amount.movePointRight(2).setScale(0, RoundingMode.UNNECESSARY).intValueExact();
    }

    /**
     * 元转分，返回 Long。
     *
     * @param amount 元金额
     * @return 分金额
     */
    public static Long toFenLong(BigDecimal amount) {
        if (amount == null) {
            return null;
        }
        return amount.movePointRight(2).setScale(0, RoundingMode.UNNECESSARY).longValueExact();
    }

    /**
     * 分转元。
     *
     * @param amount 分金额
     * @return 元金额
     */
    public static BigDecimal fromFen(Integer amount) {
        if (amount == null) {
            return null;
        }
        return BigDecimal.valueOf(amount).movePointLeft(2);
    }

    /**
     * 分转元，入参为 Long。
     *
     * @param amount 分金额
     * @return 元金额
     */
    public static BigDecimal fromFen(Long amount) {
        if (amount == null) {
            return null;
        }
        return BigDecimal.valueOf(amount).movePointLeft(2);
    }
}
