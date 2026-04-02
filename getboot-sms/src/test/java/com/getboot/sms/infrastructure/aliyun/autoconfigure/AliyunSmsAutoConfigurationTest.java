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
import com.getboot.sms.api.operator.SmsOperator;
import com.getboot.sms.infrastructure.autoconfigure.SmsAutoConfiguration;
import com.getboot.sms.infrastructure.aliyun.support.AliyunSmsProviderClient;
import com.getboot.sms.spi.SmsProviderClient;
import com.getboot.sms.spi.SmsSignResolver;
import com.getboot.sms.spi.SmsTemplateParamSerializer;
import com.getboot.sms.support.DefaultSmsOperator;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 阿里云短信自动配置测试。
 *
 * @author qiheng
 */
class AliyunSmsAutoConfigurationTest {

    /**
     * 应用上下文运行器。
     */
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(SmsAutoConfiguration.class));

    /**
     * 验证配置完整时注册短信相关 Bean。
     */
    @Test
    void shouldRegisterAliyunBeansWhenConfigured() {
        contextRunner
                .withPropertyValues(
                        "getboot.sms.enabled=true",
                        "getboot.sms.type=aliyun",
                        "getboot.sms.default-sign-name=GetBoot",
                        "getboot.sms.aliyun.enabled=true",
                        "getboot.sms.aliyun.access-key-id=test-access-key",
                        "getboot.sms.aliyun.access-key-secret=test-secret"
                )
                .run(context -> {
                    assertTrue(context.containsBean("smsSignResolver"));
                    assertTrue(context.containsBean("smsTemplateParamSerializer"));
                    assertTrue(context.containsBean("aliyunSmsClient"));
                    assertTrue(context.containsBean("smsProviderClient"));
                    assertTrue(context.containsBean("smsOperator"));

                    assertNotNull(context.getBean(SmsSignResolver.class));
                    assertNotNull(context.getBean(SmsTemplateParamSerializer.class));
                    assertTrue(context.getBean(Client.class) instanceof Client);
                    assertTrue(context.getBean(SmsProviderClient.class) instanceof AliyunSmsProviderClient);
                    assertTrue(context.getBean(SmsOperator.class) instanceof DefaultSmsOperator);
                });
    }

    /**
     * 验证禁用短信能力时不注册任何相关 Bean。
     */
    @Test
    void shouldSkipAllSmsBeansWhenDisabled() {
        contextRunner
                .withPropertyValues(
                        "getboot.sms.enabled=false",
                        "getboot.sms.aliyun.access-key-id=test-access-key",
                        "getboot.sms.aliyun.access-key-secret=test-secret"
                )
                .run(context -> {
                    assertFalse(context.containsBean("smsSignResolver"));
                    assertFalse(context.containsBean("smsTemplateParamSerializer"));
                    assertFalse(context.containsBean("aliyunSmsClient"));
                    assertFalse(context.containsBean("smsProviderClient"));
                    assertFalse(context.containsBean("smsOperator"));
                });
    }

    /**
     * 验证缺少凭证时跳过阿里云客户端与短信门面。
     */
    @Test
    void shouldSkipAliyunClientAndOperatorWhenCredentialsAreMissing() {
        contextRunner
                .withPropertyValues(
                        "getboot.sms.enabled=true",
                        "getboot.sms.type=aliyun",
                        "getboot.sms.default-sign-name=GetBoot"
                )
                .run(context -> {
                    assertTrue(context.containsBean("smsSignResolver"));
                    assertTrue(context.containsBean("smsTemplateParamSerializer"));
                    assertFalse(context.containsBean("aliyunSmsClient"));
                    assertFalse(context.containsBean("smsProviderClient"));
                    assertFalse(context.containsBean("smsOperator"));
                });
    }
}
