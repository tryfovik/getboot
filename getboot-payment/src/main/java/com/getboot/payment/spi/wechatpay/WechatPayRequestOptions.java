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
package com.getboot.payment.spi.wechatpay;

import lombok.Data;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 微信支付请求可变参数。
 *
 * <p>SPI 扩展方可在请求发出前覆盖应用标识、通知地址、付款人标识，以及 Map 形态请求体中的扩展 JSON 字段。</p>
 *
 * @author qiheng
 */
@Data
public class WechatPayRequestOptions {

    /**
     * 覆盖默认配置的 AppId。
     */
    private String appId;

    /**
     * 覆盖默认通知地址。
     */
    private String notifyUrl;

    /**
     * 覆盖付款人标识。
     */
    private String payerId;

    /**
     * 覆盖客户端 IP。
     */
    private String clientIp;

    /**
     * 覆盖附加数据。
     */
    private String attach;

    /**
     * 覆盖商品标记。
     */
    private String goodsTag;

    /**
     * 覆盖子商户号。
     */
    private String subMerchantId;

    /**
     * 追加到 Map 形态请求体中的扩展字段。
     */
    private final Map<String, Object> extraBody = new LinkedHashMap<>();

    /**
     * 写入一个扩展请求体字段。
     *
     * @param key 字段名
     * @param value 字段值
     */
    public void putExtraBody(String key, Object value) {
        extraBody.put(key, value);
    }

    /**
     * 批量写入扩展请求体字段。
     *
     * @param body 扩展字段
     */
    public void putAllExtraBody(Map<String, Object> body) {
        if (body != null && !body.isEmpty()) {
            extraBody.putAll(body);
        }
    }
}
