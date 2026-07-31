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
import org.junit.Test

class OtpStateTest {
    /**
     * The Flutter side parses this with the generated `_$OtpStateFromJson` in
     * `lib/otp/models.g.dart`, which expects snake case keys.
     */
    @Test
    fun `serializes to the field names the shared models expect`() {
        assertEquals(
            """{"slot1_configured":true,"slot2_configured":false}""",
            OtpState(slot1Configured = true, slot2Configured = false).toJson()
        )
    }
}
