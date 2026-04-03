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
package com.getboot.payment.infrastructure.wechatpay.settlement;

import com.getboot.payment.api.wechatpay.settlement.WechatPaySettlementService;
import com.wechat.pay.java.service.ecommerceprofitsharing.EcommerceProfitSharingService;
import com.wechat.pay.java.service.profitsharing.ProfitsharingService;
import com.wechat.pay.java.service.transferbatch.TransferBatchService;

/**
 * 微信支付结算能力默认实现。
 *
 * @author qiheng
 */
public class WechatPaySettlementServiceImpl implements WechatPaySettlementService {

    /**
     * 商家转账服务。
     */
    private final TransferBatchService transferBatchService;

    /**
     * 普通商户分账服务。
     */
    private final ProfitsharingService profitsharingService;

    /**
     * 电商分账服务。
     */
    private final EcommerceProfitSharingService ecommerceProfitSharingService;

    /**
     * 构造结算能力服务。
     *
     * @param transferBatchService          商家转账 service
     * @param profitsharingService          普通商户分账 service
     * @param ecommerceProfitSharingService 电商分账 service
     */
    public WechatPaySettlementServiceImpl(
            TransferBatchService transferBatchService,
            ProfitsharingService profitsharingService,
            EcommerceProfitSharingService ecommerceProfitSharingService) {
        this.transferBatchService = transferBatchService;
        this.profitsharingService = profitsharingService;
        this.ecommerceProfitSharingService = ecommerceProfitSharingService;
    }

    /**
     * 获取商家转账服务。
     *
     * @return 商家转账服务
     */
    @Override
    public TransferBatchService transferBatchService() {
        return transferBatchService;
    }

    /**
     * 获取普通商户分账服务。
     *
     * @return 普通商户分账服务
     */
    @Override
    public ProfitsharingService profitsharingService() {
        return profitsharingService;
    }

    /**
     * 获取电商分账服务。
     *
     * @return 电商分账服务
     */
    @Override
    public EcommerceProfitSharingService ecommerceProfitSharingService() {
        return ecommerceProfitSharingService;
    }
}
