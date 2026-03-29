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
package com.getboot.observability.spi;

/**
 * TraceId 生成器。
 *
 * <p>业务方可通过注册该类型 Bean，自定义链路 TraceId 的生成规则。</p>
 *
 * @author qiheng
 */
@FunctionalInterface
public interface TraceIdGenerator {

    /**
     * 生成 TraceId。
     *
     * @return TraceId
     */
    String generate();
}
