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
package com.getboot.payment.support.wechatpay;

import com.getboot.payment.infrastructure.wechatpay.WechatPayAmounts;
import com.wechat.pay.java.service.payments.model.Transaction;
import com.wechat.pay.java.service.refund.model.Refund;
import com.wechat.pay.java.service.refund.model.RefundNotification;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 微信支付响应映射辅助工具。
 *
 * @author qiheng
 */
public final class WechatPayResponseSupport {

    private WechatPayResponseSupport() {
    }

    public static Map<String, String> buildBaseMetadata(String appId, String merchantId) {
        Map<String, String> metadata = new LinkedHashMap<>();
        putIfText(metadata, "appId", appId);
        putIfText(metadata, "merchantId", merchantId);
        return metadata;
    }

    public static Map<String, String> buildCombineBaseMetadata(
            String combineAppId,
            String combineMerchantId,
            int subOrderCount) {
        Map<String, String> metadata = new LinkedHashMap<>();
        putIfText(metadata, "combineAppId", combineAppId);
        putIfText(metadata, "combineMerchantId", combineMerchantId);
        metadata.put("subOrderCount", String.valueOf(subOrderCount));
        return metadata;
    }

    public static Map<String, String> buildTransactionMetadata(Transaction transaction) {
        Map<String, String> metadata = new LinkedHashMap<>();
        if (transaction == null) {
            return metadata;
        }
        putIfText(metadata, "tradeType", enumName(transaction.getTradeType()));
        putIfText(metadata, "tradeStateDesc", transaction.getTradeStateDesc());
        putIfText(metadata, "bankType", transaction.getBankType());
        putIfText(metadata, "attach", transaction.getAttach());
        putIfText(metadata, "mchId", transaction.getMchid());
        return metadata;
    }

    public static Map<String, String> buildRefundMetadata(Refund refund) {
        Map<String, String> metadata = new LinkedHashMap<>();
        if (refund == null) {
            return metadata;
        }
        putIfText(metadata, "transactionId", refund.getTransactionId());
        putIfText(metadata, "channel", enumName(refund.getChannel()));
        putIfText(metadata, "fundsAccount", enumName(refund.getFundsAccount()));
        putIfText(metadata, "userReceivedAccount", refund.getUserReceivedAccount());
        return metadata;
    }

    public static Map<String, String> buildRefundNotificationMetadata(RefundNotification refundNotification) {
        Map<String, String> metadata = new LinkedHashMap<>();
        if (refundNotification == null) {
            return metadata;
        }
        putIfText(metadata, "userReceivedAccount", refundNotification.getUserReceivedAccount());
        putIfText(metadata, "refundId", refundNotification.getRefundId());
        putIfText(metadata, "transactionId", refundNotification.getTransactionId());
        return metadata;
    }

    public static Map<String, String> buildCombineMetadata(
            String state,
            String transactionId,
            Long settlementRate) {
        Map<String, String> metadata = new LinkedHashMap<>();
        putIfText(metadata, "state", state);
        putIfText(metadata, "transactionId", transactionId);
        putIfText(metadata, "settlementRate", settlementRate);
        return metadata;
    }

    public static Map<String, String> buildCombineSubOrderMetadata(
            String attach,
            String bankType,
            Long settlementRate) {
        Map<String, String> metadata = new LinkedHashMap<>();
        putIfText(metadata, "attach", attach);
        putIfText(metadata, "bankType", bankType);
        putIfText(metadata, "settlementRate", settlementRate);
        return metadata;
    }

    public static BigDecimal amountFromTransaction(Transaction transaction) {
        if (transaction == null || transaction.getAmount() == null) {
            return null;
        }
        return amountFromFen(transaction.getAmount().getPayerTotal());
    }

    public static String currencyFromTransaction(Transaction transaction) {
        if (transaction == null || transaction.getAmount() == null) {
            return null;
        }
        return firstNonBlank(transaction.getAmount().getPayerCurrency(), transaction.getAmount().getCurrency());
    }

    public static BigDecimal amountFromFen(Number value) {
        return value == null ? null : WechatPayAmounts.fromFen(value.longValue());
    }

    public static String enumName(Enum<?> value) {
        return value == null ? null : value.name();
    }

    public static String firstNonBlank(String... values) {
        for (String value : values) {
            if (StringUtils.hasText(value)) {
                return value;
            }
        }
        return null;
    }

    private static void putIfText(Map<String, String> metadata, String key, Object value) {
        if (metadata == null || !StringUtils.hasText(key) || value == null) {
            return;
        }
        String text = String.valueOf(value);
        if (StringUtils.hasText(text)) {
            metadata.put(key, text);
        }
    }
}
