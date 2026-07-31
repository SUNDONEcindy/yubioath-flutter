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

package com.yubico.authenticator.otp.scancodes

/**
 * Scancode map for the MODHEX keyboard layout.
 *
 * Ported verbatim from python-yubikey-manager `ykman/scancodes/modhex.py`.
 * Insertion order is significant: it defines the character set reported to the UI
 * and the alphabet used when generating a random static password.
 */
internal val modhexScancodes: Map<Char, Int> =
    linkedMapOf(
        'b' to 0x05,
        'c' to 0x06,
        'd' to 0x07,
        'e' to 0x08,
        'f' to 0x09,
        'g' to 0x0A,
        'h' to 0x0B,
        'i' to 0x0C,
        'j' to 0x0D,
        'k' to 0x0E,
        'l' to 0x0F,
        'n' to 0x11,
        'r' to 0x15,
        't' to 0x17,
        'u' to 0x18,
        'v' to 0x19,
        'B' to 0x85,
        'C' to 0x86,
        'D' to 0x87,
        'E' to 0x88,
        'F' to 0x89,
        'G' to 0x8A,
        'H' to 0x8B,
        'I' to 0x8C,
        'J' to 0x8D,
        'K' to 0x8E,
        'L' to 0x8F,
        'N' to 0x91,
        'R' to 0x95,
        'T' to 0x97,
        'U' to 0x98,
        'V' to 0x99
    )
