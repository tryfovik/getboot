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
package com.getboot.payment.api.alipay;

/**
 * 支付宝开放接口兜底访问入口。
 *
 * <p>用于承接暂未沉淀为稳定强类型接口的 OpenAPI 调用。</p>
 *
 * @author qiheng
 */
public interface AlipayApiService {

    /**
     * 发起支付宝开放接口调用。
     *
     * @param request 通用调用请求
     * @return 调用结果
     */
    AlipayApiResponse execute(AlipayApiRequest request);
}
