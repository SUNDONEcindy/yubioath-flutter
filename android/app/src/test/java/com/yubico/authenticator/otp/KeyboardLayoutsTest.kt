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

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The layout tables are ports of the python-yubikey-manager `ykman.scancodes` modules.
 *
 * The expected character sets below were dumped from ykman, so a failure here means the Android
 * tables have drifted from the desktop ones — which would make the two platforms program
 * different static passwords for the same input.
 */
class KeyboardLayoutsTest {
    @Test
    fun `layouts are reported in the ykman order`() {
        assertEquals(EXPECTED_CHARACTERS.keys.toList(), KeyboardLayouts.layouts().keys.toList())
    }

    @Test
    fun `layout character sets match ykman`() {
        val layouts = KeyboardLayouts.layouts()
        EXPECTED_CHARACTERS.forEach { (name, expected) ->
            assertEquals(
                "Character set drift in layout $name",
                expected.map(Char::toString),
                layouts[name]
            )
        }
    }

    @Test
    fun `encode matches ykman scancodes`() {
        assertEquals("0b080f0f12", KeyboardLayouts.encode("hello", "US").toHexString())
        assertEquals("8b080f0f129e", KeyboardLayouts.encode("Hello!", "US").toHexString())
        assertEquals("050607080999", KeyboardLayouts.encode("bcdefV", "MODHEX").toHexString())
        // Characters which only exist in a non-US layout.
        assertEquals("34b32d", KeyboardLayouts.encode("\u00e4\u00d6\u00df", "DE").toHexString())
    }

    @Test
    fun `encode of empty text is empty`() {
        assertEquals(0, KeyboardLayouts.encode("", "US").size)
    }

    @Test
    fun `encode rejects characters the layout cannot type`() {
        // Lower case hex digits outside the modhex alphabet.
        assertThrows(IllegalArgumentException::class.java) {
            KeyboardLayouts.encode("abcdef", "MODHEX")
        }
        // No layout can type an emoji.
        assertThrows(IllegalArgumentException::class.java) {
            KeyboardLayouts.encode("\ud83d\udd11", "US")
        }
    }

    @Test
    fun `unknown layout is rejected`() {
        assertThrows(IllegalArgumentException::class.java) {
            KeyboardLayouts.encode("hello", "DVORAK")
        }
        assertThrows(IllegalArgumentException::class.java) { KeyboardLayouts.generate(8, "us") }
    }

    @Test
    fun `generate produces a password of the requested length`() {
        assertEquals(0, KeyboardLayouts.generate(0, "US").length)
        assertEquals(38, KeyboardLayouts.generate(38, "US").length)
        assertThrows(IllegalArgumentException::class.java) { KeyboardLayouts.generate(-1, "US") }
    }

    @Test
    fun `generated passwords only use typeable non blocklisted characters`() {
        EXPECTED_CHARACTERS.keys.forEach { name ->
            val alphabet = KeyboardLayouts.layouts().getValue(name).map { it[0] }.toSet()
            val generated = KeyboardLayouts.generate(1000, name)
            generated.forEach { char ->
                assertTrue("$name generated untypeable '$char'", char in alphabet)
                assertTrue(
                    "$name generated blocklisted '${char.code}'",
                    char !in listOf('\t', '\n', ' ')
                )
            }
        }
    }

    @Test
    fun `generated passwords can be encoded with their own layout`() {
        EXPECTED_CHARACTERS.keys.forEach { name ->
            val generated = KeyboardLayouts.generate(64, name)
            assertEquals(64, KeyboardLayouts.encode(generated, name).size)
        }
    }

    @Test
    fun `ndef layouts are the otp layouts plus Swiss German`() {
        assertEquals(EXPECTED_NDEF_LAYOUTS, KeyboardLayouts.ndefLayoutNames())
        // DE-CH is decode-only: ykman has no such layout, so a slot programmed with it could not
        // be reproduced on the desktop.
        assertTrue("DE-CH" !in KeyboardLayouts.layouts())
    }

    @Test
    fun `decode inverts encode for every typeable character`() {
        EXPECTED_CHARACTERS.keys.forEach { name ->
            KeyboardLayouts.layouts().getValue(name).forEach { character ->
                val encoded = KeyboardLayouts.encode(character, name)
                val decoded = KeyboardLayouts.decode(encoded, name)
                // Not `decoded == character`: ykman's `de` types both `?` and `` ` `` from
                // 0x2D | SHIFT, so decoding is only unique up to the scancode.
                assertEquals(
                    "$name does not round-trip '$character'",
                    encoded.toHexString(),
                    KeyboardLayouts.encode(decoded, name).toHexString()
                )
            }
        }
    }

    @Test
    fun `decode matches ykman scancodes`() {
        assertEquals("hello", KeyboardLayouts.decode(scancodes(0x0B, 0x08, 0x0F, 0x0F, 0x12), "US"))
        assertEquals(
            "Hello!",
            KeyboardLayouts.decode(scancodes(0x8B, 0x08, 0x0F, 0x0F, 0x12, 0x9E), "US")
        )
        assertEquals(
            "bcdefV",
            KeyboardLayouts.decode(scancodes(0x05, 0x06, 0x07, 0x08, 0x09, 0x99), "MODHEX")
        )
    }

