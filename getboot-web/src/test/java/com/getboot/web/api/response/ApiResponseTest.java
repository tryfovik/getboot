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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 统一响应测试。
 *
 * @author qiheng
 */
class ApiResponseTest {

    /**
     * 验证默认成功响应。
     */
    @Test
    void shouldCreateDefaultSuccessResponse() {
        ApiResponse<String> response = ApiResponse.success("ok");

        assertTrue(response.isSuccess());
        assertFalse(response.isFail());
        assertEquals(ApiResponse.SUCCESS_STATUS, response.getStatus());
        assertEquals(ApiResponse.SUCCESS_CODE, response.getCode());
        assertEquals("ok", response.getData());
    }

    /**
     * 验证默认失败响应。
     */
    @Test
    void shouldCreateDefaultFailResponse() {
        ApiResponse<Void> response = ApiResponse.fail();

        assertFalse(response.isSuccess());
        assertTrue(response.isFail());
        assertEquals(ApiResponse.FAIL_STATUS, response.getStatus());
        assertEquals(ApiResponse.SYSTEM_ERROR_CODE, response.getCode());
    }

    /**
     * 验证调试信息链式设置。
     */
    @Test
    void shouldUpdateDebugInfo() {
        ApiResponse<Void> response = ApiResponse.<Void>success()
                .setTid("trace-123")
                .setCost(25L);

        assertEquals("trace-123", response.getDebug().getTid());
        assertEquals(25L, response.getDebug().getCost());
    }

    /**
     * 验证自定义成功状态码不会被误判为默认成功响应。
     */
    @Test
    void shouldTreatCustomSuccessCodeAsNonDefaultSuccess() {
        ApiResponse<Void> response = ApiResponse.success(201, "created");

        assertFalse(response.isSuccess());
        assertTrue(response.isFail());
        assertEquals(ApiResponse.SUCCESS_STATUS, response.getStatus());
        assertEquals(201, response.getCode());
        assertEquals("created", response.getMessage());
    }

    /**
     * 验证带数据和自定义状态码的失败响应会保留响应上下文。
     */
    @Test
    void shouldCreateFailResponseWithDataAndCustomCode() {
        ApiResponse<String> response = ApiResponse.fail("payload", 409, "conflict");

        assertTrue(response.isFail());
        assertEquals(ApiResponse.FAIL_STATUS, response.getStatus());
        assertEquals(409, response.getCode());
        assertEquals("payload", response.getData());
        assertEquals("conflict", response.getMessage());
    }
}
