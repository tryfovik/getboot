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
package com.getboot.exception.api.code;

/**
 * 业务通用错误码。
 *
 * <p>定义系统级别的通用业务错误码与默认提示信息。</p>
 *
 * @author qiheng
 */
public enum CommonErrorCode implements ErrorCode {

    /**
     * 通用成功
     */
    SUCCESS(200, "Request succeeded"),


    /**
     * 通用系统错误
     */
    ERROR(500, "An unexpected error occurred. Please try again later."),

    /**
     * 参数校验错误
     */
    PARAM_ERROR(422, "Parameter validation failed."),


    /**
     * 未授权访问
     */
    UNAUTHORIZED(401, "Unauthorized access. Check whether the access token is missing or expired."),

    /**
     * 禁止访问
     */
    FORBIDDEN(403, "Access forbidden"),

    /**
     * 资源不存在
     */
    NOT_FOUND(404, "The requested resource was not found."),

    /**
     * 请求头错误
     */
    REQUEST_HEADER_ERROR(400, "Invalid request header."),

    /**
     * 不支持的媒体类型
     */
    UNSUPPORTED_MEDIA_TYPE(415, "Unsupported media type."),

    /**
     * 请求方法不支持
     */
    METHOD_NOT_ALLOWED(405, "HTTP method not allowed."),

    /**
     * 请求过于频繁
     */
    TOO_MANY_REQUESTS(429, "Too many requests. Please try again later."),

    /**
     * 请求处理中
     */
    REQUEST_PROCESSING(409, "The request is being processed. Do not submit it again."),

    /**
     * 请求已经处理
     */
    REQUEST_ALREADY_PROCESSED(409, "The request has already been processed."),

    /**
     * 缺少必要请求头
     */
    MISSING_REQUIRED_HEADERS(400, "Missing required request headers."),

    /**
     * 请求已过期
     */
    EXPIRED_REQUEST(400, "The request has expired."),

    /**
     * 应用标识无效
     */
    INVALID_APP_KEY(401, "Invalid application key."),

    /**
     * 校验和验证失败
     */
    CHECKSUM_VALIDATION_FAILED(401, "Signature validation failed."),

    /**
     * RPC 鉴权元数据缺失
     */
    RPC_AUTHENTICATION_REQUIRED(401, "Missing required RPC authentication metadata."),

    /**
     * RPC 请求时间戳已过期
     */
    RPC_REQUEST_EXPIRED(401, "RPC request timestamp is outside the allowed time window."),

    /**
     * RPC 签名校验失败
     */
    RPC_SIGNATURE_VALIDATION_FAILED(401, "RPC signature validation failed."),

    /**
     * 令牌已过期
     */
    TOKEN_EXPIRED(401, "Login session expired. Please sign in again.");
    private final Integer code;
    private final String message;

    CommonErrorCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public Integer code() {
        return this.code;
    }

    @Override
    public String message() {
        return this.message;
    }

    /**
     * 根据错误码获取枚举实例
     *
     * @param code 错误码
     * @return 对应的枚举实例，找不到时返回null
     */
    public static CommonErrorCode fromCode(Integer code) {
        for (CommonErrorCode errorCode : CommonErrorCode.values()) {
            if (errorCode.code().equals(code)) {
                return errorCode;
            }
        }
        return null;
    }
}
