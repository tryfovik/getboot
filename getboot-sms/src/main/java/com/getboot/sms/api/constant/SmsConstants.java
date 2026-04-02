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
package com.getboot.sms.api.constant;

/**
 * 短信模块常量。
 *
 * @author qiheng
 */
public final class SmsConstants {

    /**
     * 工具类私有构造器。
     */
    private SmsConstants() {
    }

    /**
     * 阿里云供应商标识。
     */
    public static final String PROVIDER_ALIYUN = "aliyun";

    /**
     * 阿里云发送成功状态码。
     */
    public static final String ALIYUN_SUCCESS_CODE = "OK";

    /**
     * 阿里云默认服务地址。
     */
    public static final String ALIYUN_DEFAULT_ENDPOINT = "dysmsapi.aliyuncs.com";

    /**
     * 阿里云默认地域编码。
     */
    public static final String ALIYUN_DEFAULT_REGION_ID = "cn-hangzhou";

    /**
     * 默认验证码模板参数名。
     */
    public static final String DEFAULT_CODE_PARAM_NAME = "code";

    /**
     * 默认过期分钟模板参数名。
     */
    public static final String DEFAULT_EXPIRE_MINUTES_PARAM_NAME = "expireMinutes";
}
