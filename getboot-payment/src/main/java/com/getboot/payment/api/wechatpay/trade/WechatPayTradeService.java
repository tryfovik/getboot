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
package com.getboot.payment.api.wechatpay.trade;

/**
 * 微信支付交易增强能力接口。
 *
 * <p>承接统一支付主链路之外，但仍应归于支付域的微信交易扩展能力。</p>
 *
 * @author qiheng
 */
public interface WechatPayTradeService {

    /**
     * 发起异常退款。
     *
     * @param request 异常退款请求
     * @return 处理结果
     */
    WechatPayAbnormalRefundResponse abnormalRefund(WechatPayAbnormalRefundRequest request);

    /**
     * 申请交易账单下载地址。
     *
     * @param request 账单请求
     * @return 账单结果
     */
    WechatPayBillResponse queryTradeBill(WechatPayTradeBillRequest request);

    /**
     * 申请资金账单下载地址。
     *
     * @param request 账单请求
     * @return 账单结果
     */
    WechatPayBillResponse queryFundFlowBill(WechatPayFundFlowBillRequest request);
}
