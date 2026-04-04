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
package com.getboot.auth.infrastructure.satoken.accessor;

import cn.dev33.satoken.SaManager;
import cn.dev33.satoken.config.SaTokenConfig;
import cn.dev33.satoken.context.SaTokenContextForThreadLocal;
import cn.dev33.satoken.context.SaTokenContextForThreadLocalStorage;
import cn.dev33.satoken.context.model.SaRequest;
import cn.dev33.satoken.context.model.SaResponse;
import cn.dev33.satoken.context.model.SaStorage;
import cn.dev33.satoken.dao.SaTokenDaoDefaultImpl;
import cn.dev33.satoken.stp.StpUtil;
import com.getboot.exception.api.code.CommonErrorCode;
import com.getboot.exception.api.exception.BusinessException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * {@link SaTokenCurrentUserAccessor} 测试。
 *
 * @author qiheng
 */
class SaTokenCurrentUserAccessorTest {

    /**
     * 当前用户访问器。
     */
    private final SaTokenCurrentUserAccessor accessor = new SaTokenCurrentUserAccessor();

    /**
     * 线程级请求上下文。
     */
    private TestSaRequest request;

    /**
     * 线程级响应上下文。
     */
    private TestSaResponse response;

    /**
     * 线程级存储上下文。
     */
    private TestSaStorage storage;

    /**
     * 初始化 Sa-Token 测试上下文。
     */
    @BeforeEach
    void setUp() {
        SaManager.setConfig(new SaTokenConfig());
        SaManager.setSaTokenDao(new SaTokenDaoDefaultImpl());
        SaManager.setSaTokenContext(new SaTokenContextForThreadLocal());

        request = new TestSaRequest();
        response = new TestSaResponse();
        storage = new TestSaStorage();
        SaTokenContextForThreadLocalStorage.setBox(request, response, storage);
    }

    /**
     * 清理 Sa-Token 测试上下文。
     */
    @AfterEach
    void tearDown() {
        SaTokenContextForThreadLocalStorage.clearBox();
    }

    /**
     * 验证会话中已经存储目标类型对象时，直接返回原对象。
     */
    @Test
    void shouldReturnCurrentUserDirectlyWhenSessionValueMatchesType() {
        loginWithUserId(1001L);
        DemoCurrentUser currentUser = new DemoCurrentUser();
        currentUser.setUserId(1001L);
        currentUser.setNickname("alice");
        StpUtil.getSession().set("userInfo", currentUser);

        DemoCurrentUser actualUser = accessor.getCurrentUser(DemoCurrentUser.class);

        assertSame(currentUser, actualUser);
        assertEquals(1001L, accessor.getCurrentUserId());
    }

    /**
     * 验证会话中存储 Map 结构时，会转换为目标用户对象。
     */
    @Test
    void shouldConvertSessionValueToRequestedUserType() {
        loginWithUserId(1002L);
        StpUtil.getSession().set("userInfo", Map.of(
                "userId", 1002L,
                "nickname", "bob"
        ));

        DemoCurrentUser currentUser = accessor.getCurrentUser(DemoCurrentUser.class);

        assertEquals(1002L, currentUser.getUserId());
        assertEquals("bob", currentUser.getNickname());
    }

    /**
     * 验证会话缺少用户对象时，抛出登录过期异常。
     */
    @Test
    void shouldThrowTokenExpiredWhenSessionUserInfoMissing() {
        loginWithUserId(1003L);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> accessor.getCurrentUser(DemoCurrentUser.class)
        );