    /**
     * The hand-written table this replaced decoded these from the US layout by mistake, so a
     * German static password came back with the wrong characters.
     */
    @Test
    fun `decode fixes the German characters the old ndef table got wrong`() {
        assertEquals("ä", KeyboardLayouts.decode(scancodes(0x34), "DE")) // was '
        assertEquals("'", KeyboardLayouts.decode(scancodes(0xB2), "DE")) // was >
        // The ISO key next to the left shift; the old table's arrays stopped at 0x3F.
        assertEquals("<", KeyboardLayouts.decode(scancodes(0x64), "DE"))
        assertEquals(">", KeyboardLayouts.decode(scancodes(0xE4), "DE"))
    }

    @Test
    fun `decode prefers the question mark over the backtick on DE`() {
        // ykman's `de` maps both to 0x2D | SHIFT; `?` is what the key actually types.
        assertEquals("?", KeyboardLayouts.decode(scancodes(0xAD), "DE"))
    }

    @Test
    fun `decode handles Swiss German`() {
        assertEquals(
            "äÄöü§è",
            KeyboardLayouts.decode(scancodes(0x34, 0xB4, 0x33, 0x2F, 0x35, 0xAF), "DE-CH")
        )
    }

    @Test
    fun `decode drops scancodes the layout cannot type`() {
        // 0xB5 is unmapped in ykman's `de`; the old table wrongly rendered it as '.
        assertEquals("ab", KeyboardLayouts.decode(scancodes(0x04, 0xB5, 0x05), "DE"))
        assertEquals("", KeyboardLayouts.decode(scancodes(0x04), "MODHEX"))
    }

    @Test
    fun `decode of empty input is empty`() {
        assertEquals("", KeyboardLayouts.decode(ByteArray(0), "US"))
    }

    @Test
    fun `decode rejects an unknown layout`() {
        assertThrows(IllegalArgumentException::class.java) {
            KeyboardLayouts.decode(scancodes(0x04), "DVORAK")
        }
        // Programming layouts are a subset; DE-CH must not become encodable by accident.
        assertThrows(IllegalArgumentException::class.java) {
            KeyboardLayouts.encode("a", "DE-CH")
        }
    }

    private fun scancodes(vararg codes: Int) = ByteArray(codes.size) { codes[it].toByte() }

    companion object {
        /**
         * [KeyboardLayouts.ndefLayoutNames], mirrored by `androidNfcSupportedKbdLayoutsProvider`
         * in `lib/android/state.dart`. Update both together.
         */
        private val EXPECTED_NDEF_LAYOUTS =
            listOf("MODHEX", "US", "UK", "DE", "FR", "IT", "BEPO", "NORMAN", "DE-CH")

        /** Character sets of `ykman.scancodes.KEYBOARD_LAYOUT`, in insertion order. */
        private val EXPECTED_CHARACTERS: Map<String, String> =
            linkedMapOf(
                "MODHEX" to
                    "bcdefghijklnrtuvBCDEFGHIJKLNRTUV",
                "US" to
                    "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ" +
                    "0123456789\t\n!\"#\$%&'`()*+,-./:;<=>?@[\\]^_{}|~ ",
                "UK" to
                    "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ" +
                    "0123456789\t\n!@\u00a3\$%&'`()*+,-./:;<=>?\"[#]^_{}~" +
                    "\u00ac ",
                "DE" to
                    "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ" +
                    "0123456789\t\n!\"#\$%&'()*+,-./:;<=>?^_ `\u00a7" +
                    "\u00b4\u00c4\u00d6\u00dc\u00df\u00e4\u00f6\u00fc",
                "FR" to
                    "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ" +
                    "0123456789\t\n !\"\$%&'()*+,-./:;<=_\u007f\u00a3" +
                    "\u00a7\u00b0\u00b2\u00b5\u00e0\u00e7\u00e8\u00e9" +
                    "\u00f9",
                "IT" to
                    "\t\n !\"#\$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLM" +
                    "NOPQRSTUVWXYZ\\^_`abcdefghijklmnopqrstuvwxyz|\u00a3" +
                    "\u00a7\u00b0\u00e7\u00e8\u00e9\u00e0\u00ec\u00f2" +
                    "\u00f9",
                "BEPO" to
                    "\t\n !\"#\$%'()*+,-./0123456789:;=?@ABCDEFGHIJKLMNOP" +
                    "QRSTUVWXYZ`abcdefghijklmnopqrstuvwxyz\u00a0\u00ab" +
                    "\u00b0\u00bb\u00c0\u00c7\u00c8\u00c9\u00ca\u00e0" +
                    "\u00e7\u00e8\u00e9\u00ea",
                "NORMAN" to
                    "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ" +
                    "0123456789\t\n!\"#\$%&'`()*+,-./:;<=>?@[\\]^_{}|~ "
            )
    }
}
