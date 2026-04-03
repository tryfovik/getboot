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
package com.getboot.mail.api.properties;

import com.getboot.mail.api.constant.MailConstants;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 邮件模块配置。
 *
 * @author qiheng
 */
@ConfigurationProperties(prefix = "getboot.mail")
@Data
public class MailProperties {

    /**
     * 是否启用邮件能力。
     */
    private boolean enabled = true;

    /**
     * 当前邮件实现类型。
     */
    private String type = MailConstants.MAIL_TYPE_SMTP;

    /**
     * 默认发件人。
     */
    private String defaultFrom;

    /**
     * 默认内容类型。
     */
    private String defaultContentType = MailConstants.DEFAULT_CONTENT_TYPE;

    /**
     * SMTP 配置。
     */
    private Smtp smtp = new Smtp();

    /**
     * SMTP 配置。
     *
     * @author qiheng
     */
    @Data
    public static class Smtp {

        /**
         * 是否启用 SMTP 实现。
         */
        private boolean enabled = true;

        /**
         * SMTP 主机。
         */
        private String host;

        /**
         * SMTP 端口。
         */
        private Integer port = 25;

        /**
         * 登录用户名。
         */
        private String username;

        /**
         * 登录密码。
         */
        private String password;

        /**
         * SMTP 协议。
         */
        private String protocol = MailConstants.DEFAULT_PROTOCOL;

        /**
         * 是否启用认证。
         */
        private boolean auth = true;

        /**
         * 是否启用 STARTTLS。
         */
        private boolean starttlsEnabled = false;

        /**
         * 是否启用 SSL。
         */
        private boolean sslEnabled = false;

        /**
         * 连接超时时间，单位毫秒。
         */
        private Integer connectionTimeout = 5000;

        /**
         * 读取超时时间，单位毫秒。
         */
        private Integer timeout = 5000;

        /**
         * 写入超时时间，单位毫秒。
         */
        private Integer writeTimeout = 5000;

        /**
         * 额外 JavaMail 属性。
         */
        private Map<String, String> properties = new LinkedHashMap<>();

        /**
         * 设置额外 JavaMail 属性。
         *
         * @param properties 额外 JavaMail 属性
         */
        public void setProperties(Map<String, String> properties) {
            this.properties = properties == null ? new LinkedHashMap<>() : new LinkedHashMap<>(properties);
        }
    }
}
