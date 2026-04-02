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
package com.getboot.idempotency.support;

import com.getboot.idempotency.api.annotation.Idempotent;
import com.getboot.idempotency.api.exception.IdempotencyException;
import com.getboot.idempotency.api.model.IdempotencyRecord;
import com.getboot.idempotency.api.model.IdempotencyStatus;
import com.getboot.idempotency.spi.IdempotencyDuplicateRequestHandler;

/**
 * Default duplicate idempotent request handler.
 *
 * @author qiheng
 */
public class DefaultIdempotencyDuplicateRequestHandler implements IdempotencyDuplicateRequestHandler {

    @Override
    public Object handleDuplicate(String key, IdempotencyRecord record, Idempotent idempotent) {
        if (record == null || record.getStatus() == null) {
            throw new IdempotencyException("Idempotent request state is missing. key=" + key);
        }
        if (IdempotencyStatus.COMPLETED == record.getStatus()) {
            return record.getResult();
        }
        throw new IdempotencyException(idempotent.message());
    }
}
