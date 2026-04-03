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

    /**
     * 工具类不允许实例化。
     */
    private WechatPayResponseSupport() {
    }

    /**
     * 构建统一支付基础元数据。
     *
     * @param appId 应用 ID
     * @param merchantId 商户号
     * @return 基础元数据
     */
    public static Map<String, String> buildBaseMetadata(String appId, String merchantId) {
        Map<String, String> metadata = new LinkedHashMap<>();
        putIfText(metadata, "appId", appId);
        putIfText(metadata, "merchantId", merchantId);
        return metadata;
    }

    /**
     * 构建合单支付基础元数据。
     *
     * @param combineAppId 合单应用 ID
     * @param combineMerchantId 合单商户号
     * @param subOrderCount 子订单数量
     * @return 合单基础元数据
     */
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

    /**
     * 构建交易查询元数据。
     *
     * @param transaction 交易对象
     * @return 交易元数据
     */
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

    /**
     * 构建退款查询元数据。
     *
     * @param refund 退款对象
     * @return 退款元数据
     */
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

    /**
     * 构建退款通知元数据。
     *
     * @param refundNotification 退款通知对象
     * @return 退款通知元数据
     */
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

    /**
     * 构建合单维度元数据。
     *
     * @param state 合单状态
     * @param transactionId 微信交易单号
     * @param settlementRate 清算汇率
     * @return 合单元数据
     */
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

    /**
     * 构建合单子订单元数据。
     *
     * @param attach 附加数据
     * @param bankType 银行类型
     * @param settlementRate 清算汇率
     * @return 子订单元数据
     */
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

    /**
     * 从交易对象读取支付金额。
     *
     * @param transaction 交易对象
     * @return 支付金额
     */
    public static BigDecimal amountFromTransaction(Transaction transaction) {
        if (transaction == null || transaction.getAmount() == null) {
            return null;
        }
        return amountFromFen(transaction.getAmount().getPayerTotal());
    }

    /**
     * 从交易对象读取币种。
     *
     * @param transaction 交易对象
     * @return 币种
     */
    public static String currencyFromTransaction(Transaction transaction) {
        if (transaction == null || transaction.getAmount() == null) {
            return null;
        }
        return firstNonBlank(transaction.getAmount().getPayerCurrency(), transaction.getAmount().getCurrency());
    }

    /**
     * 将分转换为元。
     *
     * @param value 分单位金额
     * @return 元单位金额
     */
    public static BigDecimal amountFromFen(Number value) {
        return value == null ? null : WechatPayAmounts.fromFen(value.longValue());
    }

    /**
     * 返回枚举名称。
     *
     * @param value 枚举值
     * @return 枚举名称
     */
    public static String enumName(Enum<?> value) {
        return value == null ? null : value.name();
    }

    /**
     * 返回第一个非空白文本。
     *
     * @param values 候选文本
     * @return 第一个非空白值
     */
    public static String firstNonBlank(String... values) {
        for (String value : values) {
            if (StringUtils.hasText(value)) {
                return value;
            }
        }
        return null;
    }

    /**
     * 在值可用时写入元数据。
     *
     * @param metadata 元数据对象
     * @param key 键名
     * @param value 值
     */
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
