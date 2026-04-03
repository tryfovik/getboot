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

import com.getboot.exception.api.exception.BusinessException;
import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.common.extension.DisableInject;
import org.apache.dubbo.common.logger.ErrorTypeAwareLogger;
import org.apache.dubbo.common.logger.LoggerFactory;
import org.apache.dubbo.common.utils.ReflectUtils;
import org.apache.dubbo.common.utils.StringUtils;
import org.apache.dubbo.rpc.*;
import org.apache.dubbo.rpc.filter.ExceptionFilter;
import org.apache.dubbo.rpc.service.GenericService;
import org.apache.dubbo.rpc.support.RpcUtils;

import java.lang.reflect.Method;

import static org.apache.dubbo.common.constants.LoggerCodeConstants.CONFIG_FILTER_VALIDATION_EXCEPTION;

/**
 * Dubbo 异常过滤器扩展。
 *
 * <p>在保留 Dubbo 默认异常处理逻辑的基础上，确保业务异常可按原样透传。</p>
 *
 * @author qiheng
 */
@Activate(group = CommonConstants.PROVIDER)
public class DubboExceptionFilter implements Filter, Filter.Listener {

    /**
     * 服务名日志片段。
     */
    public static final String SERVICE = ". service: ";

    /**
     * 方法名日志片段。
     */
    public static final String METHOD = ", method: ";

    /**
     * 异常日志片段。
     */
    public static final String EXCEPTION = ", exception: ";

    /**
     * Dubbo 错误类型日志器。
     */
    private ErrorTypeAwareLogger logger = LoggerFactory.getErrorTypeAwareLogger(ExceptionFilter.class);

    /**
     * 直接继续执行 Dubbo 调用链。
     *
     * @param invoker 调用执行器
     * @param invocation 调用信息
     * @return 调用结果
     * @throws RpcException RPC 调用异常
     */
    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        return invoker.invoke(invocation);
    }

    /**
     * 在响应返回后按 Dubbo 默认语义处理异常，同时保留业务异常透传。
     *
     * @param appResponse 调用结果
     * @param invoker 调用执行器
     * @param invocation 调用信息
     */
    @Override
    public void onResponse(Result appResponse, Invoker<?> invoker, Invocation invocation) {
        if (appResponse.hasException() && GenericService.class != invoker.getInterface()) {
            try {
                Throwable exception = appResponse.getException();

                // 受检异常直接透传。
                if (!(exception instanceof RuntimeException) && (exception instanceof Exception)) {
                    return;
                }
                // 方法签名中已声明的异常直接透传。
                try {
                    Method method = invoker.getInterface()
                            .getMethod(RpcUtils.getMethodName(invocation), invocation.getParameterTypes());
                    Class<?>[] exceptionClasses = method.getExceptionTypes();
                    for (Class<?> exceptionClass : exceptionClasses) {
                        if (exception.getClass().equals(exceptionClass)) {
                            return;
                        }
                    }
                } catch (NoSuchMethodException e) {
                    return;
                }

                // 对于方法签名中未声明的异常，先记录服务端错误日志。
                logger.error(
                        CONFIG_FILTER_VALIDATION_EXCEPTION,
                        "",
                        "",
                        "Got unchecked and undeclared exception which called by "
                                + RpcContext.getServiceContext().getRemoteHost() + SERVICE
                                + invoker.getInterface().getName() + METHOD + RpcUtils.getMethodName(invocation)
                                + EXCEPTION
                                + exception.getClass().getName() + ": " + exception.getMessage(),
                        exception);

                // 如果异常类与接口类来自同一 jar，则直接透传。
                String serviceFile = ReflectUtils.getCodeBase(invoker.getInterface());
                String exceptionFile = ReflectUtils.getCodeBase(exception.getClass());
                if (serviceFile == null || exceptionFile == null || serviceFile.equals(exceptionFile)) {
                    return;
                }
                // JDK 异常直接透传。
                String className = exception.getClass().getName();
                if (className.startsWith("java.")
                        || className.startsWith("javax.")
                        || className.startsWith("jakarta.")) {
                    return;
                }
                // Dubbo 自身异常直接透传。
                if (exception instanceof RpcException) {
                    return;
                }

                // 业务异常继续保持透明透传。
                if (exception instanceof BusinessException) {
                    return;
                }

                // 其他未声明异常统一包装为 RuntimeException 返回给客户端。
                appResponse.setException(new RuntimeException(StringUtils.toString(exception)));
            } catch (Throwable e) {
                logger.warn(
                        CONFIG_FILTER_VALIDATION_EXCEPTION,
                        "",
                        "",
                        "Fail to ExceptionFilter when called by "
                                + RpcContext.getServiceContext().getRemoteHost() + SERVICE
                                + invoker.getInterface().getName() + METHOD + RpcUtils.getMethodName(invocation)
                                + EXCEPTION
                                + e.getClass().getName() + ": " + e.getMessage(),
                        e);
            }
        }
    }

    /**
     * 在调用链发生错误时记录未声明异常。
     *
     * @param e 异常对象
     * @param invoker 调用执行器
     * @param invocation 调用信息
     */
    @Override
    public void onError(Throwable e, Invoker<?> invoker, Invocation invocation) {
        logger.error(
                CONFIG_FILTER_VALIDATION_EXCEPTION,
                "",
                "",
                "Got unchecked and undeclared exception which called by "
                        + RpcContext.getServiceContext().getRemoteHost() + SERVICE
                        + invoker.getInterface().getName() + METHOD + RpcUtils.getMethodName(invocation)
                        + EXCEPTION
                        + e.getClass().getName() + ": " + e.getMessage(),
                e);
    }

    /**
     * 为测试场景注入日志器。
     *
     * @param logger 错误类型日志器
     */
    @DisableInject
    public void mockLogger(ErrorTypeAwareLogger logger) {
        this.logger = logger;
    }
}
