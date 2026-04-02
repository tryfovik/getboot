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
package com.getboot.sms.infrastructure.aliyun.autoconfigure;

import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.teaopenapi.models.Config;
import com.getboot.sms.api.properties.SmsProperties;
import com.getboot.sms.infrastructure.aliyun.support.AliyunSmsProviderClient;
import com.getboot.sms.spi.SmsProviderClient;
import com.getboot.sms.spi.SmsTemplateParamSerializer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.util.StringUtils;

/**
 * 阿里云短信自动配置。
 *
 * @author qiheng
 */
@AutoConfiguration
@ConditionalOnClass(Client.class)
@ConditionalOnProperty(prefix = "getboot.sms", name = "enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnExpression("'${getboot.sms.type:aliyun}' == 'aliyun' and '${getboot.sms.aliyun.enabled:true}' == 'true'")
public class AliyunSmsAutoConfiguration {

    /**
     * 注册阿里云短信客户端。
     *
     * @param properties 短信模块配置
     * @return 阿里云短信客户端
     * @throws Exception 创建客户端时的异常
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "getboot.sms.aliyun", name = {"access-key-id", "access-key-secret"})
    public Client aliyunSmsClient(SmsProperties properties) throws Exception {
        SmsProperties.Aliyun aliyun = properties.getAliyun();
        Config config = new Config()
                .setAccessKeyId(aliyun.getAccessKeyId())
                .setAccessKeySecret(aliyun.getAccessKeySecret())
                .setEndpoint(aliyun.getEndpoint())
                .setRegionId(aliyun.getRegionId());
        if (aliyun.getConnectTimeout() != null) {
            config.setConnectTimeout(aliyun.getConnectTimeout());
        }
        if (aliyun.getReadTimeout() != null) {
            config.setReadTimeout(aliyun.getReadTimeout());
        }
        if (StringUtils.hasText(aliyun.getEndpoint())) {
            config.setEndpoint(aliyun.getEndpoint().trim());
        }
        if (StringUtils.hasText(aliyun.getRegionId())) {
            config.setRegionId(aliyun.getRegionId().trim());
        }
        return new Client(config);
    }

    /**
     * 注册默认短信供应商客户端。
     *
     * @param aliyunSmsClient 阿里云短信客户端
     * @param smsTemplateParamSerializer 模板变量序列化器
     * @return 短信供应商客户端
     */
    @Bean
    @ConditionalOnBean(Client.class)
    @ConditionalOnMissingBean(SmsProviderClient.class)
    public SmsProviderClient smsProviderClient(Client aliyunSmsClient,
                                               SmsTemplateParamSerializer smsTemplateParamSerializer) {
        return new AliyunSmsProviderClient(aliyunSmsClient, smsTemplateParamSerializer);
    }
}
