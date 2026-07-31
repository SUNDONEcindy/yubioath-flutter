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

import com.yubico.authenticator.otp.scancodes.bepoScancodes
import com.yubico.authenticator.otp.scancodes.deScancodes
import com.yubico.authenticator.otp.scancodes.frScancodes
import com.yubico.authenticator.otp.scancodes.itScancodes
import com.yubico.authenticator.otp.scancodes.modhexScancodes
import com.yubico.authenticator.otp.scancodes.normanScancodes
import com.yubico.authenticator.otp.scancodes.ukScancodes
import com.yubico.authenticator.otp.scancodes.usScancodes
import java.security.SecureRandom

/**
 * Character to HID scancode maps for static password programming.
 *
 * This is the Kotlin counterpart of python-yubikey-manager's `ykman.scancodes` package and
 * `ykman.otp.generate_static_pw`; the layout names and character sets must stay identical to
 * the desktop implementation, because the shared Flutter UI treats them as opaque identifiers
 * coming from [layouts].
 *
 * Note this is unrelated to `com.yubico.authenticator.ndef.KeyboardLayout`, which maps in the
 * opposite direction (scancode to character) for decoding NDEF payloads.
 */
object KeyboardLayouts {
    /** Characters never used when generating a random password (`ykman.otp`). */
    private val PW_CHAR_BLOCKLIST = setOf('\t', '\n', ' ')

    private val layouts: Map<String, Map<Char, Int>> =
        linkedMapOf(
            "MODHEX" to modhexScancodes,
            "US" to usScancodes,
            "UK" to ukScancodes,
            "DE" to deScancodes,
            "FR" to frScancodes,
            "IT" to itScancodes,
            "BEPO" to bepoScancodes,
            "NORMAN" to normanScancodes
        )

    private val secureRandom = SecureRandom()

    private fun layout(name: String): Map<Char, Int> =
        layouts[name] ?: throw IllegalArgumentException("Unsupported keyboard layout: $name")

    /** The characters each layout can type, keyed by layout name. */
    fun layouts(): Map<String, List<String>> = layouts.mapValues { (_, map) ->
        map.keys.map(Char::toString)
    }

    /**
     * Encodes [text] as HID scancodes using the layout called [layoutName].
     *
     * @throws IllegalArgumentException if the layout is unknown or [text] contains a character
     *     the layout cannot type.
     */
    fun encode(text: String, layoutName: String): ByteArray {
        val scancodes = layout(layoutName)
        return ByteArray(text.length) { index ->
            val char = text[index]
            val scancode =
                scancodes[char]
                    ?: throw IllegalArgumentException("Unsupported character: $char")
            scancode.toByte()
        }
    }

    /** Generates a random password of [length] characters typeable with layout [layoutName]. */
    fun generate(length: Int, layoutName: String): String {
        require(length >= 0) { "Password length cannot be negative" }
        val alphabet = layout(layoutName).keys.filterNot { it in PW_CHAR_BLOCKLIST }
        return String(CharArray(length) { alphabet[secureRandom.nextInt(alphabet.size)] })
    }
}
