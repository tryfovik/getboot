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

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * 分页响应结果。
 *
 * <p>用于统一承载分页列表数据与分页元信息。</p>
 *
 * @param <T> 记录数据类型
 * @author qiheng
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PagingResponse<T> implements Serializable {
    @Serial
    private static final long serialVersionUID = 8545996863226528798L;
    /**
     * 查询数据列表
     */
    @Builder.Default
    protected List<T> records = Collections.emptyList();

    /**
     * 总数
     */
    @Builder.Default
    protected long total = 0;
    /**
     * 每页显示条数，默认 10
     */
    @Builder.Default
    protected long size = 10;
    /**
     * 当前页
     */
    @Builder.Default
    protected long current = 1;

    /**
     * 总页数
     */
    @Builder.Default
    protected long pages = 1;

}
