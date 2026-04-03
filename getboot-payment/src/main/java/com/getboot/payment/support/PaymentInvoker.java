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
package com.getboot.payment.support;

import com.getboot.exception.api.exception.BusinessException;

/**
 * 跨渠道统一异常包装工具。
 *
 * <p>将受检异常统一转换为 {@link BusinessException}，避免各渠道实现重复定义相同的 try-catch 模板。</p>
 *
 * @author qiheng
 */
public final class PaymentInvoker {

    /**
     * 工具类不允许实例化。
     */
    private PaymentInvoker() {
    }

    /**
     * 执行有返回值的支付操作，将受检异常包装为 {@link BusinessException}。
     *
     * @param supplier     操作
     * @param errorMessage 失败消息前缀
     * @param <T>          返回类型
     * @return 操作结果
     */
    public static <T> T invoke(CheckedSupplier<T> supplier, String errorMessage) {
        try {
            return supplier.get();
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException(errorMessage + ": " + ex.getMessage(), ex);
        }
    }

    /**
     * 执行无返回值的支付操作，将受检异常包装为 {@link BusinessException}。
     *
     * @param runnable     操作
     * @param errorMessage 失败消息前缀
     */
    public static void invokeVoid(CheckedRunnable runnable, String errorMessage) {
        try {
            runnable.run();
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException(errorMessage + ": " + ex.getMessage(), ex);
        }
    }

    /**
     * 可抛出受检异常的返回值供应器。
     *
     * @param <T> 返回值类型
     */
    @FunctionalInterface
    public interface CheckedSupplier<T> {

        /**
         * 获取操作结果。
         *
         * @return 操作结果
         * @throws Exception 执行失败时抛出
         */
        T get() throws Exception;
    }

    /**
     * 可抛出受检异常的无返回值操作。
     */
    @FunctionalInterface
    public interface CheckedRunnable {

        /**
         * 执行操作。
         *
         * @throws Exception 执行失败时抛出
         */
        void run() throws Exception;
    }
}
