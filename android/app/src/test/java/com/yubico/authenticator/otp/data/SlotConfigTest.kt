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

package com.yubico.authenticator.otp.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertThrows
import org.junit.Test

/**
 * The input maps are the JSON shapes the shared Flutter UI sends over the method channel (the
 * `SlotConfiguration` freezed union in `lib/otp/models.dart`).
 *
 * The expected configuration blobs were produced with python-yubikit — the exact structs the
 * desktop helper writes to the key — so a mismatch means Android would program a slot differently
 * from desktop for the same dialog input. The trailing two bytes are a CRC over the whole struct,
 * so these assertions cover every field including the flags.
 */
class SlotConfigTest {
    // ykman: base64.b32encode(bytes(range(20)))
    private val secretB32 = "AAAQEAYEAUDAOCAJBIFQYDIOB4IBCEQT"

    @Test
    fun `hotp uses the base32 secret and defaults to append cr`() {
        assertEquals(
            "00000000000000000000000000000000" + // fixed
                "101112130000" + // uid: secret[16..20]
                "000102030405060708090a0b0c0d0e0f" + // key: secret[0..16]
                "000000000000" + // access code
                "00" + // fixed size
                "34" + // ext flags: SERIAL_API_VISIBLE | SERIAL_USB_VISIBLE | ALLOW_UPDATE
                "60" + // ticket flags: OATH_HOTP | APPEND_CR
                "40" + // config flags: OATH_FIXED_MODHEX2
                "0000" + // RFU
                "cd1a", // CRC
            config(mapOf("type" to "hotp", "key" to secretB32))
        )
    }

    @Test
    fun `hotp accepts an unpadded base32 secret`() {
        // 16 bytes is not a multiple of 5, so ykman's parse_b32_key has to re-add the '=' padding.
        assertEquals(
            "00000000000000000000000000000000" +
                "000000000000" +
                "000102030405060708090a0b0c0d0e0f" +
                "000000000000" +
                "0034604000000d9d",
            config(mapOf("type" to "hotp", "key" to "AAAQEAYEAUDAOCAJBIFQYDIOB4"))
        )
        assertEquals(
            config(mapOf("type" to "hotp", "key" to "AAAQEAYEAUDAOCAJBIFQYDIOB4======")),
            config(mapOf("type" to "hotp", "key" to "AAAQEAYEAUDAOCAJBIFQYDIOB4"))
        )
    }

    @Test
    fun `hotp options toggle the config flags`() {
        fun hotp(vararg options: Pair<String, Any>) =
            mapOf("type" to "hotp", "key" to secretB32, "options" to mapOf(*options))
        // digits8 sets OATH_HOTP8, which lives in the config flags byte (offset 47).
        assertEquals("42", byteAt(47, hotp("digits8" to true)))
        assertEquals("40", byteAt(47, hotp("digits8" to false)))
        // appendCr sets APPEND_CR in the ticket flags byte (offset 46), on top of OATH_HOTP.
        assertEquals("60", byteAt(46, hotp("append_cr" to true)))
        assertEquals("40", byteAt(46, hotp("append_cr" to false)))
    }

    @Test
    fun `chalresp uses the hex key and honours require touch`() {
        val base =
            "00000000000000000000000000000000" +
                "101112130000" +
                "000102030405060708090a0b0c0d0e0f" +
                "000000000000" +
                "00" + // fixed size
                "24" + // ext flags
                "40" + // ticket flags: CHAL_RESP
                "26" // config flags: CHAL_HMAC | HMAC_LT64 | CHAL_RESP(bit)
        assertEquals(
            base + "00004af2",
            config(
                mapOf(
                    "type" to "hmac_sha1",
                    "key" to "000102030405060708090a0b0c0d0e0f10111213"
                )
            )
        )
        assertEquals(
            // CHAL_BTN_TRIG additionally set in the config flags byte.
            base.dropLast(2) + "2e" + "00008834",
            config(
                mapOf(
                    "type" to "hmac_sha1",
                    "key" to "000102030405060708090a0b0c0d0e0f10111213",
                    "options" to mapOf("require_touch" to true)
                )
            )
        )
    }

