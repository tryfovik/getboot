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

/**
 * 支付宝结算能力入口。
 *
 * <p>当前承载单笔转账、转账通知、账户查询与电子回单等资金能力。</p>
 *
 * @author qiheng
 */
public interface AlipaySettlementService {

    /**
     * 发起单笔转账。
     *
     * @param request 转账请求
     * @return 转账结果
     */
    AlipayTransferResponse transfer(AlipayTransferRequest request);

    /**
     * 解析转账异步通知。
     *
     * @param request 通知请求
     * @return 通知结果
     */
    AlipayTransferNotifyResponse parseTransferNotify(AlipayTransferNotifyRequest request);

    /**
     * 查询单笔转账。
     *
     * @param request 查询请求
     * @return 查询结果
     */
    AlipayTransferQueryResponse queryTransfer(AlipayTransferQueryRequest request);

    /**
     * 查询支付宝账户余额。
     *
     * @param request 查询请求
     * @return 余额结果
     */
    AlipayAccountQueryResponse queryAccount(AlipayAccountQueryRequest request);

    /**
     * 申请电子回单。
     *
     * @param request 申请请求
     * @return 申请结果
     */
    AlipayElectronicReceiptApplyResponse applyElectronicReceipt(AlipayElectronicReceiptApplyRequest request);

    /**
     * 查询电子回单。
     *
     * @param request 查询请求
     * @return 查询结果
     */
    AlipayElectronicReceiptQueryResponse queryElectronicReceipt(AlipayElectronicReceiptQueryRequest request);
}
