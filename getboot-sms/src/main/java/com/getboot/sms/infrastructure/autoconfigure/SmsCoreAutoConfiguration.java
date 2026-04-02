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

import com.getboot.sms.api.properties.SmsProperties;
import com.getboot.sms.spi.SmsSignResolver;
import com.getboot.sms.spi.SmsTemplateParamSerializer;
import com.getboot.sms.support.DefaultSmsSignResolver;
import com.getboot.sms.support.FastjsonSmsTemplateParamSerializer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * 短信模块核心自动配置。
 *
 * @author qiheng
 */
@AutoConfiguration
@ConditionalOnProperty(prefix = "getboot.sms", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(SmsProperties.class)
public class SmsCoreAutoConfiguration {

    /**
     * 注册默认短信签名解析器。
     *
     * @param properties 短信模块配置
     * @return 短信签名解析器
     */
    @Bean
    @ConditionalOnMissingBean
    public SmsSignResolver smsSignResolver(SmsProperties properties) {
        return new DefaultSmsSignResolver(properties);
    }

    /**
     * 注册默认模板变量序列化器。
     *
     * @return 模板变量序列化器
     */
    @Bean
    @ConditionalOnMissingBean
    public SmsTemplateParamSerializer smsTemplateParamSerializer() {
        return new FastjsonSmsTemplateParamSerializer();
    }
}
