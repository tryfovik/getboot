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
package com.getboot.search.api.constant;

import java.time.Duration;

/**
 * 搜索模块常量。
 *
 * @author qiheng
 */
public final class SearchConstants {

    /**
     * Elasticsearch 实现类型。
     */
    public static final String SEARCH_TYPE_ELASTICSEARCH = "elasticsearch";

    /**
     * 默认页码。
     */
    public static final int DEFAULT_PAGE_NO = 1;

    /**
     * 默认分页大小。
     */
    public static final int DEFAULT_PAGE_SIZE = 20;

    /**
     * 默认最大分页大小。
     */
    public static final int DEFAULT_MAX_PAGE_SIZE = 100;

    /**
     * 默认连接超时时间。
     */
    public static final Duration DEFAULT_CONNECT_TIMEOUT = Duration.ofSeconds(2);

    /**
     * 默认读超时时间。
     */
    public static final Duration DEFAULT_SOCKET_TIMEOUT = Duration.ofSeconds(5);

    /**
     * 工具类不允许实例化。
     */
    private SearchConstants() {
    }
}
