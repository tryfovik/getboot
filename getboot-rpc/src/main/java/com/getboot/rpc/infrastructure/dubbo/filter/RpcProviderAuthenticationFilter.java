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

import com.getboot.exception.api.code.CommonErrorCode;
import com.getboot.exception.api.exception.BusinessException;
import com.getboot.rpc.api.properties.RpcSecurityProperties;
import com.getboot.rpc.api.properties.RpcTraceProperties;
import com.getboot.rpc.api.resolver.RpcCallerSecretResolver;
import com.getboot.rpc.infrastructure.dubbo.security.RpcAuthenticationAttachments;
import com.getboot.rpc.infrastructure.dubbo.security.RpcTraceAttachments;
import com.getboot.rpc.spi.RpcAuthenticationSigner;
import com.getboot.rpc.spi.dubbo.RpcAuthenticationValidationContext;
import com.getboot.rpc.spi.dubbo.RpcAuthenticationValidationHook;
import com.getboot.support.api.context.SpringContextHolder;
import com.getboot.support.api.trace.TraceContextHolder;
import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.Filter;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.Result;
import org.slf4j.MDC;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * RPC 服务端认证过滤器。
 *
 * <p>用于校验调用方携带的应用标识、时间戳与签名，确保请求来源可信。</p>
 *
 * @author qiheng
 */
@Activate(group = CommonConstants.PROVIDER, order = -10_000)
public class RpcProviderAuthenticationFilter implements Filter {

    private static final String DEFAULT_TRACE_MDC_KEY = "traceId";

    private RpcSecurityProperties rpcSecurityProperties;

