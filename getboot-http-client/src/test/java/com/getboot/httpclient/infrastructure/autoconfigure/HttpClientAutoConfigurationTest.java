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
package com.getboot.httpclient.infrastructure.autoconfigure;

import com.getboot.httpclient.infrastructure.feign.autoconfigure.OpenFeignHttpClientAutoConfiguration;
import com.getboot.httpclient.infrastructure.feign.support.TraceFeignRequestInterceptor;
import com.getboot.httpclient.infrastructure.headers.autoconfigure.OutboundHttpHeadersAutoConfiguration;
import com.getboot.httpclient.infrastructure.resttemplate.autoconfigure.RestTemplateHttpClientAutoConfiguration;
import com.getboot.httpclient.infrastructure.resttemplate.support.TraceRestTemplateInterceptor;
import com.getboot.httpclient.infrastructure.webclient.autoconfigure.WebClientHttpClientAutoConfiguration;
import com.getboot.httpclient.infrastructure.webclient.support.TraceWebClientFilterFunction;
import com.getboot.httpclient.support.headers.OutboundHttpHeadersResolver;
import feign.RequestInterceptor;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.web.client.RestTemplateCustomizer;
import org.springframework.boot.web.reactive.function.client.WebClientCustomizer;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * HTTP 客户端自动配置测试。
 *
 * @author qiheng
 */
class HttpClientAutoConfigurationTest {

    /**
     * 测试用上下文运行器。
     */
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    OutboundHttpHeadersAutoConfiguration.class,
                    OpenFeignHttpClientAutoConfiguration.class,
                    WebClientHttpClientAutoConfiguration.class,
                    RestTemplateHttpClientAutoConfiguration.class
            ));

    /**
     * 验证默认情况下会同时注册通用请求头解析器以及三类客户端增强 Bean。
     */
    @Test
    void shouldRegisterTraceInfrastructureForAllHttpClientsByDefault() {
        contextRunner.run(context -> {
            assertTrue(context.containsBean("getbootOutboundHttpHeadersResolver"));
            assertTrue(context.containsBean("getbootTraceFeignRequestInterceptor"));
            assertTrue(context.containsBean("getbootTraceWebClientFilterFunction"));
            assertTrue(context.containsBean("getbootTraceWebClientCustomizer"));
            assertTrue(context.containsBean("getbootTraceRestTemplateInterceptor"));
            assertTrue(context.containsBean("getbootTraceRestTemplateCustomizer"));

            assertInstanceOf(
                    OutboundHttpHeadersResolver.class,
                    context.getBean("getbootOutboundHttpHeadersResolver")
            );
            assertInstanceOf(
                    TraceFeignRequestInterceptor.class,
                    context.getBean("getbootTraceFeignRequestInterceptor")
            );
            assertInstanceOf(
                    TraceWebClientFilterFunction.class,
                    context.getBean("getbootTraceWebClientFilterFunction")
            );
            assertInstanceOf(
                    TraceRestTemplateInterceptor.class,
                    context.getBean("getbootTraceRestTemplateInterceptor")
            );

            ExchangeFilterFunction traceFilterFunction =
                    context.getBean("getbootTraceWebClientFilterFunction", ExchangeFilterFunction.class);
            WebClientCustomizer webClientCustomizer =
                    context.getBean("getbootTraceWebClientCustomizer", WebClientCustomizer.class);
            List<ExchangeFilterFunction> filters = new ArrayList<>();
            WebClient.Builder builder = WebClient.builder();
            webClientCustomizer.customize(builder);
            builder.filters(filters::addAll);
            assertTrue(filters.contains(traceFilterFunction));

            ClientHttpRequestInterceptor restTemplateInterceptor =
                    context.getBean("getbootTraceRestTemplateInterceptor", ClientHttpRequestInterceptor.class);
            RestTemplateCustomizer restTemplateCustomizer =
                    context.getBean("getbootTraceRestTemplateCustomizer", RestTemplateCustomizer.class);
            RestTemplate restTemplate = new RestTemplate();
            restTemplateCustomizer.customize(restTemplate);
            assertTrue(restTemplate.getInterceptors().contains(restTemplateInterceptor));
        });
    }

    /**
     * 验证关闭单个客户端增强时，不会影响其他客户端的默认装配。
     */
    @Test
    void shouldSkipWebClientBeansWhenWebClientTraceDisabled() {
        contextRunner
                .withPropertyValues("getboot.http-client.webclient.trace.enabled=false")
                .run(context -> {
                    assertTrue(context.containsBean("getbootOutboundHttpHeadersResolver"));
                    assertTrue(context.containsBean("getbootTraceFeignRequestInterceptor"));
                    assertTrue(context.containsBean("getbootTraceRestTemplateInterceptor"));
                    assertTrue(context.containsBean("getbootTraceRestTemplateCustomizer"));
                    assertFalse(context.containsBean("getbootTraceWebClientFilterFunction"));
                    assertFalse(context.containsBean("getbootTraceWebClientCustomizer"));
                });
    }

    /**
     * 验证业务方提供同名 Bean 时，自动配置会整体回退。
     */
    @Test
    void shouldBackOffWhenCustomNamedBeansProvided() {
        RequestInterceptor customFeignInterceptor = requestTemplate -> requestTemplate.header("X-Test-Feign", "custom");
        ExchangeFilterFunction customWebClientFilter = (request, next) -> next.exchange(request);
        WebClientCustomizer customWebClientCustomizer = webClientBuilder -> {
        };
        ClientHttpRequestInterceptor customRestTemplateInterceptor =
                (request, body, execution) -> execution.execute(request, body);
        RestTemplateCustomizer customRestTemplateCustomizer = restTemplate -> {
        };

        contextRunner
                .withBean(
                        "getbootTraceFeignRequestInterceptor",
                        RequestInterceptor.class,
                        () -> customFeignInterceptor
                )
                .withBean(
                        "getbootTraceWebClientFilterFunction",
                        ExchangeFilterFunction.class,
                        () -> customWebClientFilter
                )
                .withBean(
                        "getbootTraceWebClientCustomizer",
                        WebClientCustomizer.class,
                        () -> customWebClientCustomizer
                )
                .withBean(
                        "getbootTraceRestTemplateInterceptor",
                        ClientHttpRequestInterceptor.class,
                        () -> customRestTemplateInterceptor
                )
                .withBean(
                        "getbootTraceRestTemplateCustomizer",
                        RestTemplateCustomizer.class,
                        () -> customRestTemplateCustomizer
                )
                .run(context -> {
                    assertSame(
                            customFeignInterceptor,
                            context.getBean("getbootTraceFeignRequestInterceptor", RequestInterceptor.class)
                    );
                    assertSame(
                            customWebClientFilter,
                            context.getBean("getbootTraceWebClientFilterFunction", ExchangeFilterFunction.class)
                    );
                    assertSame(
                            customWebClientCustomizer,
                            context.getBean("getbootTraceWebClientCustomizer", WebClientCustomizer.class)
                    );
                    assertSame(
                            customRestTemplateInterceptor,
                            context.getBean("getbootTraceRestTemplateInterceptor", ClientHttpRequestInterceptor.class)
                    );
                    assertSame(
                            customRestTemplateCustomizer,
                            context.getBean("getbootTraceRestTemplateCustomizer", RestTemplateCustomizer.class)
                    );
                });
    }
}