    @Test
    fun `static password is stored as scancodes for the chosen layout`() {
        assertEquals(
            // "hello" typed on a US keyboard, see KeyboardLayoutsTest.
            "0b080f0f12" + "0000000000000000000000" + // fixed: scancodes, zero padded
                "000000000000" + // uid
                "00000000000000000000000000000000" + // key
                "000000000000" + // access code
                "10" + // fixed size: the full 16 byte scancode area
                "34" + // ext flags
                "20" + // ticket flags: APPEND_CR
                "02" + // config flags: STATIC_TICKET
                "00001e39",
            config(
                mapOf(
                    "type" to "static_password",
                    "password" to "hello",
                    "keyboard_layout" to "US"
                )
            )
        )
        // Same password on a different layout produces different scancodes.
        assertNotEquals(
            config(
                mapOf(
                    "type" to "static_password",
                    "password" to "qwerty",
                    "keyboard_layout" to "US"
                )
            ),
            config(
                mapOf(
                    "type" to "static_password",
                    "password" to "qwerty",
                    "keyboard_layout" to "BEPO"
                )
            )
        )
    }

    @Test
    fun `yubiotp maps public id to fixed private id to uid and key to key`() {
        val configuration =
            mapOf(
                "type" to "yubiotp",
                "public_id" to "cccccccccccb",
                "private_id" to "010203040506",
                "key" to "000102030405060708090a0b0c0d0e0f"
            )
        assertEquals(
            "000000000001" + "00000000000000000000" + // fixed: modhex public id, zero padded
                "010203040506" + // uid: private id
                "000102030405060708090a0b0c0d0e0f" + // key
                "000000000000" + // access code
                "06" + // fixed size: the 6 byte public id
                "34" + // ext flags
                "20" + // ticket flags: APPEND_CR
                "00" + // config flags
                "0000e842",
            config(configuration)
        )
        // Dropping the carriage return only clears APPEND_CR.
        assertEquals("00", byteAt(46, configuration + ("options" to mapOf("append_cr" to false))))
    }

    @Test
    fun `options are optional and unknown ones are ignored`() {
        val plain = config(mapOf("type" to "hotp", "key" to secretB32))
        assertEquals(plain, config(mapOf("type" to "hotp", "key" to secretB32, "options" to null)))
        assertEquals(
            plain,
            config(
                mapOf(
                    "type" to "hotp",
                    "key" to secretB32,
                    "options" to mapOf("no_such_option" to true)
                )
            )
        )
    }

    @Test
    fun `an unsupported type is rejected`() {
        assertThrows(IllegalArgumentException::class.java) {
            SlotConfig.fromMap(mapOf("type" to "totp", "key" to secretB32))
        }
        assertThrows(IllegalArgumentException::class.java) {
            SlotConfig.fromMap(mapOf("key" to secretB32))
        }
    }

    @Test
    fun `a missing required value is rejected`() {
        assertThrows(IllegalArgumentException::class.java) {
            SlotConfig.fromMap(mapOf("type" to "hotp"))
        }
        assertThrows(IllegalArgumentException::class.java) {
            SlotConfig.fromMap(mapOf("type" to "static_password", "password" to "hello"))
        }
        assertThrows(IllegalArgumentException::class.java) {
            SlotConfig.fromMap(
                mapOf("type" to "yubiotp", "public_id" to "cccccccccccb", "key" to "00")
            )
        }
    }

    private fun config(configuration: Map<*, *>): String =
        SlotConfig.fromMap(configuration).getConfig(null).toHexString()

    /** The [index]th byte of the built configuration, as hex. */
    private fun byteAt(index: Int, configuration: Map<*, *>): String =
        config(configuration).substring(index * 2, index * 2 + 2)
}
