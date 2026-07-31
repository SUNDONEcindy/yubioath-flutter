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
 * Scancode map for the BEPO keyboard layout.
 *
 * Ported verbatim from python-yubikey-manager `ykman/scancodes/bepo.py`.
 * Insertion order is significant: it defines the character set reported to the UI
 * and the alphabet used when generating a random static password.
 */
internal val bepoScancodes: Map<Char, Int> =
    linkedMapOf(
        '\t' to 0xAB,
        '\n' to 0xA8,
        ' ' to 0x2C,
        '!' to 0x9C,
        '"' to 0x1E,
        '#' to 0xB5,
        '\$' to 0x35,
        '%' to 0x2E,
        '\'' to 0x11,
        '(' to 0x21,
        ')' to 0x22,
        '*' to 0x27,
        '+' to 0x24,
        ',' to 0x0A,
        '-' to 0x25,
        '.' to 0x19,
        '/' to 0x26,
        '0' to 0xA7,
        '1' to 0x9E,
        '2' to 0x9F,
        '3' to 0xA0,
        '4' to 0xA1,
        '5' to 0xA2,
        '6' to 0xA3,
        '7' to 0xA4,
        '8' to 0xA5,
        '9' to 0xA6,
        ':' to 0x99,
        ';' to 0x8A,
        '=' to 0x2D,
        '?' to 0x91,
        '@' to 0x23,
        'A' to 0x84,
        'B' to 0x94,
        'C' to 0x8B,
        'D' to 0x8C,
        'E' to 0x89,
        'F' to 0xB8,
        'G' to 0xB6,
        'H' to 0xB7,
        'I' to 0x87,
        'J' to 0x93,
        'K' to 0x85,
        'L' to 0x92,
        'M' to 0xB4,
        'N' to 0xB3,
        'O' to 0x95,
        'P' to 0x88,
        'Q' to 0x90,
        'R' to 0x8F,
        'S' to 0x8E,
        'T' to 0x8D,
        'U' to 0x96,
        'V' to 0x98,
        'W' to 0xB0,
        'X' to 0x86,
        'Y' to 0x9B,
        'Z' to 0xAF,
        '`' to 0xAE,
        'a' to 0x04,
        'b' to 0x14,
        'c' to 0x0B,
        'd' to 0x0C,
        'e' to 0x09,
        'f' to 0x38,
        'g' to 0x36,
        'h' to 0x37,
        'i' to 0x07,
        'j' to 0x13,
        'k' to 0x05,
        'l' to 0x12,
        'm' to 0x34,
        'n' to 0x33,
        'o' to 0x15,
        'p' to 0x08,
        'q' to 0x10,
        'r' to 0x0F,
        's' to 0x0E,
        't' to 0x0D,
        'u' to 0x16,
        'v' to 0x18,
        'w' to 0x30,
        'x' to 0x06,
        'y' to 0x1B,
        'z' to 0x2F,
        '\u00a0' to 0xAC,
        '\u00ab' to 0x1F,
        '\u00b0' to 0xAD,
        '\u00bb' to 0x20,
        '\u00c0' to 0x9D,
        '\u00c7' to 0xB1,
        '\u00c8' to 0x97,
        '\u00c9' to 0x9A,
        '\u00ca' to 0xE4,
        '\u00e0' to 0x1D,
        '\u00e7' to 0x31,
        '\u00e8' to 0x17,
        '\u00e9' to 0x1A,
        '\u00ea' to 0x64
    )
