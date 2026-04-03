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

    /**
     * 默认写入 MDC 的 Trace 键名。
     */
    private static final String DEFAULT_TRACE_MDC_KEY = "traceId";

    /**
     * RPC 安全配置。
     */
    private RpcSecurityProperties rpcSecurityProperties;

    /**
     * 调用方密钥解析器。
     */
    private RpcCallerSecretResolver rpcCallerSecretResolver;

    /**
     * RPC 认证签名器。
     */
    private RpcAuthenticationSigner rpcAuthenticationSigner;

    /**
     * RPC Trace 配置。
     */
    private RpcTraceProperties rpcTraceProperties;

    /**
     * 在服务端调用链路中完成 Trace 恢复与认证校验。
     *
     * @param invoker 调用执行器
     * @param invocation 调用信息
     * @return 调用结果
     */
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

    /**
     * 执行实际的服务端认证校验逻辑。
     *
     * @param invoker 调用执行器
     * @param invocation 调用信息
     * @return 调用结果
     */
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

    /**
     * 解析用于写入 MDC 的 Trace 键名。
     *
     * @return Trace 键名
     */
    private String resolveTraceMdcKey() {
        if (rpcTraceProperties == null) {
            rpcTraceProperties = SpringContextHolder.getBeanIfAvailable(RpcTraceProperties.class);
        }
        if (rpcTraceProperties == null || !StringUtils.hasText(rpcTraceProperties.getMdcKey())) {
            return DEFAULT_TRACE_MDC_KEY;
        }
        return rpcTraceProperties.getMdcKey().trim();
    }

    /**
     * 判断当前是否启用 Trace 恢复与透传。
     *
     * @return 启用时返回 {@code true}
     */
    private boolean isTraceEnabled() {
        if (rpcTraceProperties == null) {
            rpcTraceProperties = SpringContextHolder.getBeanIfAvailable(RpcTraceProperties.class);
        }
        return rpcTraceProperties == null || rpcTraceProperties.isEnabled();
    }

    /**
     * 恢复调用前的 TraceContextHolder 与 MDC 状态。
     *
     * @param previousTraceId 之前绑定的 TraceId
     * @param traceMdcKey Trace 的 MDC 键名
     * @param previousMdcTraceId 之前 MDC 中的 Trace 值
     */
    private void restoreTraceContext(String previousTraceId, String traceMdcKey, String previousMdcTraceId) {
        TraceContextHolder.restoreTraceId(previousTraceId);
        if (previousMdcTraceId == null) {
            MDC.remove(traceMdcKey);
            return;
        }
        MDC.put(traceMdcKey, previousMdcTraceId);
    }

    /**
     * 判断当前调用是否需要跳过认证处理。
     *
     * @param invocation 调用信息
     * @param resolvedProperties RPC 安全配置
     * @return 跳过认证时返回 {@code true}
     */
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

    /**
     * 解析请求时间戳。
     *
     * @param timestampValue 时间戳文本
     * @return 时间戳数值
     */
    private long parseTimestamp(String timestampValue) {
        try {
            return Long.parseLong(timestampValue);
        } catch (NumberFormatException exception) {
            throw new BusinessException(CommonErrorCode.RPC_AUTHENTICATION_REQUIRED);
        }
    }

    /**
     * 校验请求时间戳是否仍在允许范围内。
     *
     * @param timestamp 请求时间戳
     * @param resolvedProperties RPC 安全配置
     */
    private void validateTimestamp(long timestamp, RpcSecurityProperties resolvedProperties) {
        long now = System.currentTimeMillis() / 1000;
        long allowedClockSkewSeconds = resolvedProperties.getAuthentication().getAllowedClockSkewSeconds();
        if (Math.abs(now - timestamp) > allowedClockSkewSeconds) {
            throw new BusinessException(CommonErrorCode.RPC_REQUEST_EXPIRED);
        }
    }

    /**
     * 根据调用方应用标识解析签名密钥。
     *
     * @param callerAppId 调用方应用标识
     * @return 调用方签名密钥
     */
    private String resolveAppSecret(String callerAppId) {
        return Optional.ofNullable(resolveRpcCallerSecretResolver())
                .flatMap(resolver -> resolver.resolve(callerAppId))
                .filter(StringUtils::hasText)
                .orElseThrow(() -> new BusinessException(CommonErrorCode.INVALID_APP_KEY));
    }

    /**
     * 解析 RPC 安全配置。
     *
     * @return RPC 安全配置
     */
    private RpcSecurityProperties resolveRpcSecurityProperties() {
        return rpcSecurityProperties != null ? rpcSecurityProperties : SpringContextHolder.getBeanIfAvailable(RpcSecurityProperties.class);
    }

    /**
     * 解析调用方密钥解析器。
     *
     * @return 调用方密钥解析器
     */
    private RpcCallerSecretResolver resolveRpcCallerSecretResolver() {
        if (rpcCallerSecretResolver == null) {
            rpcCallerSecretResolver = SpringContextHolder.getBeanIfAvailable(RpcCallerSecretResolver.class);
        }
        return rpcCallerSecretResolver;
    }

    /**
     * 解析 RPC 认证签名器。
     *
     * @return RPC 认证签名器
     */
    private RpcAuthenticationSigner resolveRpcAuthenticationSigner() {
        if (rpcAuthenticationSigner == null) {
            rpcAuthenticationSigner = SpringContextHolder.getBeanIfAvailable(RpcAuthenticationSigner.class);
        }
        if (rpcAuthenticationSigner == null) {
            throw new BusinessException(CommonErrorCode.RPC_SIGNATURE_VALIDATION_FAILED);
        }
        return rpcAuthenticationSigner;
    }

    /**
     * 执行扩展认证校验钩子。
     *
     * @param invocation 调用信息
     * @param callerAppId 调用方应用标识
     * @param timestamp 请求时间戳
     * @param signature 请求签名
     * @param resolvedProperties RPC 安全配置
     */
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

    /**
     * 将空白字符串规范化为 {@code null}。
     *
     * @param value 原始字符串
     * @return 规范化后的字符串
     */
    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    /**
     * 注入 RPC 安全配置。
     *
     * @param rpcSecurityProperties RPC 安全配置
     */
    public void setRpcSecurityProperties(RpcSecurityProperties rpcSecurityProperties) {
        this.rpcSecurityProperties = rpcSecurityProperties;
    }

    /**
     * 注入调用方密钥解析器。
     *
     * @param rpcCallerSecretResolver 调用方密钥解析器
     */
    public void setRpcCallerSecretResolver(RpcCallerSecretResolver rpcCallerSecretResolver) {
        this.rpcCallerSecretResolver = rpcCallerSecretResolver;
    }

    /**
     * 注入 RPC 认证签名器。
     *
     * @param rpcAuthenticationSigner RPC 认证签名器
     */
    public void setRpcAuthenticationSigner(RpcAuthenticationSigner rpcAuthenticationSigner) {
        this.rpcAuthenticationSigner = rpcAuthenticationSigner;
    }
}
