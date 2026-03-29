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
package com.getboot.payment.api.alipay.settlement;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 支付宝电子回单申请请求。
 *
 * @author qiheng
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlipayElectronicReceiptApplyRequest {

    /**
     * 账单归属支付宝用户 ID。
     *
     * <p>转账场景通常传商户支付宝用户 ID。</p>
     */
    private String billUserId;

    /**
     * 回单类型。
     *
     * <p>转账资金流水场景通常使用 `FUND_DETAIL`。</p>
     */
    @Builder.Default
    private String type = "FUND_DETAIL";

    /**
     * 回单业务键。
     *
     * <p>转账场景通常传 `pay_fund_order_id`。</p>
     */
    private String key;

    /**
     * 扩展元数据。
     */
    @Builder.Default
    private Map<String, String> metadata = new LinkedHashMap<>();
}
