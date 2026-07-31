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

import com.yubico.authenticator.otp.KeyboardLayouts
import com.yubico.yubikit.core.otp.Modhex
import com.yubico.yubikit.oath.Base32
import com.yubico.yubikit.yubiotp.HmacSha1SlotConfiguration
import com.yubico.yubikit.yubiotp.HotpSlotConfiguration
import com.yubico.yubikit.yubiotp.SlotConfiguration
import com.yubico.yubikit.yubiotp.StaticPasswordSlotConfiguration
import com.yubico.yubikit.yubiotp.YubiOtpSlotConfiguration

/**
 * Builds a yubikit [SlotConfiguration] from the JSON shape of the shared Flutter
 * `SlotConfiguration` union (`lib/otp/models.dart`).
 *
 * This is the Kotlin counterpart of `SlotNode._get_config` / `SlotNode._apply_options` in
 * `helper/helper/yubiotp.py`; only the options the shared UI actually sends are supported.
 */
object SlotConfig {
    private const val KEY_TYPE = "type"
    private const val KEY_OPTIONS = "options"

    fun fromMap(configuration: Map<*, *>): SlotConfiguration {
        val options = configuration[KEY_OPTIONS] as? Map<*, *> ?: emptyMap<String, Any?>()
        val digits8 = options["digits8"] as? Boolean
        val requireTouch = options["require_touch"] as? Boolean
        val appendCr = options["append_cr"] as? Boolean

        return when (val type = configuration[KEY_TYPE] as? String) {
            "hotp" -> {
                val config = HotpSlotConfiguration(parseBase32Key(configuration.string("key")))
                // python-yubikit sets OATH_FIXED_MODHEX2 in its HotpSlotConfiguration constructor
                // but yubikit-android does not, so set it explicitly to write the same
                // configuration as the desktop app. The token id stays empty, as the shared UI
                // never offers one.
                config.tokenId(ByteArray(0), false, true)
                digits8?.let { config.digits8(it) }
                appendCr?.let { config.appendCr(it) }
                config
            }

            "hmac_sha1" -> {
                val config =
                    HmacSha1SlotConfiguration(configuration.string("key").hexToByteArray())
                requireTouch?.let { config.requireTouch(it) }
                config
            }

            "static_password" -> {
                val config =
                    StaticPasswordSlotConfiguration(
                        KeyboardLayouts.encode(
                            configuration.string("password"),
                            configuration.string("keyboard_layout")
                        )
                    )
                appendCr?.let { config.appendCr(it) }
                config
            }

            "yubiotp" -> {
                val config =
                    YubiOtpSlotConfiguration(
                        Modhex.decode(configuration.string("public_id")),
                        configuration.string("private_id").hexToByteArray(),
                        configuration.string("key").hexToByteArray()
                    )
                appendCr?.let { config.appendCr(it) }
                config
            }

            else -> throw IllegalArgumentException("Unsupported configuration type provided: $type")
        }
    }

    /** Base32 decoding accepting unpadded input, matching `yubikit.oath.parse_b32_key`. */
    private fun parseBase32Key(key: String): ByteArray {
        val normalized = key.uppercase().replace(" ", "")
        val padding = (-normalized.length).mod(8)
        return Base32.decode(normalized + "=".repeat(padding))
    }

    private fun Map<*, *>.string(key: String): String =
        this[key] as? String ?: throw IllegalArgumentException("Missing configuration value: $key")
}
