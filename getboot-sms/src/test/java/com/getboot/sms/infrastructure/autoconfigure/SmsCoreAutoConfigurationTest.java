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
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 短信核心自动配置测试。
 *
 * @author qiheng
 */
class SmsCoreAutoConfigurationTest {

    /**
     * 测试用上下文运行器。
     */
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(SmsAutoConfiguration.class));

    /**
     * 验证核心 Bean 与验证码场景配置会正确绑定。
     */
    @Test
    void shouldRegisterCoreBeansAndBindVerificationSceneProperties() {
        contextRunner
                .withPropertyValues(
                        "getboot.sms.enabled=true",
                        "getboot.sms.default-sign-name=GetBoot",
                        "getboot.sms.scene-sign-names.notice=NoticeSign",
                        "getboot.sms.verification-scenes.login.sign-name=VerifySign",
                        "getboot.sms.verification-scenes.login.template-code=SMS_LOGIN_001",
                        "getboot.sms.verification-scenes.login.code-param-name=verifyCode",
                        "getboot.sms.verification-scenes.login.expire-minutes-param-name=ttl",
                        "getboot.sms.verification-scenes.login.extra-params.product=getboot"
                )
                .run(context -> {
                    assertInstanceOf(DefaultSmsSignResolver.class, context.getBean(SmsSignResolver.class));
                    assertInstanceOf(FastjsonSmsTemplateParamSerializer.class,
                            context.getBean(SmsTemplateParamSerializer.class));
                    assertFalse(context.containsBean("smsProviderClient"));
                    assertFalse(context.containsBean("smsOperator"));

                    SmsProperties properties = context.getBean(SmsProperties.class);
                    assertEquals("NoticeSign", properties.getSceneSignNames().get("notice"));
                    assertEquals("VerifySign", properties.getVerificationScenes().get("login").getSignName());
                    assertEquals("SMS_LOGIN_001", properties.getVerificationScenes().get("login").getTemplateCode());
                    assertEquals("verifyCode", properties.getVerificationScenes().get("login").getCodeParamName());
                    assertEquals("ttl", properties.getVerificationScenes().get("login").getExpireMinutesParamName());
                    assertEquals("getboot",
                            properties.getVerificationScenes().get("login").getExtraParams().get("product"));
                });
    }

    /**
     * 验证用户自定义核心 Bean 存在时，默认实现会让位。
     */
    @Test
    void shouldBackOffWhenCustomCoreBeansProvided() {
        SmsSignResolver customSignResolver = (scene, requestedSignName) -> "custom-sign";
        SmsTemplateParamSerializer customSerializer = templateParams -> "custom-json";

        contextRunner
                .withPropertyValues("getboot.sms.enabled=true")
                .withBean(SmsSignResolver.class, () -> customSignResolver)
                .withBean(SmsTemplateParamSerializer.class, () -> customSerializer)
                .run(context -> {
                    assertSame(customSignResolver, context.getBean(SmsSignResolver.class));
                    assertSame(customSerializer, context.getBean(SmsTemplateParamSerializer.class));
                });
    }

    /**
     * 验证禁用短信能力时跳过核心 Bean。
     */
    @Test
    void shouldSkipCoreBeansWhenSmsIsDisabled() {
        contextRunner
                .withPropertyValues("getboot.sms.enabled=false")
                .run(context -> {
                    assertFalse(context.containsBean("smsSignResolver"));
                    assertFalse(context.containsBean("smsTemplateParamSerializer"));
                    assertFalse(context.containsBean("smsOperator"));
                });
    }
}
