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
package com.getboot.payment.api.alipay.trade;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 支付宝交易撤销请求。
 *
 * <p>该能力用于支付结果未知或需立即撤销的场景，与统一主链路中的关单能力区分开。</p>
 *
 * @author qiheng
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlipayTradeCancelRequest {

    /**
     * 商户订单号。
     */
    private String merchantOrderNo;

    /**
     * 请求扩展参数。
     *
     * <p>当前支持 `appAuthToken`、`authToken`、`route` 三类上下文字段。</p>
     */
    @Builder.Default
    private Map<String, String> metadata = new LinkedHashMap<>();
}
