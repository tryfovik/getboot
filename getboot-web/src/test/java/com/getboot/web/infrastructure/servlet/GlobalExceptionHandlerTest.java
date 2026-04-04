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
package com.getboot.web.infrastructure.servlet;

import com.getboot.exception.api.code.CommonErrorCode;
import com.getboot.exception.api.exception.BusinessException;
import com.getboot.web.api.request.PagingRequest;
import com.getboot.web.api.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link GlobalExceptionHandler} 测试。
 *
 * @author qiheng
 */
class GlobalExceptionHandlerTest {

    /**
     * 全局异常处理器。
     */
    private final GlobalExceptionHandler exceptionHandler = new GlobalExceptionHandler();

    /**
     * 验证业务异常没有消息时会回退到系统默认错误信息。
     */
    @Test
    void shouldFallbackToSystemMessageWhenBusinessExceptionMessageMissing() {
        ApiResponse<Void> response = exceptionHandler.handleBusinessException(new BusinessException((String) null));

        assertEquals(CommonErrorCode.ERROR.code(), response.getCode());
        assertEquals(CommonErrorCode.ERROR.message(), response.getMessage());
        assertTrue(response.isFail());
    }

    /**
     * 验证参数校验异常会按字段去重拼接错误消息。
     *
     * @throws Exception 反射异常
     */
    @Test
    void shouldDeduplicateValidationMessagesForPagingRequestFields() throws Exception {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new PagingRequest(), "pagingRequest");
        bindingResult.addError(new FieldError("pagingRequest", "currentPage", "currentPage must be greater than 0"));
        bindingResult.addError(new FieldError("pagingRequest", "currentPage", "currentPage must be greater than 0"));
        bindingResult.addError(new FieldError("pagingRequest", "pageSize", "pageSize must be greater than 0"));

        Method method = ValidationController.class.getDeclaredMethod("page", PagingRequest.class);
        MethodParameter methodParameter = new MethodParameter(method, 0);
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(methodParameter, bindingResult);

        ApiResponse<Void> response = exceptionHandler.handleMethodArgumentNotValidException(exception);

        assertEquals(CommonErrorCode.PARAM_ERROR.code(), response.getCode());
        assertEquals("currentPage must be greater than 0,pageSize must be greater than 0", response.getMessage());
    }

    /**
     * 验证缺少请求头时会返回带请求头名的错误信息。
     *
     * @throws Exception 反射异常
     */
    @Test
    void shouldReturnHeaderNameWhenRequestHeaderMissing() throws Exception {
        Method method = ValidationController.class.getDeclaredMethod("header", String.class);
        MethodParameter methodParameter = new MethodParameter(method, 0);
        MissingRequestHeaderException exception = new MissingRequestHeaderException("X-Trace-Id", methodParameter);

        ApiResponse<Void> response = exceptionHandler.handleMissingRequestHeaderException(exception);

        assertEquals(CommonErrorCode.REQUEST_HEADER_ERROR.code(), response.getCode());
        assertEquals(
                CommonErrorCode.REQUEST_HEADER_ERROR.message() + " Missing header: X-Trace-Id",
                response.getMessage()
        );
    }

    /**
     * 验证兜底异常会返回系统错误响应。
     */
    @Test
    void shouldReturnSystemErrorWhenUnhandledExceptionOccurs() {
        HttpServletRequest request = (HttpServletRequest) Proxy.newProxyInstance(
                GlobalExceptionHandlerTest.class.getClassLoader(),
                new Class<?>[]{HttpServletRequest.class},
                (proxy, method, args) -> method.getName().equals("getRequestURI") ? "/demo" : null
        );

        ApiResponse<Void> response = exceptionHandler.handleException(request, new IllegalStateException("boom"));

        assertEquals(CommonErrorCode.ERROR.code(), response.getCode());
        assertEquals(CommonErrorCode.ERROR.message(), response.getMessage());
    }

    /**
     * 测试用校验控制器签名。
     */
    private static final class ValidationController {

        /**
         * 测试用分页接口签名。
         *
         * @param request 分页请求
         */
        private void page(PagingRequest request) {
        }

        /**
         * 测试用请求头接口签名。
         *
         * @param traceId 链路标识
         */
        private void header(String traceId) {
        }
    }
}
