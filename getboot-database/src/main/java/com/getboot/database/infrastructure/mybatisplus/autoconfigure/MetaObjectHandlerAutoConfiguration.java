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
package com.getboot.database.infrastructure.mybatisplus.autoconfigure;

import com.getboot.database.infrastructure.mybatisplus.handler.AuditFieldMetaObjectHandler;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

/**
 * MyBatis-Plus 审计字段自动填充配置。
 *
 * <p>用于注册默认的时间审计字段填充处理器。</p>
 *
 * @author qiheng
 */
@AutoConfiguration
@ConditionalOnProperty(prefix = "getboot.database", name = "enabled", havingValue = "true")
public class MetaObjectHandlerAutoConfiguration {

    /**
     * 注册默认审计字段处理器。
     *
     * @return 审计字段处理器
     */
    @Bean
    @ConditionalOnMissingBean
    public AuditFieldMetaObjectHandler metaObjectHandler() {
        return new AuditFieldMetaObjectHandler();
    }
}