    private RpcCallerSecretResolver rpcCallerSecretResolver;
    private RpcAuthenticationSigner rpcAuthenticationSigner;
    private RpcTraceProperties rpcTraceProperties;

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) {
        String incomingTraceId = trimToNull(invocation.getAttachment(RpcTraceAttachments.TRACE_ID));
        String previousTraceId = null;
        String traceMdcKey = resolveTraceMdcKey();
        String previousMdcTraceId = null;
        boolean traceBound = false;
        if (isTraceEnabled() && incomingTraceId != null) {
            previousTraceId = TraceContextHolder.bindTraceId(incomingTraceId);
            previousMdcTraceId = MDC.get(traceMdcKey);
            MDC.put(traceMdcKey, incomingTraceId);
            traceBound = true;
        }

        try {
            return doInvoke(invoker, invocation);
        } finally {
            if (traceBound) {
                restoreTraceContext(previousTraceId, traceMdcKey, previousMdcTraceId);
            }
        }
    }

    private Result doInvoke(Invoker<?> invoker, Invocation invocation) {
        RpcSecurityProperties resolvedProperties = resolveRpcSecurityProperties();
        if (resolvedProperties == null || shouldSkipAuthentication(invocation, resolvedProperties)) {
            return invoker.invoke(invocation);
        }

        String callerAppId = trimToNull(invocation.getAttachment(RpcAuthenticationAttachments.APP_ID));
        String timestampValue = trimToNull(invocation.getAttachment(RpcAuthenticationAttachments.TIMESTAMP));
        String signature = trimToNull(invocation.getAttachment(RpcAuthenticationAttachments.SIGNATURE));

        boolean hasAuthenticationMetadata = callerAppId != null || timestampValue != null || signature != null;
        if (!hasAuthenticationMetadata && !resolvedProperties.getAuthentication().getProvider().isRequired()) {
            return invoker.invoke(invocation);
        }
        if (callerAppId == null || timestampValue == null || signature == null) {
            throw new BusinessException(CommonErrorCode.RPC_AUTHENTICATION_REQUIRED);
        }

        long timestamp = parseTimestamp(timestampValue);
        validateTimestamp(timestamp, resolvedProperties);

        String appSecret = resolveAppSecret(callerAppId);
        RpcAuthenticationSigner signer = resolveRpcAuthenticationSigner();
        String expectedSignature = signer.sign(
                callerAppId,
                appSecret,
                invocation.getServiceName(),
                invocation.getMethodName(),
                timestamp
        );
        if (!signer.matches(expectedSignature, signature)) {
            throw new BusinessException(CommonErrorCode.RPC_SIGNATURE_VALIDATION_FAILED);
        }
        applyValidationHooks(invocation, callerAppId, timestamp, signature, resolvedProperties);
        return invoker.invoke(invocation);
    }

    private String resolveTraceMdcKey() {
        if (rpcTraceProperties == null) {
            rpcTraceProperties = SpringContextHolder.getBeanIfAvailable(RpcTraceProperties.class);
        }
        if (rpcTraceProperties == null || !StringUtils.hasText(rpcTraceProperties.getMdcKey())) {
            return DEFAULT_TRACE_MDC_KEY;
        }
        return rpcTraceProperties.getMdcKey().trim();
    }

    private boolean isTraceEnabled() {
        if (rpcTraceProperties == null) {
            rpcTraceProperties = SpringContextHolder.getBeanIfAvailable(RpcTraceProperties.class);
        }
        return rpcTraceProperties == null || rpcTraceProperties.isEnabled();
    }

    private void restoreTraceContext(String previousTraceId, String traceMdcKey, String previousMdcTraceId) {
        TraceContextHolder.restoreTraceId(previousTraceId);
        if (previousMdcTraceId == null) {
            MDC.remove(traceMdcKey);
            return;
        }
        MDC.put(traceMdcKey, previousMdcTraceId);
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

    private long parseTimestamp(String timestampValue) {
        try {
            return Long.parseLong(timestampValue);
        } catch (NumberFormatException exception) {
            throw new BusinessException(CommonErrorCode.RPC_AUTHENTICATION_REQUIRED);
        }
    }

    private void validateTimestamp(long timestamp, RpcSecurityProperties resolvedProperties) {
        long now = System.currentTimeMillis() / 1000;
        long allowedClockSkewSeconds = resolvedProperties.getAuthentication().getAllowedClockSkewSeconds();
        if (Math.abs(now - timestamp) > allowedClockSkewSeconds) {
            throw new BusinessException(CommonErrorCode.RPC_REQUEST_EXPIRED);
        }
    }

    private String resolveAppSecret(String callerAppId) {
        return Optional.ofNullable(resolveRpcCallerSecretResolver())
                .flatMap(resolver -> resolver.resolve(callerAppId))
                .filter(StringUtils::hasText)
                .orElseThrow(() -> new BusinessException(CommonErrorCode.INVALID_APP_KEY));
    }

    private RpcSecurityProperties resolveRpcSecurityProperties() {
        return rpcSecurityProperties != null ? rpcSecurityProperties : SpringContextHolder.getBeanIfAvailable(RpcSecurityProperties.class);
    }

    private RpcCallerSecretResolver resolveRpcCallerSecretResolver() {
        if (rpcCallerSecretResolver == null) {
            rpcCallerSecretResolver = SpringContextHolder.getBeanIfAvailable(RpcCallerSecretResolver.class);
        }
        return rpcCallerSecretResolver;
    }

    private RpcAuthenticationSigner resolveRpcAuthenticationSigner() {
        if (rpcAuthenticationSigner == null) {
            rpcAuthenticationSigner = SpringContextHolder.getBeanIfAvailable(RpcAuthenticationSigner.class);
        }
        if (rpcAuthenticationSigner == null) {
            throw new BusinessException(CommonErrorCode.RPC_SIGNATURE_VALIDATION_FAILED);
        }
        return rpcAuthenticationSigner;
    }

    private void applyValidationHooks(
            Invocation invocation,
            String callerAppId,
            long timestamp,
            String signature,
            RpcSecurityProperties resolvedProperties) {
        List<RpcAuthenticationValidationHook> validationHooks = new ArrayList<>(
                SpringContextHolder.getBeansOfType(RpcAuthenticationValidationHook.class).values()
        );
        AnnotationAwareOrderComparator.sort(validationHooks);
        if (validationHooks.isEmpty()) {
            return;
        }
        RpcAuthenticationValidationContext context = new RpcAuthenticationValidationContext(
                invocation,
                callerAppId,
                timestamp,
                signature,
                resolvedProperties
        );
        validationHooks.forEach(hook -> hook.validate(context));
    }

    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    public void setRpcSecurityProperties(RpcSecurityProperties rpcSecurityProperties) {
        this.rpcSecurityProperties = rpcSecurityProperties;
    }

    public void setRpcCallerSecretResolver(RpcCallerSecretResolver rpcCallerSecretResolver) {
        this.rpcCallerSecretResolver = rpcCallerSecretResolver;
    }

    public void setRpcAuthenticationSigner(RpcAuthenticationSigner rpcAuthenticationSigner) {
        this.rpcAuthenticationSigner = rpcAuthenticationSigner;
    }
}
