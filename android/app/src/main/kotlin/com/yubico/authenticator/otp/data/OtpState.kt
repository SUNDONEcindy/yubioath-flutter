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

import com.yubico.authenticator.JsonSerializable
import com.yubico.authenticator.jsonSerializer
import com.yubico.yubikit.yubiotp.Slot
import com.yubico.yubikit.yubiotp.YubiOtpSession
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OtpState(
    @SerialName("slot1_configured")
    val slot1Configured: Boolean,
    @SerialName("slot2_configured")
    val slot2Configured: Boolean
) : JsonSerializable {
    override fun toJson(): String = jsonSerializer.encodeToString(this)

    companion object {
        /**
         * Reads the configuration state of both slots.
         *
         * YubiKeys older than 2.1 cannot report whether a slot holds a configuration. As the
         * desktop helper does, assume both slots are configured in that case so that the UI
         * offers the (destructive) overwrite confirmation rather than silently reprogramming.
         */
        fun from(session: YubiOtpSession): OtpState =
            if (session.supports(YubiOtpSession.FEATURE_CHECK_CONFIGURED)) {
                val state = session.configurationState
                OtpState(state.isConfigured(Slot.ONE), state.isConfigured(Slot.TWO))
            } else {
                OtpState(slot1Configured = true, slot2Configured = true)
            }
    }
}
