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
package com.getboot.coordination.infrastructure.zookeeper.curator.autoconfigure;

import com.getboot.coordination.infrastructure.zookeeper.curator.properties.CuratorZookeeperProperties;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.util.StringUtils;

/**
 * Curator ZooKeeper auto configuration.
 *
 * @author qiheng
 */
@AutoConfiguration
@ConditionalOnClass(CuratorFramework.class)
@ConditionalOnProperty(prefix = "getboot.coordination.zookeeper", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(CuratorZookeeperProperties.class)
public class CuratorZookeeperAutoConfiguration {

    @Bean(destroyMethod = "close", initMethod = "start")
    @ConditionalOnMissingBean
    public CuratorFramework curatorFramework(CuratorZookeeperProperties properties) {
        if (!StringUtils.hasText(properties.getConnectString())) {
            throw new IllegalStateException(
                    "getboot.coordination.zookeeper.connect-string must not be empty when ZooKeeper is enabled."
            );
        }

        CuratorFrameworkFactory.Builder builder = CuratorFrameworkFactory.builder()
                .connectString(properties.getConnectString())
                .sessionTimeoutMs(properties.getSessionTimeoutMs())
                .connectionTimeoutMs(properties.getConnectionTimeoutMs())
                .retryPolicy(createRetryPolicy(properties));

        if (StringUtils.hasText(properties.getNamespace())) {
            builder.namespace(properties.getNamespace());
        }

        return builder.build();
    }

    private RetryPolicy createRetryPolicy(CuratorZookeeperProperties properties) {
        CuratorZookeeperProperties.Retry retry = properties.getRetry();
        return new ExponentialBackoffRetry(
                retry.getBaseSleepTimeMs(),
                retry.getMaxRetries(),
                retry.getMaxSleepMs()
        );
    }
}
