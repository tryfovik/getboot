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
package com.getboot.auth.infrastructure.satoken.autoconfigure;

import cn.dev33.satoken.stp.StpUtil;
import com.getboot.auth.api.accessor.CurrentUserAccessor;
import com.getboot.auth.infrastructure.satoken.accessor.SaTokenCurrentUserAccessor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * Sa-Token 认证自动配置。
 *
 * <p>补充认证能力层统一的当前用户访问入口。</p>
 *
 * @author qiheng
 */
@AutoConfiguration
@ConditionalOnClass(StpUtil.class)
public class SaTokenAuthAutoConfiguration {

    /**
     * 注册当前用户访问器。
     *
     * @return 当前用户访问器
     */
    @Bean
    @ConditionalOnMissingBean
    public CurrentUserAccessor currentUserAccessor() {
        return new SaTokenCurrentUserAccessor();
    }
}
