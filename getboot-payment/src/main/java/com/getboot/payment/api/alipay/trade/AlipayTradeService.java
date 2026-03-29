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

/**
 * 支付宝交易增强能力接口。
 *
 * @author qiheng
 */
public interface AlipayTradeService {

    /**
     * 查询账单下载地址。
     *
     * @param request 查询请求
     * @return 下载地址结果
     */
    AlipayTradeBillResponse downloadBill(AlipayTradeBillRequest request);

    /**
     * 撤销交易。
     *
     * @param request 撤销请求
     * @return 撤销结果
     */
    AlipayTradeCancelResponse cancel(AlipayTradeCancelRequest request);
}
