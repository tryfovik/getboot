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
package com.getboot.sms.api.exception;

/**
 * 短信模块异常。
 *
 * @author qiheng
 */
public class SmsException extends RuntimeException {

    /**
     * 使用错误信息构造短信异常。
     *
     * @param message 错误信息
     */
    public SmsException(String message) {
        super(message);
    }

    /**
     * 使用错误信息和根因构造短信异常。
     *
     * @param message 错误信息
     * @param cause 根因异常
     */
    public SmsException(String message, Throwable cause) {
        super(message, cause);
    }
}
