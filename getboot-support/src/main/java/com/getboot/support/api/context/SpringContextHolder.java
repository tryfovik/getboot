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
package com.getboot.support.api.context;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.NonNull;

import java.util.Map;

/**
 * Spring 上下文持有器。
 *
 * <p>用于在非 Spring 管理对象中按需获取容器中的 Bean 实例。</p>
 *
 * @author qiheng
 */
public class SpringContextHolder implements ApplicationContextAware, DisposableBean {

    /**
     * 静态 Spring 容器引用。
     */
    private static ApplicationContext applicationContext;

    /**
     * 当前 Bean 注入的 Spring 容器引用。
     */
    private ApplicationContext injectedApplicationContext;

    /**
     * 注入当前 Spring 容器，并同步刷新静态引用。
     *
     * @param context Spring 容器
     */
    @Override
    public void setApplicationContext(@NonNull ApplicationContext context) {
        this.injectedApplicationContext = context;
        applicationContext = context;
    }

    /**
     * 按类型获取 Bean。
     *
     * @param beanType Bean 类型
     * @param <T> Bean 泛型
     * @return Bean 实例
     */
    public static <T> T getBean(Class<T> beanType) {
        if (applicationContext == null) {
            throw new IllegalStateException("ApplicationContext has not been initialized.");
        }
        return applicationContext.getBean(beanType);
    }

    /**
     * 按需获取 Bean，不存在时返回 {@code null}。
     *
     * @param beanType Bean 类型
     * @param <T> Bean 泛型
     * @return Bean 实例，不存在时返回 {@code null}
     */
    public static <T> T getBeanIfAvailable(Class<T> beanType) {
        if (applicationContext == null) {
            return null;
        }
        return applicationContext.getBeanProvider(beanType).getIfAvailable();
    }

    /**
     * 获取某类型的全部 Bean。
     *
     * @param beanType Bean 类型
     * @param <T> Bean 泛型
     * @return Bean 名称与实例映射
     */
    public static <T> Map<String, T> getBeansOfType(Class<T> beanType) {
        if (applicationContext == null) {
            return Map.of();
        }
        return applicationContext.getBeansOfType(beanType);
    }

    /**
     * 在 Bean 销毁时清理当前实例写入的静态容器引用。
     */
    @Override
    public void destroy() {
        if (applicationContext == injectedApplicationContext) {
            applicationContext = null;
        }
    }
}
