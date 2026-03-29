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
package com.getboot.payment.api.alipay.trade.facetoface;

/**
 * 支付宝当面付服务。
 *
 * @author qiheng
 */
public interface AlipayFaceToFaceService {

    /**
     * 发起条码支付。
     *
     * @param request 条码支付请求
     * @return 支付结果
     */
    AlipayFaceToFacePayResponse pay(AlipayFaceToFacePayRequest request);
}
