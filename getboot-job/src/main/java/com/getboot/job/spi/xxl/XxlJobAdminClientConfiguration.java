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
package com.getboot.job.spi.xxl;

/**
 * XXL-JOB 管理端客户端可定制配置。
 *
 * <p>用于在实例创建前调整管理端地址、认证凭证与执行器应用名。</p>
 *
 * @author qiheng
 */
public class XxlJobAdminClientConfiguration {

    /**
     * XXL-JOB 管理端地址。
     */
    private String addresses;

    /**
     * 管理端登录用户名。
     */
    private String username;

    /**
     * 管理端登录密码。
     */
    private String password;

    /**
     * 对应执行器应用名。
     */
    private String appName;

    public String getAddresses() {
        return addresses;
    }

    public void setAddresses(String addresses) {
        this.addresses = addresses;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }
}
