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
package com.getboot.job.support;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Objects;

/**
 * Cron 表达式工具类。
 *
 * <p>用于根据日期或调度片段快速构建 XXL-JOB 所需的 Cron 表达式。</p>
 *
 * @author qiheng
 */
public final class CronExpressionUtils {

    /**
     * 日期转 Cron 表达式时使用的格式。
     */
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("ss mm HH dd MM ? yyyy");

    /**
     * 工具类不允许实例化。
     */
    private CronExpressionUtils() {
    }

    /**
     * 根据日期生成 Cron 表达式。
     *
     * @param date 日期对象
     * @return Cron 表达式
     */
    public static String fromDate(Date date) {
        if (Objects.isNull(date)) {
            return null;
        }
        return DATE_FORMAT.format(date.toInstant().atZone(ZoneId.systemDefault()));
    }

    /**
     * 按完整七段式参数拼接 Cron 表达式。
     *
     * @param year 年份段
     * @param week 星期段
     * @param month 月份段
     * @param day 日期段
     * @param hour 小时段
     * @param minutes 分钟段
     * @param seconds 秒段
     * @return Cron 表达式
     */
    public static String build(String year, String week, String month, String day,
                               String hour, String minutes, String seconds) {
        return seconds + " " + minutes + " " + hour + " " + day + " " + month + " " + week + " " + year;
    }

    /**
     * 按六段式参数拼接 Cron 表达式，年份默认为任意。
     *
     * @param week 星期段
     * @param month 月份段
     * @param day 日期段
     * @param hour 小时段
     * @param minutes 分钟段
     * @param seconds 秒段
     * @return Cron 表达式
     */
    public static String build(String week, String month, String day,
                               String hour, String minutes, String seconds) {
        return build("*", week, month, day, hour, minutes, seconds);
    }

    /**
     * 构建按月日触发的 Cron 表达式。
     *
     * @param month 月份段
     * @param day 日期段
     * @param hour 小时段
     * @param minutes 分钟段
     * @param seconds 秒段
     * @return Cron 表达式
     */
    static String buildForMonthDay(String month, String day, String hour, String minutes, String seconds) {
        return build("?", month, day, hour, minutes, seconds);
    }

    /**
     * 按完整七段式参数拼接区间 Cron 表达式。
     *
     * @param year 年份段
     * @param week 星期段
     * @param month 月份段
     * @param day 日期段
     * @param hour 小时段
     * @param minutes 分钟段
     * @param seconds 秒段
     * @return Cron 表达式
     */
    public static String buildRange(String year, String week, String month, String day,
                                    String hour, String minutes, String seconds) {
        return build(year, week, month, day, hour, minutes, seconds);
    }

    /**
     * 按六段式参数拼接区间 Cron 表达式，年份默认为任意。
     *
     * @param week 星期段
     * @param month 月份段
     * @param day 日期段
     * @param hour 小时段
     * @param minutes 分钟段
     * @param seconds 秒段
     * @return Cron 表达式
     */
    public static String buildRange(String week, String month, String day,
                                    String hour, String minutes, String seconds) {
        return build("*", week, month, day, hour, minutes, seconds);
    }

    /**
     * 构建按月日触发的区间 Cron 表达式。
     *
     * @param month 月份段
     * @param day 日期段
     * @param hour 小时段
     * @param minutes 分钟段
     * @param seconds 秒段
     * @return Cron 表达式
     */
    static String buildRangeForMonthDay(String month, String day, String hour, String minutes, String seconds) {
        return build("?", month, day, hour, minutes, seconds);
    }
}
