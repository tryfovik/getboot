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
package com.getboot.exception.api.exception;

import com.getboot.exception.api.code.ErrorCode;

import java.io.Serial;
import java.io.Serializable;

/**
 * 业务异常类
 * 用于封装业务逻辑中的异常情况
 *
 * @author qiheng
 */
public class BusinessException extends RuntimeException implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 错误码对象
     */
    private ErrorCode errorCode;


    // ==================== 构造方法 ====================

    /**
     * 通过错误码和消息创建业务异常
     *
     * @param code    错误码
     * @param message 错误消息
     */
    public BusinessException(Integer code, String message) {
        super(message);
        this.errorCode = new SimpleErrorCode(code, message);
    }

    /**
     * 通过错误码、消息和原因创建业务异常
     *
     * @param code    错误码
     * @param message 错误消息
     * @param cause   原因异常
     */
    public BusinessException(Integer code, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = new SimpleErrorCode(code, message);
    }

    /**
     * 通过自定义消息创建业务异常
     * 注意：这种情况下errorCode为null
     *
     * @param message 自定义错误消息
     */
    public BusinessException(String message) {
        super(message);
    }

    /**
     * 通过自定义消息和原因创建业务异常
     * 注意：这种情况下errorCode为null
     *
     * @param message 自定义错误消息
     * @param cause   原因异常
     */
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 通过错误码创建业务异常
     *
     * @param errorCode 错误码
     */
    public BusinessException(ErrorCode errorCode) {
        super(errorCode.message());
        this.errorCode = errorCode;
    }

    /**
     * 通过错误码和自定义消息创建业务异常
     * 使用自定义消息覆盖错误码的默认消息
     *
     * @param message   自定义错误消息
     * @param errorCode 错误码
     */
    public BusinessException(String message, ErrorCode errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    // ==================== 便捷方法 ====================


    /**
     * 获取错误码的代码
     * 如果errorCode为null，则返回null
     *
     * @return 错误码代码
     */
    public Integer getErrorCodeValue() {
        return errorCode != null ? errorCode.code() : null;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(ErrorCode errorCode) {
        this.errorCode = errorCode;
    }

    /**
     * 获取完整的错误信息
     * 格式：错误码:错误信息
     * 如果errorCode为null，则只返回异常消息
     *
     * @return 完整错误信息
     */
    public String getFullMessage() {
        if (errorCode != null) {
            return errorCode.code() + ":" + errorCode.message() + " - " + getMessage();
        }
        return getMessage();
    }

    // ==================== 静态工厂方法 ====================

    /**
     * 快速创建业务异常
     *
     * @param code    错误码
     * @param message 错误消息
     * @return 业务异常对象
     */
    public static BusinessException of(Integer code, String message) {
        return new BusinessException(code, message);
    }

    /**
     * 快速创建业务异常
     *
     * @param errorCode 错误码
     * @return 业务异常对象
     */
    public static BusinessException of(ErrorCode errorCode) {
        return new BusinessException(errorCode);
    }

    // ==================== 内部类 ====================

    /**
     * 简单的错误码实现
     * 用于直接传入code和message的场景
     */
    private record SimpleErrorCode(Integer code, String message) implements ErrorCode {
    }
}
