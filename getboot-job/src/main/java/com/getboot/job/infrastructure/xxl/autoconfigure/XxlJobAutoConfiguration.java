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
package com.getboot.job.infrastructure.xxl.autoconfigure;

import com.getboot.job.api.properties.JobProperties;
import com.getboot.job.infrastructure.xxl.client.XxlJobAdminClient;
import com.getboot.job.spi.xxl.XxlJobAdminClientConfiguration;
import com.getboot.job.spi.xxl.XxlJobAdminClientConfigurer;
import com.getboot.job.spi.xxl.XxlJobExecutorCustomizer;
import com.xxl.job.core.executor.impl.XxlJobSpringExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * XXL-JOB 自动配置。
 *
 * <p>负责注册 XXL-JOB 执行器与管理端客户端，并向业务方暴露可定制入口。</p>
 *
 * @author qiheng
 */
@AutoConfiguration
@ConditionalOnClass(XxlJobSpringExecutor.class)
@ConditionalOnProperty(prefix = "getboot.job", value = "enabled", havingValue = "true")
@EnableConfigurationProperties(JobProperties.class)
@Slf4j
public class XxlJobAutoConfiguration {

    /**
     * 任务调度配置。
     */
    private final JobProperties jobProperties;

    /**
     * 创建 XXL-JOB 自动配置。
     *
     * @param jobProperties 任务调度配置
     */
    public XxlJobAutoConfiguration(JobProperties jobProperties) {
        this.jobProperties = jobProperties;
    }

    /**
     * 注册 XXL-JOB 执行器。
     *
     * @param executorCustomizers 执行器定制器集合
     * @return XXL-JOB Spring 执行器
     */
    @Bean
    @ConditionalOnMissingBean
    public XxlJobSpringExecutor xxlJobExecutor(ObjectProvider<XxlJobExecutorCustomizer> executorCustomizers) {
        log.info(">>>>>>>>>>> xxl-job config init.");
        JobProperties.Xxl xxl = jobProperties.getXxl();
        XxlJobSpringExecutor xxlJobSpringExecutor = new XxlJobSpringExecutor();
        xxlJobSpringExecutor.setAdminAddresses(xxl.getAdmin().getAddresses());
        xxlJobSpringExecutor.setAppname(xxl.getExecutor().getAppName());
        xxlJobSpringExecutor.setAddress(xxl.getExecutor().getAddress());
        xxlJobSpringExecutor.setIp(xxl.getExecutor().getIp());
        xxlJobSpringExecutor.setPort(xxl.getExecutor().getPort());
        xxlJobSpringExecutor.setAccessToken(xxl.getAccessToken());
        xxlJobSpringExecutor.setLogPath(xxl.getExecutor().getLogPath());
        xxlJobSpringExecutor.setLogRetentionDays(xxl.getExecutor().getLogRetentionDays());
        executorCustomizers.orderedStream().forEach(customizer -> customizer.customize(xxlJobSpringExecutor));
        return xxlJobSpringExecutor;
    }

    /**
     * 注册 XXL-JOB 管理端客户端。
     *
     * @param adminClientConfigurers 管理端客户端配置定制器集合
     * @return XXL-JOB 管理端客户端
     */
    @Bean
    @ConditionalOnMissingBean
    public XxlJobAdminClient xxlJobAdminClient(ObjectProvider<XxlJobAdminClientConfigurer> adminClientConfigurers) {
        JobProperties.Xxl xxl = jobProperties.getXxl();
        XxlJobAdminClientConfiguration configuration = new XxlJobAdminClientConfiguration();
        configuration.setAddresses(xxl.getAdmin().getAddresses());
        configuration.setUsername(xxl.getAdmin().getUsername());
        configuration.setPassword(xxl.getAdmin().getPassword());
        configuration.setAppName(xxl.getExecutor().getAppName());
        adminClientConfigurers.orderedStream().forEach(configurer -> configurer.configure(configuration));

        XxlJobAdminClient xxlJobAdminClient = new XxlJobAdminClient();
        xxlJobAdminClient.initConfig(
                configuration.getAddresses(),
                configuration.getUsername(),
                configuration.getPassword(),
                configuration.getAppName()
        );
        return xxlJobAdminClient;
    }
}
