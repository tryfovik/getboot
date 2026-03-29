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
package com.getboot.payment.support.registry;

import com.getboot.payment.api.model.PaymentChannel;
import com.getboot.payment.api.model.PaymentMode;
import com.getboot.payment.api.request.PaymentCloseRequest;
import com.getboot.payment.api.request.PaymentCreateRequest;
import com.getboot.payment.api.request.PaymentNotifyRequest;
import com.getboot.payment.api.request.PaymentOrderQueryRequest;
import com.getboot.payment.api.request.PaymentRefundRequest;
import com.getboot.payment.api.request.PaymentRefundQueryRequest;
import com.getboot.payment.api.response.PaymentCloseResponse;
import com.getboot.payment.api.response.PaymentCreateResponse;
import com.getboot.payment.api.response.PaymentNotifyResponse;
import com.getboot.payment.api.response.PaymentOrderQueryResponse;
import com.getboot.payment.api.response.PaymentRefundResponse;
import com.getboot.payment.api.response.PaymentRefundQueryResponse;
import com.getboot.payment.api.service.PaymentService;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultPaymentServiceRegistryTest {

    @Test
    void shouldReturnRegisteredService() {
        PaymentService paymentService = new StubPaymentService(PaymentChannel.ALIPAY);
        DefaultPaymentServiceRegistry registry = new DefaultPaymentServiceRegistry(List.of(paymentService));

        assertTrue(registry.get(PaymentChannel.ALIPAY).isPresent());
        assertEquals(paymentService, registry.getRequired(PaymentChannel.ALIPAY));
    }

    @Test
    void shouldRejectDuplicateChannelRegistration() {
        assertThrows(IllegalStateException.class, () -> new DefaultPaymentServiceRegistry(List.of(
                new StubPaymentService(PaymentChannel.ALIPAY),
                new StubPaymentService(PaymentChannel.ALIPAY)
        )));
    }

    private record StubPaymentService(PaymentChannel channel) implements PaymentService {
        @Override
        public Set<PaymentMode> supportedModes() {
            return Set.of(PaymentMode.APP);
        }

        @Override
        public PaymentCreateResponse create(PaymentCreateRequest request) {
            return null;
        }

        @Override
        public PaymentRefundResponse refund(PaymentRefundRequest request) {
            return null;
        }

        @Override
        public PaymentOrderQueryResponse queryOrder(PaymentOrderQueryRequest request) {
            return null;
        }

        @Override
        public PaymentRefundQueryResponse queryRefund(PaymentRefundQueryRequest request) {
            return null;
        }

        @Override
        public PaymentCloseResponse close(PaymentCloseRequest request) {
            return null;
        }

        @Override
        public PaymentNotifyResponse parseNotify(PaymentNotifyRequest request) {
            return null;
        }
    }
}
