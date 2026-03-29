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
package com.getboot.payment.api.wechatpay.settlement;

import com.wechat.pay.java.service.ecommerceprofitsharing.EcommerceProfitSharingService;
import com.wechat.pay.java.service.profitsharing.ProfitsharingService;
import com.wechat.pay.java.service.transferbatch.TransferBatchService;

/**
 * 微信支付结算能力入口。
 *
 * @author qiheng
 */
public interface WechatPaySettlementService {

    /**
     * 商家转账能力。
     *
     * @return 商家转账 service
     */
    TransferBatchService transferBatchService();

    /**
     * 普通商户分账能力。
     *
     * @return 分账 service
     */
    ProfitsharingService profitsharingService();

    /**
     * 电商收付通分账能力。
     *
     * @return 电商分账 service
     */
    EcommerceProfitSharingService ecommerceProfitSharingService();
}
