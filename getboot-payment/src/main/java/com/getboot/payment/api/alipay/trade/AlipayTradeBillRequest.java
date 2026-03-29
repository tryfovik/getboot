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
 * 支付宝账单下载地址查询请求。
 *
 * @author qiheng
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlipayTradeBillRequest {

    /**
     * 账单类型，例如 `trade`。
     */
    private String billType;

    /**
     * 账单日期，按官方接口要求传入 `yyyy-MM-dd` 或 `yyyy-MM`。
     */
    private String billDate;

    /**
     * 请求扩展参数。
     *
     * <p>当前支持 `appAuthToken`、`authToken`、`route` 三类上下文字段。</p>
     */
    @Builder.Default
    private Map<String, String> metadata = new LinkedHashMap<>();
}
