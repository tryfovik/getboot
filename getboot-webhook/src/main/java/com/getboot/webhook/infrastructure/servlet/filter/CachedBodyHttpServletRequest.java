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
package com.getboot.webhook.infrastructure.servlet.filter;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.springframework.util.StreamUtils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * 支持重复读取请求体的请求包装器。
 *
 * <p>用于缓存原始请求体字节数组，方便后续验签、日志等流程重复读取。</p>
 *
 * @author qiheng
 */
public class CachedBodyHttpServletRequest extends HttpServletRequestWrapper {

    /**
     * 缓存后的原始请求体字节数组。
     */
    private final byte[] cachedBody;

    /**
     * 读取请求体时使用的字符集。
     */
    private final Charset charset;

    /**
     * 创建支持重复读取的请求包装器。
     *
     * @param request 原始请求对象
     * @throws IOException 读取请求体失败时抛出
     */
    public CachedBodyHttpServletRequest(HttpServletRequest request) throws IOException {
        super(request);
        this.cachedBody = StreamUtils.copyToByteArray(request.getInputStream());
        this.charset = request.getCharacterEncoding() == null
                ? StandardCharsets.UTF_8
                : Charset.forName(request.getCharacterEncoding());
    }

    /**
     * 返回缓存后的请求体副本。
     *
     * @return 请求体字节数组副本
     */
    public byte[] getCachedBody() {
        return cachedBody.clone();
    }

    /**
     * 返回可重复读取的输入流。
     *
     * @return 请求体输入流
     */
    @Override
    public ServletInputStream getInputStream() {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(cachedBody);
        return new ServletInputStream() {

            /**
             * 读取下一个字节。
             *
             * @return 读取到的字节值
             */
            @Override
            public int read() {
                return inputStream.read();
            }

            /**
             * 判断输入流是否已读取完成。
             *
             * @return 读取完成时返回 {@code true}
             */
            @Override
            public boolean isFinished() {
                return inputStream.available() == 0;
            }

            /**
             * 判断输入流当前是否可读。
             *
             * @return 始终返回 {@code true}
             */
            @Override
            public boolean isReady() {
                return true;
            }

            /**
             * 当前实现不支持异步读取监听器。
             *
             * @param readListener 读取监听器
             */
            @Override
            public void setReadListener(ReadListener readListener) {
                throw new UnsupportedOperationException();
            }
        };
    }

    /**
     * 返回基于缓存请求体的字符读取器。
     *
     * @return 请求体字符读取器
     */
    @Override
    public BufferedReader getReader() {
        return new BufferedReader(new InputStreamReader(getInputStream(), charset));
    }
}
