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

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 支付宝单笔转账请求。
 *
 * @author qiheng
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlipayTransferRequest {

    /**
     * 商户转账请求号，对应 `out_biz_no`。
     */
    private String transferRequestNo;

    /**
     * 转账金额。
     */
    private BigDecimal amount;

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
     * 转账标题。
     */
    private String orderTitle;

    /**
     * 收款方标识。
     */
    private String payeeIdentity;

    /**
     * 收款方标识类型。
     */
    @Builder.Default
    private String payeeIdentityType = "ALIPAY_LOGON_ID";

    /**
     * 收款方姓名。
     */
    private String payeeName;

    /**
     * 转账备注。
     */
    private String remark;

    /**
     * 扩展元数据。
     */
    @Builder.Default
    private Map<String, String> metadata = new LinkedHashMap<>();
}
