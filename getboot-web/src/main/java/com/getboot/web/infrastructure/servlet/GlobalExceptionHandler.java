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
import com.getboot.exception.api.code.ErrorCode;
import com.getboot.exception.api.exception.BusinessException;
import com.getboot.web.api.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 全局异常处理器。
 *
 * <p>统一将常见业务异常、参数异常与系统异常转换为标准响应结构。</p>
 *
 * @author qiheng
 */
@Slf4j
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ApiResponse<Void> handleBusinessException(BusinessException businessException) {
        Integer code = CommonErrorCode.ERROR.code();
        String message = businessException.getMessage();

        ErrorCode errorCode = businessException.getErrorCode();
        if (errorCode != null) {
            code = errorCode.code();
            // Prefer the custom message carried by the business exception.
            message = businessException.getMessage();
        }

        if (message == null || message.isEmpty()) {
            message = CommonErrorCode.ERROR.message();
        }

        log.error("Business exception. code={}, message={}", code, message, businessException);
        return ApiResponse.fail(code, message);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponse<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        BindingResult bindingResult = ex.getBindingResult();
        StringBuilder sb = new StringBuilder();
        Set<String> processedFields = new HashSet<>();
        List<FieldError> fieldErrors = bindingResult.getFieldErrors();

        for (FieldError fieldError : fieldErrors) {
            String fieldName = fieldError.getField();
            if (!processedFields.contains(fieldName)) {
                sb.append(fieldError.getDefaultMessage()).append(",");
                processedFields.add(fieldName);
            }
        }

        if (!sb.isEmpty()) {
            sb.deleteCharAt(sb.length() - 1);
        }

        String errorMessage = sb.toString();
        log.warn("Request validation failed: {}", errorMessage);
        return ApiResponse.fail(CommonErrorCode.PARAM_ERROR.code(), errorMessage);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ApiResponse<Void> handleNoResourceFoundException(NoResourceFoundException ex) {
        log.warn("Resource not found: {}", ex.getMessage());
        return ApiResponse.fail(CommonErrorCode.NOT_FOUND.code(), CommonErrorCode.NOT_FOUND.message());
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ApiResponse<Void> handleMissingRequestHeaderException(MissingRequestHeaderException ex) {
        String customMessage = CommonErrorCode.REQUEST_HEADER_ERROR.message() + " Missing header: " + ex.getHeaderName();
        log.warn("Missing request header: {} - {}", ex.getHeaderName(), ex.getMessage());
        return ApiResponse.fail(CommonErrorCode.REQUEST_HEADER_ERROR.code(), customMessage);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ApiResponse<Void> handleHttpMediaTypeNotSupportedException(HttpMediaTypeNotSupportedException ex) {
        log.warn("Unsupported media type: {}", ex.getMessage());
        return ApiResponse.fail(CommonErrorCode.UNSUPPORTED_MEDIA_TYPE.code(), CommonErrorCode.UNSUPPORTED_MEDIA_TYPE.message());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ApiResponse<Void> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException ex) {
        log.warn("HTTP method not allowed: {}", ex.getMessage());
        return ApiResponse.fail(CommonErrorCode.METHOD_NOT_ALLOWED.code(), CommonErrorCode.METHOD_NOT_ALLOWED.message());
    }

    @ExceptionHandler(Exception.class)
    public ApiResponse<Void> handleException(HttpServletRequest request, Exception ex) {
        log.error("Unhandled exception. path={}, message={}", request.getRequestURI(), ex.getMessage(), ex);
        return ApiResponse.fail(CommonErrorCode.ERROR.code(), CommonErrorCode.ERROR.message());
    }
}
