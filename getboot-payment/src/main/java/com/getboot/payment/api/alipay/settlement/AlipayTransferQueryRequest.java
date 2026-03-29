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
 * 支付宝转账查询请求。
 *
 * @author qiheng
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlipayTransferQueryRequest {

    /**
     * 商户转账请求号，对应 `out_biz_no`。
     */
    private String transferRequestNo;

    /**
     * 支付宝转账订单号，对应 `order_id`。
     */
    private String platformTransferOrderNo;

    /**
     * 业务场景。
     */
    @Builder.Default
    private String businessScene = "DIRECT_TRANSFER";

    /**
     * 产品码。
     */
    @Builder.Default
    private String productCode = "TRANS_ACCOUNT_NO_PWD";

    /**
     * 扩展元数据。
     */
    @Builder.Default
    private Map<String, String> metadata = new LinkedHashMap<>();
}
