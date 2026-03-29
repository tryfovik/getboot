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
package com.getboot.governance.infrastructure.sentinel.autoconfigure;

import com.alibaba.csp.sentinel.annotation.aspectj.SentinelResourceAspect;
import com.getboot.governance.api.properties.GovernanceProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Sentinel 治理自动配置。
 *
 * <p>统一托管 Sentinel 相关能力的接入边界，避免业务侧直接依赖底层前缀与手工装配切面。</p>
 *
 * @author qiheng
 */
@AutoConfiguration
@ConditionalOnClass(name = "com.alibaba.cloud.sentinel.custom.SentinelAutoConfiguration")
@ConditionalOnProperty(prefix = "getboot.governance", name = {"enabled", "sentinel.enabled"}, havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(GovernanceProperties.class)
public class SentinelGovernanceAutoConfiguration {

    /**
     * 注册 Sentinel 注解切面。
     *
     * <p>Spring Cloud Alibaba 主要负责基础自动配置，注解切面由当前模块按需补齐。</p>
     *
     * @return Sentinel 注解切面
     */
    @Bean
    @ConditionalOnClass(SentinelResourceAspect.class)
    @ConditionalOnMissingBean
    public SentinelResourceAspect sentinelResourceAspect() {
        return new SentinelResourceAspect();
    }
}
