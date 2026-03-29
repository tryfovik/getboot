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
package com.getboot.rpc.infrastructure.dubbo.filter;

import com.getboot.rpc.api.properties.RpcSecurityProperties;
import com.getboot.rpc.api.properties.RpcTraceProperties;
import com.getboot.rpc.infrastructure.dubbo.security.RpcAuthenticationAttachments;
import com.getboot.rpc.infrastructure.dubbo.security.RpcTraceAttachments;
import com.getboot.rpc.spi.RpcAuthenticationSigner;
import com.getboot.rpc.spi.dubbo.RpcAuthenticationAttachmentCustomizer;
import com.getboot.support.api.context.SpringContextHolder;
import com.getboot.support.api.trace.TraceContextHolder;
import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.Filter;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.Result;
import org.apache.dubbo.rpc.RpcException;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * RPC 消费端认证过滤器。
 *
 * <p>在消费者发起调用前自动补充应用标识、时间戳与签名信息。</p>
 *
 * @author qiheng
 */
@Activate(group = CommonConstants.CONSUMER, order = -10_000)
public class RpcConsumerAuthenticationFilter implements Filter {

    private RpcSecurityProperties rpcSecurityProperties;
    private RpcTraceProperties rpcTraceProperties;
    private RpcAuthenticationSigner rpcAuthenticationSigner;

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        RpcSecurityProperties resolvedProperties = resolveRpcSecurityProperties();
        if (resolvedProperties == null || shouldSkipAuthentication(invocation, resolvedProperties)) {
            return invoker.invoke(invocation);
        }

        RpcSecurityProperties.Consumer consumer = resolvedProperties.getAuthentication().getConsumer();
        if (!consumer.isConfigured()) {
            if (consumer.hasAnyConfiguredValue()) {
                throw new RpcException("RPC authentication is enabled, but consumer credentials are incomplete.");
            }
            return invoker.invoke(invocation);
        }

        long timestamp = System.currentTimeMillis() / 1000;
        RpcAuthenticationSigner signer = resolveRpcAuthenticationSigner();
        String signature = signer.sign(
                consumer.getAppId(),
                consumer.getAppSecret(),
                invocation.getServiceName(),
                invocation.getMethodName(),
                timestamp
        );
        Map<String, String> attachments = new LinkedHashMap<>();
        attachments.put(RpcAuthenticationAttachments.APP_ID, consumer.getAppId());
        attachments.put(RpcAuthenticationAttachments.TIMESTAMP, Long.toString(timestamp));
        attachments.put(RpcAuthenticationAttachments.SIGNATURE, signature);
        String traceId = TraceContextHolder.getTraceId();
        if (isTraceEnabled() && StringUtils.hasText(traceId)) {
            attachments.putIfAbsent(RpcTraceAttachments.TRACE_ID, traceId);
        }
        getAttachmentCustomizers().forEach(customizer -> customizer.customize(attachments, invocation, resolvedProperties));
        attachments.forEach(invocation::setAttachmentIfAbsent);
        return invoker.invoke(invocation);
    }

    private boolean shouldSkipAuthentication(Invocation invocation, RpcSecurityProperties resolvedProperties) {
        if (!resolvedProperties.getAuthentication().isEnabled()) {
            return true;
        }
        String serviceName = invocation.getServiceName();
        if (!StringUtils.hasText(serviceName)) {
            return true;
        }
        return resolvedProperties.getAuthentication().getExcludedServicePrefixes().stream()
                .filter(StringUtils::hasText)
                .anyMatch(serviceName::startsWith);
    }

    private RpcSecurityProperties resolveRpcSecurityProperties() {
        return rpcSecurityProperties != null ? rpcSecurityProperties : SpringContextHolder.getBeanIfAvailable(RpcSecurityProperties.class);
    }

    private RpcAuthenticationSigner resolveRpcAuthenticationSigner() {
        if (rpcAuthenticationSigner == null) {
            rpcAuthenticationSigner = SpringContextHolder.getBeanIfAvailable(RpcAuthenticationSigner.class);
        }
        if (rpcAuthenticationSigner == null) {
            throw new RpcException("RPC authentication signer is unavailable.");
        }
        return rpcAuthenticationSigner;
    }

    private boolean isTraceEnabled() {
        if (rpcTraceProperties == null) {
            rpcTraceProperties = SpringContextHolder.getBeanIfAvailable(RpcTraceProperties.class);
        }
        return rpcTraceProperties == null || rpcTraceProperties.isEnabled();
    }

    private List<RpcAuthenticationAttachmentCustomizer> getAttachmentCustomizers() {
        List<RpcAuthenticationAttachmentCustomizer> customizers = new ArrayList<>(
                SpringContextHolder.getBeansOfType(RpcAuthenticationAttachmentCustomizer.class).values()
        );
        AnnotationAwareOrderComparator.sort(customizers);
        return customizers;
    }

    public void setRpcSecurityProperties(RpcSecurityProperties rpcSecurityProperties) {
        this.rpcSecurityProperties = rpcSecurityProperties;
    }

    public void setRpcAuthenticationSigner(RpcAuthenticationSigner rpcAuthenticationSigner) {
        this.rpcAuthenticationSigner = rpcAuthenticationSigner;
    }
}
