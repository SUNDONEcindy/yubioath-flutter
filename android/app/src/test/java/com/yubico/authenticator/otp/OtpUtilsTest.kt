/*
 * Copyright (C) 2026 Yubico.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.yubico.authenticator.otp

import java.util.Calendar
import java.util.Date
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

/**
 * Expected values were produced with python-yubikey-manager (`ykman.otp.format_csv` and the
 * `serial_modhex` RPC in `helper/helper/yubiotp.py`), which is what the desktop app uses.
 */
class OtpUtilsTest {
    @Test
    fun `modhexEncodeSerial matches the desktop helper`() {
        assertEquals("vvcccccccccb", OtpUtils.modhexEncodeSerial(1))
        assertEquals("vvccccnrhbfu", OtpUtils.modhexEncodeSerial(12345678))
        assertEquals("vvcccbebdtcc", OtpUtils.modhexEncodeSerial(20000000))
    }

    @Test
    fun `modhexEncodeSerial always prefixes ff00`() {
        // The public ID is 6 bytes, so the encoding is always 12 modhex characters.
        listOf(0, 1, 7_100_000, Int.MAX_VALUE).forEach { serial ->
            val encoded = OtpUtils.modhexEncodeSerial(serial)
            assertEquals("Wrong length for serial $serial", 12, encoded.length)
            assertEquals("Missing ff00 prefix for serial $serial", "vvcc", encoded.take(4))
        }
    }

    @Test
    fun `formatYubiOtpCsv matches ykman format_csv`() {
        assertEquals(
            "12345678,cccccccccccb,010203040506," +
                "000102030405060708090a0b0c0d0e0f,,2026-07-31T09:05:03,",
            OtpUtils.formatYubiOtpCsv(
                serial = 12345678,
                publicId = "cccccccccccb",
                privateId = "010203040506",
                key = "000102030405060708090a0b0c0d0e0f",
                timestamp = dateOf(2026, Calendar.JULY, 31, 9, 5, 3)
            )
        )
    }

    @Test
    fun `formatYubiOtpCsv normalises hex casing`() {
        val csv =
            OtpUtils.formatYubiOtpCsv(
                serial = 1,
                publicId = "cccccccccccb",
                privateId = "0A0B0C0D0E0F",
                key = "000102030405060708090A0B0C0D0E0F",
                timestamp = dateOf(2026, Calendar.JANUARY, 1, 0, 0, 0)
            )
        assertEquals(
            "1,cccccccccccb,0a0b0c0d0e0f,000102030405060708090a0b0c0d0e0f,,2026-01-01T00:00:00,",
            csv
        )
    }

    @Test
    fun `formatYubiOtpCsv leaves the access code empty and adds a trailing comma`() {
        val fields =
            OtpUtils
                .formatYubiOtpCsv(
                    serial = 1,
                    publicId = "cccccccccccb",
                    privateId = "010203040506",
                    key = "000102030405060708090a0b0c0d0e0f",
                    timestamp = dateOf(2026, Calendar.JULY, 31, 9, 5, 3)
                ).split(",")
        assertEquals(7, fields.size)
        assertEquals("", fields[4])
        assertEquals("", fields[6])
    }

    @Test
    fun `formatYubiOtpCsv rejects malformed input`() {
        assertThrows(IllegalArgumentException::class.java) {
            // 'a' is not a modhex character.
            OtpUtils.formatYubiOtpCsv(1, "aaaaaaaaaaaa", "010203040506", "00")
        }
        assertThrows(IllegalArgumentException::class.java) {
            // 'z' is not a hex character.
            OtpUtils.formatYubiOtpCsv(1, "cccccccccccb", "zzzzzzzzzzzz", "00")
        }
    }

    private fun dateOf(year: Int, month: Int, day: Int, hour: Int, minute: Int, second: Int): Date =
        Calendar
            .getInstance()
            .apply {
                clear()
                set(year, month, day, hour, minute, second)
            }.time
}
