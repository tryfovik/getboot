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
package com.getboot.payment.spi.alipay;

import lombok.Data;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 支付宝请求可变参数。
 *
 * <p>SPI 扩展方可在请求发出前覆盖通知地址、回跳地址、调用上下文以及 `biz_content` 扩展字段。</p>
 *
 * @author qiheng
 */
@Data
public class AlipayRequestOptions {

    /**
     * 异步通知地址。
     */
    private String notifyUrl;

    /**
     * 同步回跳地址。
     */
    private String returnUrl;

    /**
     * WAP 退出地址。
     */
    private String quitUrl;

    /**
     * ISV 代调用 token。
     */
    private String appAuthToken;

    /**
     * 用户授权 token。
     */
    private String authToken;

    /**
     * 调试路由地址。
     */
    private String route;

    /**
     * 扩展业务参数。
     */
    private final Map<String, Object> optionalArgs = new LinkedHashMap<>();

    /**
     * 写入一个扩展业务参数。
     *
     * @param key 参数名
     * @param value 参数值
     */
    public void putOptionalArg(String key, Object value) {
        optionalArgs.put(key, value);
    }

    /**
     * 批量写入扩展业务参数。
     *
     * @param args 参数表
     */
    public void putAllOptionalArgs(Map<String, Object> args) {
        if (args != null && !args.isEmpty()) {
            optionalArgs.putAll(args);
        }
    }
}
