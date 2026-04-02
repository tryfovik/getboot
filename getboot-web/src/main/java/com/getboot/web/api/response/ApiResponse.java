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
package com.getboot.web.api.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.getboot.exception.api.code.CommonErrorCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 统一响应结果封装。
 *
 * @param <T> 响应数据类型
 * @author qiheng
 */
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
@ToString
public class ApiResponse<T> implements Serializable {
    /**
     * 序列化版本号。
     */
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 成功状态码
     */
    public static final Integer SUCCESS_CODE = CommonErrorCode.SUCCESS.code();

    /**
     * 系统错误码
     */
    public static final Integer SYSTEM_ERROR_CODE = CommonErrorCode.ERROR.code();

    /**
     * 默认成功提示信息
     */
    public static final String DEFAULT_SUCCESS_MESSAGE = CommonErrorCode.SUCCESS.message();

    /**
     * 默认系统错误提示信息
     */
    public static final String DEFAULT_SYSTEM_ERROR_MESSAGE = CommonErrorCode.ERROR.message();

    /**
     * 调试时间格式化器。
     */
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 成功状态字符串
     */
    public static final String SUCCESS_STATUS = "success";

    /**
     * 失败状态字符串
     */
    public static final String FAIL_STATUS = "fail";

    /**
     * 响应状态：success/fail
     */
    private String status = SUCCESS_STATUS;

    /**
     * 响应数据
     */
    private T data;

    /**
     * 响应状态码
     */
    private Integer code = SUCCESS_CODE;

    /**
     * 响应信息
     */
    private String message = DEFAULT_SUCCESS_MESSAGE;

    /**
     * 调试信息
     */
    private DebugInfo debug = new DebugInfo();

    /**
     * 调试信息类
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @Accessors(chain = true)
    @ToString
    public static class DebugInfo implements Serializable {
        /**
         * 序列化版本号。
         */
        @Serial
        private static final long serialVersionUID = 1L;

        /**
         * 追踪ID
         */
        private String tid = "";

