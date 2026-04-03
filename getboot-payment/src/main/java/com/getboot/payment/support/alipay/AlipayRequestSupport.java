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

import com.getboot.payment.spi.alipay.AlipayRequestOptions;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 支付宝请求辅助工具。
 *
 * @author qiheng
 */
public final class AlipayRequestSupport {

    /**
     * ISV 代调用 token 元数据键。
     */
    public static final String APP_AUTH_TOKEN = "appAuthToken";

    /**
     * 用户授权 token 元数据键。
     */
    public static final String AUTH_TOKEN = "authToken";

    /**
     * 调试路由元数据键。
     */
    public static final String ROUTE = "route";

    /**
     * WAP 退出地址元数据键。
     */
    public static final String QUIT_URL = "quitUrl";

    /**
     * 超时表达式元数据键。
     */
    public static final String TIMEOUT_EXPRESS = "timeoutExpress";

    /**
     * 透传参数元数据键。
     */
    public static final String PASSBACK_PARAMS = "passbackParams";

    /**
     * 卖家账号 ID 元数据键。
     */
    public static final String SELLER_ID = "sellerId";

    /**
     * 门店 ID 元数据键。
     */
    public static final String STORE_ID = "storeId";

    /**
     * 操作员 ID 元数据键。
     */
    public static final String OPERATOR_ID = "operatorId";

    /**
     * 终端 ID 元数据键。
     */
    public static final String TERMINAL_ID = "terminalId";

    /**
     * 禁用渠道元数据键。
     */
    public static final String DISABLE_PAY_CHANNELS = "disablePayChannels";

    /**
     * 启用渠道元数据键。
     */
    public static final String ENABLE_PAY_CHANNELS = "enablePayChannels";

    /**
     * 服务商 PID 元数据键。
     */
    public static final String SERVICE_PROVIDER_ID = "serviceProviderId";

    /**
     * 工具类不允许实例化。
     */
    private AlipayRequestSupport() {
    }

    /**
     * 提取调用上下文。
     *
     * @param metadata 元数据
     * @return 调用上下文
     */
    public static AlipayRequestContext resolveContext(Map<String, String> metadata) {
        return new AlipayRequestContext(
                text(metadata, APP_AUTH_TOKEN),
                text(metadata, AUTH_TOKEN),
                text(metadata, ROUTE)
        );
    }

    /**
     * 从 SPI 请求选项提取调用上下文。
     *
     * @param options SPI 请求选项
     * @return 调用上下文
     */
    public static AlipayRequestContext resolveContext(AlipayRequestOptions options) {
        if (options == null) {
            return new AlipayRequestContext(null, null, null);
        }
        return new AlipayRequestContext(
                options.getAppAuthToken(),
                options.getAuthToken(),
                options.getRoute()
        );
    }

    /**
     * 安全读取元数据文本值。
     *
     * @param metadata 元数据
     * @param key      键名
     * @return 文本值
     */
    public static String text(Map<String, String> metadata, String key) {
        if (metadata == null || !StringUtils.hasText(key)) {
            return null;
        }
        String value = metadata.get(key);
        return StringUtils.hasText(value) ? value : null;
    }

    /**
     * 创建新的可写参数表。
     *
     * @return 可写参数表
     */
    public static Map<String, Object> newOptionalArgs() {
        return new LinkedHashMap<>();
    }

    /**
     * 在文本值非空时写入可选参数。
     *
     * @param target 目标参数表
     * @param key    参数名
     * @param value  参数值
     */
    public static void putIfText(Map<String, Object> target, String key, String value) {
        if (target != null && StringUtils.hasText(key) && StringUtils.hasText(value)) {
            target.put(key, value);
        }
    }
}
