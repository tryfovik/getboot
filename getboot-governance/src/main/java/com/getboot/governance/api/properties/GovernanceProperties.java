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
package com.getboot.governance.api.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 流量治理 starter 配置。
 *
 * <p>对外保持能力层前缀，当前内部实现基于 Sentinel。</p>
 *
 * @author qiheng
 */
@Data
@ConfigurationProperties(prefix = "getboot.governance")
public class GovernanceProperties {

    /**
     * 是否全局启用治理能力。
     */
    private boolean enabled = true;

    /**
     * Sentinel 实现配置。
     */
    private final Sentinel sentinel = new Sentinel();

    @Data
    public static class Sentinel {
        /**
         * 是否启用 Sentinel 实现。
         */
        private boolean enabled = true;

        /**
         * 是否在启动早期建立心跳连接。
         */
        private boolean eager = false;

        /**
         * 是否统一 Web 上下文。
         */
        private boolean webContextUnify = true;

        /**
         * 是否按 HTTP 方法拆分资源名。
         */
        private boolean httpMethodSpecify = false;

        /**
         * 触发流控时的默认阻断页面。
         */
        private String blockPage;

        /**
         * 控制台与客户端传输层配置。
         */
        private final Transport transport = new Transport();

        /**
         * Web 拦截器配置。
         */
        private final Filter filter = new Filter();

        /**
         * OpenFeign 适配开关。
         */
        private final OpenFeign openfeign = new OpenFeign();

        /**
         * RestTemplate 适配开关。
         */
        private final RestTemplate restTemplate = new RestTemplate();

        /**
         * 管理端点适配配置。
         */
        private final Management management = new Management();
    }

    @Data
    public static class Transport {
        /**
         * Sentinel Dashboard 地址。
         */
        private String dashboard;

        /**
         * 本地 API 端口。
         */
        private Integer port = 8719;

        /**
         * 客户端上报 IP。
         */
        private String clientIp;

        /**
         * 心跳间隔，单位毫秒。
         */
        private Integer heartbeatIntervalMs;
    }

    @Data
    public static class Filter {
        /**
         * 是否启用 Web 拦截器。
         */
        private boolean enabled = true;

        /**
         * 拦截器顺序。
         */
        private int order = Integer.MIN_VALUE;
    }

    @Data
    public static class OpenFeign {
        /**
         * 是否启用 OpenFeign 降级桥接。
         */
        private boolean enabled = false;
    }

    @Data
    public static class RestTemplate {
        /**
         * 是否启用 RestTemplate 保护。
         */
        private boolean enabled = true;
    }

    @Data
    public static class Management {
        /**
         * Sentinel Actuator 端点配置。
         */
        private final Endpoint endpoint = new Endpoint();

        /**
         * Sentinel 健康检查配置。
         */
        private final Health health = new Health();
    }

    @Data
    public static class Endpoint {
        /**
         * 是否暴露 Sentinel 端点。
         */
        private boolean enabled = true;
    }

    @Data
    public static class Health {
        /**
         * 是否启用 Sentinel 健康检查。
         */
        private boolean enabled = true;
    }
}
