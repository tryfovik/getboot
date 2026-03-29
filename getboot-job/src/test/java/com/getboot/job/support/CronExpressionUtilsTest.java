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

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class CronExpressionUtilsTest {

    @Test
    void shouldReturnNullWhenDateIsNull() {
        assertNull(CronExpressionUtils.fromDate(null));
    }

    @Test
    void shouldBuildCronExpressionFromDate() {
        Date date = Date.from(Instant.parse("2026-03-29T12:34:56Z"));
        String expected = DateTimeFormatter.ofPattern("ss mm HH dd MM ? yyyy")
                .format(date.toInstant().atZone(ZoneId.systemDefault()));

        assertEquals(expected, CronExpressionUtils.fromDate(date));
    }

    @Test
    void shouldBuildCronExpressionFromSegments() {
        assertEquals("30 15 10 5 8 MON 2026",
                CronExpressionUtils.build("2026", "MON", "8", "5", "10", "15", "30"));
        assertEquals("30 15 10 5 8 ? *",
                CronExpressionUtils.buildForMonthDay("8", "5", "10", "15", "30"));
        assertEquals("30 15 10 5 8 ? *",
                CronExpressionUtils.buildRangeForMonthDay("8", "5", "10", "15", "30"));
    }
}
