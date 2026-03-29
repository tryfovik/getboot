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
package com.getboot.job.api.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 任务调度能力配置。
 *
 * <p>用于承接 `getboot.job.*` 下的统一配置，并在内部收敛各调度实现的配置子树。</p>
 *
 * @author qiheng
 */
@ConfigurationProperties(prefix = "getboot.job")
@Data
public class JobProperties {

    /**
     * 是否启用任务调度自动配置。
     */
    private boolean enabled;

    /**
     * XXL-JOB 实现级配置。
     */
    private Xxl xxl = new Xxl();

    /**
     * XXL-JOB 配置树。
     */
    @Data
    public static class Xxl {

        /**
         * XXL-JOB 管理端配置。
         */
        private Admin admin = new Admin();

        /**
         * XXL-JOB 通信访问令牌。
         */
        private String accessToken;

        /**
         * 执行器配置。
         */
        private Executor executor = new Executor();
    }

    /**
     * XXL-JOB 管理端连接配置。
     */
    @Data
    public static class Admin {
        /**
         * 管理端地址，多个地址可按 XXL-JOB 规范填写。
         */
        private String addresses;

        /**
         * 管理端用户名。
         */
        private String username;

        /**
         * 管理端密码。
         */
        private String password;
    }

    /**
     * XXL-JOB 执行器配置。
     */
    @Data
    public static class Executor {
        /**
         * 执行器应用名称。
         */
        private String appName;

        /**
         * 执行器注册地址。
         */
        private String address;

        /**
         * 执行器 IP。
         */
        private String ip;

        /**
         * 执行器端口。
         */
        private int port;

        /**
         * 执行器日志目录。
         */
        private String logPath;

        /**
         * 执行器日志保留天数。
         */
        private int logRetentionDays;
    }
}