        /**
         * 格式化时间
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private String time = LocalDateTime.now().format(TIME_FORMATTER);

        /**
         * 耗时（毫秒）
         */
        private Long cost = 0L;
    }

    /**
     * 私有构造器
     */
    private ApiResponse(String status, T data, Integer code, String message) {
        this.status = status;
        this.data = data;
        this.code = code;
        this.message = message;
    }

    /**
     * 判断响应是否成功
     */
    public boolean isSuccess() {
        return SUCCESS_STATUS.equals(this.status) && SUCCESS_CODE.equals(this.code);
    }

    /**
     * 判断响应是否失败
     */
    public boolean isFail() {
        return !isSuccess();
    }

    /**
     * 设置追踪ID
     *
     * @param tid 追踪ID
     * @return 当前对象
     */
    public ApiResponse<T> setTid(String tid) {
        this.debug.setTid(tid);
        return this;
    }

    /**
     * 设置耗时
     *
     * @param cost 耗时（毫秒）
     * @return 当前对象
     */
    public ApiResponse<T> setCost(Long cost) {
        this.debug.setCost(cost);
        return this;
    }

    // ==================== 成功响应静态方法 ====================

    /**
     * 成功响应（无数据，使用默认成功信息）
     */
    public static <T> ApiResponse<T> success() {
        return new ApiResponse<>(SUCCESS_STATUS, null, SUCCESS_CODE, DEFAULT_SUCCESS_MESSAGE);
    }

    /**
     * 成功响应（带数据，使用默认成功信息）
     *
     * @param data 响应数据
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(SUCCESS_STATUS, data, SUCCESS_CODE, DEFAULT_SUCCESS_MESSAGE);
    }

    /**
     * 成功响应（带数据，自定义提示信息）
     *
     * @param data 响应数据
     * @param message 提示信息
     */
    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(SUCCESS_STATUS, data, SUCCESS_CODE, message);
    }

    /**
     * 成功响应（自定义状态码和提示信息）
     *
     * @param code 状态码
     * @param message 提示信息
     */
    public static <T> ApiResponse<T> success(Integer code, String message) {
        return new ApiResponse<>(SUCCESS_STATUS, null, code, message);
    }

    /**
     * 成功响应（带数据，自定义状态码）
     *
     * @param data 响应数据
     * @param code 状态码
     */
    public static <T> ApiResponse<T> success(T data, Integer code) {
        return new ApiResponse<>(SUCCESS_STATUS, data, code, DEFAULT_SUCCESS_MESSAGE);
    }

    /**
     * 成功响应（带数据，自定义状态码和提示信息）
     *
     * @param data 响应数据
     * @param code 状态码
     * @param message 提示信息
     */
    public static <T> ApiResponse<T> success(T data, Integer code, String message) {
        return new ApiResponse<>(SUCCESS_STATUS, data, code, message);
    }

    // ==================== 失败响应静态方法 ====================

    /**
     * 失败响应（无数据，使用默认系统错误信息）
     */
    public static <T> ApiResponse<T> fail() {
        return new ApiResponse<>(FAIL_STATUS, null, SYSTEM_ERROR_CODE, DEFAULT_SYSTEM_ERROR_MESSAGE);
    }

    /**
     * 失败响应（带数据，使用默认系统错误信息）
     *
     * @param data 响应数据
     */
    public static <T> ApiResponse<T> fail(T data) {
        return new ApiResponse<>(FAIL_STATUS, data, SYSTEM_ERROR_CODE, DEFAULT_SYSTEM_ERROR_MESSAGE);
    }

    /**
     * 失败响应（自定义提示信息）
     *
     * @param message 提示信息
     */
    public static <T> ApiResponse<T> fail(String message) {
        return new ApiResponse<>(FAIL_STATUS, null, SYSTEM_ERROR_CODE, message);
    }

    /**
     * 失败响应（带数据，自定义提示信息）
     *
     * @param data 响应数据
     * @param message 提示信息
     */
    public static <T> ApiResponse<T> fail(T data, String message) {
        return new ApiResponse<>(FAIL_STATUS, data, SYSTEM_ERROR_CODE, message);
    }

    /**
     * 失败响应（自定义状态码和提示信息）
     *
     * @param code 状态码
     * @param message 提示信息
     */
    public static <T> ApiResponse<T> fail(Integer code, String message) {
        return new ApiResponse<>(FAIL_STATUS, null, code, message);
    }

    /**
     * 失败响应（带数据，自定义状态码）
     *
     * @param data 响应数据
     * @param code 状态码
     */
    public static <T> ApiResponse<T> fail(T data, Integer code) {
        return new ApiResponse<>(FAIL_STATUS, data, code, DEFAULT_SYSTEM_ERROR_MESSAGE);
    }

    /**
     * 失败响应（带数据，自定义状态码和提示信息）
     *
     * @param data 响应数据
     * @param code 状态码
     * @param message 提示信息
     */
    public static <T> ApiResponse<T> fail(T data, Integer code, String message) {
        return new ApiResponse<>(FAIL_STATUS, data, code, message);
    }

    // ==================== 链式调用方法 ====================

    /**
     * 设置响应数据
     *
     * @param data 响应数据
     * @return 当前对象
     */
    public ApiResponse<T> setData(T data) {
        this.data = data;
        return this;
    }

    /**
     * 设置提示信息
     *
     * @param message 提示信息
     * @return 当前对象
     */
    public ApiResponse<T> setMessage(String message) {
        this.message = message;
        return this;
    }

    /**
     * 设置状态码
     *
     * @param code 状态码
     * @return 当前对象
     */
    public ApiResponse<T> setCode(Integer code) {
        this.code = code;
        return this;
    }

    /**
     * 设置响应状态
     *
     * @param status 响应状态
     * @return 当前对象
     */
    public ApiResponse<T> setStatus(String status) {
        this.status = status;
        return this;
    }
}
