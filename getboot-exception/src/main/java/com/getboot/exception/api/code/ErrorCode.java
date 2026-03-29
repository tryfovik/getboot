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
 * 错误码接口定义
 * 所有业务错误码枚举都应实现此接口
 *
 * @author qiheng
 */
public interface ErrorCode {

    /**
     * 获取错误码
     *
     * @return 错误码数值
     */
    Integer code();

    /**
     * 获取错误信息
     *
     * @return 错误描述信息
     */
    String message();

    /**
     * 转换为字符串表示
     *
     * @return 格式: 错误码:错误信息
     */
    default String toCodeString() {
        return code() + ":" + message();
    }
}
