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

import com.yubico.yubikit.core.otp.Modhex
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Off-device helpers backing the OTP method channel.
 *
 * Ported from `helper/helper/yubiotp.py` and `ykman/otp.py` so that the shared Flutter UI
 * behaves identically on Android and desktop.
 */
object OtpUtils {
    /**
     * Encodes a YubiKey serial number as the modhex public ID Yubico's upload form expects,
     * i.e. the prefix `ff 00` followed by the big-endian serial (`yubiotp.py:serial_modhex`).
     */
    fun modhexEncodeSerial(serial: Int): String {
        val encoded =
            byteArrayOf(
                0xFF.toByte(),
                0x00,
                (serial ushr 24).toByte(),
                (serial ushr 16).toByte(),
                (serial ushr 8).toByte(),
                serial.toByte()
            )
        return Modhex.encode(encoded)
    }

    /**
     * Produces a CSV line in the "Yubico" upload format (`ykman.otp.format_csv`):
     * `serial,publicId,privateId,key,accessCode,timestamp,` — note the empty access code field
     * and the trailing comma, both of which the upload form requires.
     *
     * [publicId] is already modhex encoded; [privateId] and [key] are hex. All three are
     * round-tripped through their decoders, which both normalises the casing and rejects
     * malformed input, exactly as the desktop helper does.
     */
    fun formatYubiOtpCsv(
        serial: Int,
        publicId: String,
        privateId: String,
        key: String,
        timestamp: Date = Date()
    ): String {
        // Python's datetime.isoformat(timespec="seconds") on a naive local timestamp.
        val isoTimestamp =
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).format(timestamp)
        return listOf(
            serial.toString(),
            Modhex.encode(Modhex.decode(publicId)),
            privateId.hexToByteArray().toHexString(),
            key.hexToByteArray().toHexString(),
            // access code, never set by the shared UI when exporting
            "",
            isoTimestamp,
            // trailing comma
            ""
        ).joinToString(",")
    }
}
