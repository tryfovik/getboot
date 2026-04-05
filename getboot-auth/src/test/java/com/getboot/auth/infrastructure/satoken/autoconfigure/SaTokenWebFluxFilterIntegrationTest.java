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

import cn.dev33.satoken.dao.SaTokenDao;
import cn.dev33.satoken.dao.SaTokenDaoDefaultImpl;
import cn.dev33.satoken.stp.StpUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Sa-Token WebFlux 认证过滤集成测试。
 *
 * @author qiheng
 */
@SpringBootTest(
        classes = SaTokenWebFluxFilterIntegrationTest.TestApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "getboot.auth.satoken.token-name=Authorization",
                "getboot.auth.satoken.is-log=false",
                "getboot.auth.satoken.webflux.filter.enabled=true",
                "getboot.auth.satoken.webflux.filter.include-paths[0]=/secure/**",
                "spring.main.web-application-type=reactive"
        }
)
class SaTokenWebFluxFilterIntegrationTest {

    /**
     * 测试客户端。
     */
    @Autowired
    private WebTestClient webTestClient;

    /**
     * 验证未登录访问受保护接口时会返回 401。
     */
    @Test
    void shouldRejectProtectedPathWhenNotLoggedIn() {
        webTestClient.get()
                .uri("/secure/ping")
                .exchange()
                .expectStatus().isUnauthorized()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.status").isEqualTo("fail")
                .jsonPath("$.code").isEqualTo(401)
                .jsonPath("$.message").isEqualTo("Unauthorized");
    }

    /**
     * 验证完成登录后可访问受保护接口。
     */
    @Test
    @SuppressWarnings("unchecked")
    void shouldAllowProtectedPathAfterLogin() {
        Map<String, String> loginBody = (Map<String, String>) webTestClient.get()
                .uri("/login")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Map.class)
                .returnResult()
                .getResponseBody();
        assertThat(loginBody).isNotNull();
        String token = loginBody.get("token");
        assertThat(token).isNotBlank();

        webTestClient.get()
                .uri("/secure/ping")
                .header("Authorization", token)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("ok")
                .jsonPath("$.loginId").isEqualTo("1001");
    }

    /**
     * 验证 OPTIONS 预检请求不会被认证过滤器提前拒绝。
     */
    @Test
    void shouldSkipOptionsPreflightRequest() {
        webTestClient.options()
                .uri("/secure/ping")
                .exchange()
                .expectStatus().isOk();
    }

    /**
     * 测试用应用。
     */
    @SpringBootConfiguration
    @EnableAutoConfiguration
    @RestController
    static class TestApplication {

        /**
         * 使用内存版 Sa-Token Dao，避免测试依赖外部 Redis。
         *
         * @return 内存版 Dao
         */
        @Bean
        @Primary
        public SaTokenDao saTokenDao() {
            return new SaTokenDaoDefaultImpl();
        }

        /**
         * 执行测试登录并返回 token。
         *
         * @return 登录结果
         */
        @GetMapping("/login")
        public Map<String, String> login() {
            StpUtil.login(1001L);
            return Map.of("token", StpUtil.getTokenValue());
        }

        /**
         * 受保护的测试接口。
         *
         * @return 返回结果
         */
        @RequestMapping(path = "/secure/ping", method = RequestMethod.GET)
        public Map<String, String> securePing() {
            return Map.of("status", "ok", "loginId", String.valueOf(StpUtil.getLoginId()));
        }

        /**
         * 为预检请求提供独立响应，验证过滤器跳过后不会再触发登录校验。
         */
        @RequestMapping(path = "/secure/ping", method = RequestMethod.OPTIONS)
        @ResponseStatus(HttpStatus.OK)
        public void securePingOptions() {
        }
    }
}
