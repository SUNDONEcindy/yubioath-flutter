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
import com.yubico.authenticator.otp.scancodes.dechScancodes
import com.yubico.authenticator.otp.scancodes.frScancodes
import com.yubico.authenticator.otp.scancodes.itScancodes
import com.yubico.authenticator.otp.scancodes.modhexScancodes
import com.yubico.authenticator.otp.scancodes.normanScancodes
import com.yubico.authenticator.otp.scancodes.ukScancodes
import com.yubico.authenticator.otp.scancodes.usScancodes
import java.security.SecureRandom

/**
 * Character to HID scancode maps, used both to program a static password slot and to decode an
 * NDEF payload typed by the key.
 *
 * This is the Kotlin counterpart of python-yubikey-manager's `ykman.scancodes` package and
 * `ykman.otp.generate_static_pw`; the layout names and character sets must stay identical to
 * the desktop implementation, because the shared Flutter UI treats them as opaque identifiers
 * coming from [layouts].
 *
 * Decoding runs the same tables backwards, so the two directions cannot drift apart.
 */
object KeyboardLayouts {
    /** Characters never used when generating a random password (`ykman.otp`). */
    private val PW_CHAR_BLOCKLIST = setOf('\t', '\n', ' ')

    /** Layouts a slot can be programmed with. Exactly the `ykman.scancodes` set, in its order. */
    private val otpLayouts: Map<String, Map<Char, Int>> =
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

    /**
     * Layouts an NDEF payload can be decoded with: [otpLayouts] plus Swiss German, which ykman
     * does not know and which is therefore decode-only. See [dechScancodes].
     */
    private val ndefLayouts: Map<String, Map<Char, Int>> =
        otpLayouts + ("DE-CH" to dechScancodes)

    /**
     * Scancode to character, derived from [ndefLayouts].
     *
     * The first character wins where a layout types one scancode from two keys - ykman's `de`
     * maps both `?` and `` ` `` to `0x2D | SHIFT`, and `?` is the one a German keyboard produces.
     */
    private val decodeMaps: Map<String, Map<Int, Char>> by lazy {
        ndefLayouts.mapValues { (_, scancodes) ->
            val decoded = LinkedHashMap<Int, Char>(scancodes.size)
            scancodes.forEach { (char, scancode) ->
                if (scancode !in decoded) decoded[scancode] = char
            }
            decoded
        }
    }

    private val secureRandom = SecureRandom()

    private fun layout(name: String): Map<Char, Int> =
        otpLayouts[name] ?: throw IllegalArgumentException("Unsupported keyboard layout: $name")

    /** The characters each layout can type, keyed by layout name. */
    fun layouts(): Map<String, List<String>> = otpLayouts.mapValues { (_, map) ->
        map.keys.map(Char::toString)
    }

    /** The layout names an NDEF payload can be decoded with, in display order. */
    fun ndefLayoutNames(): List<String> = ndefLayouts.keys.toList()

    /**
     * Decodes HID [scancodes] typed with the layout called [layoutName].
     *
     * Scancodes the layout has no character for are dropped rather than rendered, so a payload
     * that was typed with a different layout degrades instead of throwing.
     *
     * @throws IllegalArgumentException if the layout is unknown.
     */
    fun decode(scancodes: ByteArray, layoutName: String): String {
        val characters =
            decodeMaps[layoutName]
                ?: throw IllegalArgumentException("Unsupported keyboard layout: $layoutName")
        return buildString(scancodes.size) {
            scancodes.forEach { scancode ->
                characters[scancode.toInt() and 0xFF]?.let(::append)
            }
        }
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
