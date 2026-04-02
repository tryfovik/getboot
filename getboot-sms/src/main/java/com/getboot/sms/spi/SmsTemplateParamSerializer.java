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
package com.getboot.sms.spi;

import java.util.Map;

/**
 * 短信模板变量序列化扩展点。
 *
 * @author qiheng
 */
public interface SmsTemplateParamSerializer {

    /**
     * 序列化短信模板变量。
     *
     * @param templateParams 模板变量
     * @return 序列化结果
     */
    String serialize(Map<String, Object> templateParams);
}
