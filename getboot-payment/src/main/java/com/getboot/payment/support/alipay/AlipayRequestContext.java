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
package com.getboot.payment.support.alipay;

/**
 * 支付宝单次调用上下文。
 *
 * @param appAuthToken ISV 代调用 token
 * @param authToken    用户授权 token
 * @param route        后端调试路由
 * @author qiheng
 */
public record AlipayRequestContext(
        String appAuthToken,
        String authToken,
        String route) {
}
