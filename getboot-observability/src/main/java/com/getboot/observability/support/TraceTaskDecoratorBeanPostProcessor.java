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
package com.getboot.observability.support;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskDecorator;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;

/**
 * Trace 任务装饰器后处理器。
 *
 * <p>用于为 Spring 常见异步执行器自动挂载 Trace 任务装饰器。</p>
 *
 * @author qiheng
 */
public class TraceTaskDecoratorBeanPostProcessor implements BeanPostProcessor {

    private final TaskDecorator taskDecorator;

    public TraceTaskDecoratorBeanPostProcessor(TaskDecorator taskDecorator) {
        this.taskDecorator = taskDecorator;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof ThreadPoolTaskExecutor taskExecutor && !hasTaskDecorator(taskExecutor, ThreadPoolTaskExecutor.class)) {
            taskExecutor.setTaskDecorator(taskDecorator);
            return bean;
        }
        if (bean instanceof SimpleAsyncTaskExecutor taskExecutor && !hasTaskDecorator(taskExecutor, SimpleAsyncTaskExecutor.class)) {
            taskExecutor.setTaskDecorator(taskDecorator);
            return bean;
        }
        return bean;
    }

    private boolean hasTaskDecorator(Object target, Class<?> targetClass) {
        Field field = ReflectionUtils.findField(targetClass, "taskDecorator");
        if (field == null) {
            return false;
        }
        ReflectionUtils.makeAccessible(field);
        Object decorator = ReflectionUtils.getField(field, target);
        return decorator != null;
    }
}
