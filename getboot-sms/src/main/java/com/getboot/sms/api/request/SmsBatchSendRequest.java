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
package com.getboot.sms.api.request;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 批量短信发送请求。
 *
 * @author qiheng
 */
@Data
public class SmsBatchSendRequest {

    /**
     * 业务场景。
     */
    private String scene;

    /**
     * 短信模板编码。
     */
    private String templateCode;

    /**
     * 外部业务流水号。
     */
    private String outId;

    /**
     * 批量接收项。
     */
    private List<SmsBatchSendItem> items = new ArrayList<>();

    /**
     * 设置批量接收项。
     *
     * @param items 批量接收项
     */
    public void setItems(List<SmsBatchSendItem> items) {
        this.items = items == null ? new ArrayList<>() : new ArrayList<>(items);
    }
}
