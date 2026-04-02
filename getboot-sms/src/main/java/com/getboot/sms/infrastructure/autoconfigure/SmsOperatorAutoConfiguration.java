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
package com.getboot.sms.infrastructure.autoconfigure;

import com.getboot.sms.api.operator.SmsOperator;
import com.getboot.sms.api.properties.SmsProperties;
import com.getboot.sms.spi.SmsProviderClient;
import com.getboot.sms.spi.SmsSignResolver;
import com.getboot.sms.support.DefaultSmsOperator;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

/**
 * 短信门面自动配置。
 *
 * @author qiheng
 */
@AutoConfiguration
@ConditionalOnProperty(prefix = "getboot.sms", name = "enabled", havingValue = "true", matchIfMissing = true)
public class SmsOperatorAutoConfiguration {

    /**
     * 注册短信发送门面。
     *
     * @param smsProviderClient 短信供应商客户端
     * @param smsSignResolver 短信签名解析器
     * @param properties 短信模块配置
     * @return 短信发送门面
     */
    @Bean
    @ConditionalOnBean(SmsProviderClient.class)
    @ConditionalOnMissingBean(SmsOperator.class)
    public SmsOperator smsOperator(SmsProviderClient smsProviderClient,
                                   SmsSignResolver smsSignResolver,
                                   SmsProperties properties) {
        return new DefaultSmsOperator(smsProviderClient, smsSignResolver, properties);
    }
}
