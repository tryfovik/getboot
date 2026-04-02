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
package com.getboot.web.api.request;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 分页请求。
 *
 * @author qiheng
 */
@Data
public class PagingRequest implements Serializable {
    /**
     * 序列化版本号。
     */
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 当前页
     */
    private int currentPage = 1;

    /**
     * 每页结果数
     */
    private int pageSize = 10;
}