        assertSame(CommonErrorCode.TOKEN_EXPIRED, exception.getErrorCode());
    }

    /**
     * 验证没有当前登录态时，读取用户 ID 会抛出登录过期异常。
     */
    @Test
    void shouldThrowTokenExpiredWhenCurrentUserIdIsUnavailable() {
        BusinessException exception = assertThrows(BusinessException.class, accessor::getCurrentUserId);

        assertSame(CommonErrorCode.TOKEN_EXPIRED, exception.getErrorCode());
    }

    /**
     * 使用给定用户 ID 建立登录上下文。
     *
     * @param userId 用户 ID
     */
    private void loginWithUserId(Long userId) {
        StpUtil.login(userId);
    }

    /**
     * 测试用当前用户对象。
     */
    private static final class DemoCurrentUser {

        /**
         * 用户 ID。
         */
        private Long userId;

        /**
         * 用户昵称。
         */
        private String nickname;

        /**
         * 返回用户 ID。
         *
         * @return 用户 ID
         */
        public Long getUserId() {
            return userId;
        }

        /**
         * 设置用户 ID。
         *
         * @param userId 用户 ID
         */
        public void setUserId(Long userId) {
            this.userId = userId;
        }

        /**
         * 返回用户昵称。
         *
         * @return 用户昵称
         */
        public String getNickname() {
            return nickname;
        }

        /**
         * 设置用户昵称。
         *
         * @param nickname 用户昵称
         */
        public void setNickname(String nickname) {
            this.nickname = nickname;
        }
    }

    /**
     * 测试用请求对象。
     */
    private static final class TestSaRequest implements SaRequest {

        /**
         * 请求头集合。
         */
        private final Map<String, String> headers = new LinkedHashMap<>();

        /**
         * 请求参数集合。
         */
        private final Map<String, String> params = new LinkedHashMap<>();

        /**
         * Cookie 集合。
         */
        private final Map<String, String> cookies = new LinkedHashMap<>();

        /**
         * 返回原始请求源对象。
         *
         * @return 原始请求源对象
         */
        @Override
        public Object getSource() {
            return this;
        }

        /**
         * 返回请求参数值。
         *
         * @param name 参数名
         * @return 参数值
         */
        @Override
        public String getParam(String name) {
            return params.get(name);
        }

        /**
         * 返回请求参数名列表。
         *
         * @return 参数名列表
         */
        @Override
        public List<String> getParamNames() {
            return new ArrayList<>(params.keySet());
        }

        /**
         * 返回请求参数集合。
         *
         * @return 参数集合
         */
        @Override
        public Map<String, String> getParamMap() {
            return params;
        }

        /**
         * 返回请求头值。
         *
         * @param name 请求头名
         * @return 请求头值
         */
        @Override
        public String getHeader(String name) {
            return headers.get(name);
        }

        /**
         * 返回 Cookie 值。
         *
         * @param name Cookie 名称
         * @return Cookie 值
         */
        @Override
        public String getCookieValue(String name) {
            return cookies.get(name);
        }

        /**
         * 返回请求路径。
         *
         * @return 请求路径
         */
        @Override
        public String getRequestPath() {
            return "/";
        }

        /**
         * 返回请求 URL。
         *
         * @return 请求 URL
         */
        @Override
        public String getUrl() {
            return "http://localhost/test";
        }

        /**
         * 返回请求方法。
         *
         * @return 请求方法
         */
        @Override
        public String getMethod() {
            return "GET";
        }

        /**
         * 模拟请求转发。
         *
         * @param path 转发路径
         * @return 转发结果
         */
        @Override
        public Object forward(String path) {
            return path;
        }
    }

    /**
     * 测试用响应对象。
     */
    private static final class TestSaResponse implements SaResponse {

        /**
         * 响应状态码。
         */
        private int status = 200;

        /**
         * 响应头集合。
         */
        private final Map<String, String> headers = new LinkedHashMap<>();

        /**
         * 返回原始响应源对象。
         *
         * @return 原始响应源对象
         */
        @Override
        public Object getSource() {
            return this;
        }

        /**
         * 设置响应状态码。
         *
         * @param sc 状态码
         * @return 当前响应对象
         */
        @Override
        public SaResponse setStatus(int sc) {
            this.status = sc;
            return this;
        }

        /**
         * 设置响应头。
         *
         * @param name 响应头名
         * @param value 响应头值
         * @return 当前响应对象
         */
        @Override
        public SaResponse setHeader(String name, String value) {
            headers.put(name, value);
            return this;
        }

        /**
         * 追加响应头。
         *
         * @param name 响应头名
         * @param value 响应头值
         * @return 当前响应对象
         */
        @Override
        public SaResponse addHeader(String name, String value) {
            headers.put(name, value);
            return this;
        }

        /**
         * 模拟重定向。
         *
         * @param url 目标地址
         * @return 目标地址
         */
        @Override
        public Object redirect(String url) {
            return url;
        }
    }

    /**
     * 测试用存储对象。
     */
    private static final class TestSaStorage implements SaStorage {

        /**
         * 存储数据集合。
         */
        private final Map<String, Object> values = new LinkedHashMap<>();

        /**
         * 返回原始存储源对象。
         *
         * @return 原始存储源对象
         */
        @Override
        public Object getSource() {
            return this;
        }

        /**
         * 返回指定键对应的值。
         *
         * @param key 键名
         * @return 存储值
         */
        @Override
        public Object get(String key) {
            return values.get(key);
        }

        /**
         * 设置存储值。
         *
         * @param key 键名
         * @param value 存储值
         * @return 当前存储对象
         */
        @Override
        public SaStorage set(String key, Object value) {
            values.put(key, value);
            return this;
        }

        /**
         * 删除指定键。
         *
         * @param key 键名
         * @return 当前存储对象
         */
        @Override
        public SaStorage delete(String key) {
            values.remove(key);
            return this;
        }
    }
}
