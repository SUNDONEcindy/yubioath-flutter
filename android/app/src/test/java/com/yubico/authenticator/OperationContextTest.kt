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

package com.yubico.authenticator

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Pins the [OperationContext] values sent over the app method channel.
 *
 * They are decoded by `sectionFromAppContext` in `lib/android/app_methods.dart`, which relies on
 * them matching the declaration order of the Dart `Section` enum. This test covers the Kotlin
 * half of that contract only — the alignment between the two enums is asserted from the Dart
 * side, by `test/android/app_methods_test.dart`, which mirrors the subset of the values below
 * that Android reports as a section. Renumbering an entry here fails a test on both sides.
 */
class OperationContextTest {

    @Test
    fun `operation context values are unchanged`() {
        assertEquals(
            EXPECTED_OPERATION_CONTEXTS,
            OperationContext.entries.associate {
                it.name to it.value
            }
        )
    }

    @Test
    fun `getByValue resolves every known value`() {
        EXPECTED_OPERATION_CONTEXTS.forEach { (name, value) ->
            assertEquals(name, OperationContext.getByValue(value).name)
        }
    }

    @Test
    fun `getByValue falls back to Default for unknown values`() {
        listOf(11, 99, -2).forEach { value ->
            assertEquals(OperationContext.Default, OperationContext.getByValue(value))
        }
    }

    companion object {
        /**
         * The `Home`..`Settings` entries are index-aligned with the Dart `Section` enum and are
         * mirrored by hand in `app_methods_test.dart`. `Default`, `OpenPgp`, `HsmAuth` and
         * `Management` have no `Section` and are never reported as one.
         */
        private val EXPECTED_OPERATION_CONTEXTS = mapOf(
            "Default" to -1,
            "Home" to 0,
            "Oath" to 1,
            "FidoU2f" to 2,
            "FidoFingerprints" to 3,
            "FidoPasskeys" to 4,
            "Piv" to 5,
            "YubiOtp" to 6,
            "Settings" to 7,
            "OpenPgp" to 8,
            "HsmAuth" to 9,
            "Management" to 10
        )
    }
}
