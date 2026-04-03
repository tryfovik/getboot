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

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 微信金额换算工具测试。
 *
 * @author qiheng
 */
class WechatPayAmountsTest {

    /**
     * 验证元到分的转换。
     */
    @Test
    void shouldConvertYuanToFen() {
        assertEquals(1234, WechatPayAmounts.toFen(new BigDecimal("12.34")));
    }

    /**
     * 验证分到元的转换。
     */
    @Test
    void shouldConvertFenToYuan() {
        assertEquals(new BigDecimal("12.34"), WechatPayAmounts.fromFen(1234));
    }
}
