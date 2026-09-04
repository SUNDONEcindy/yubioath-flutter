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
 * Scancode map for the FR keyboard layout.
 *
 * Ported verbatim from python-yubikey-manager `ykman/scancodes/fr.py`.
 * Insertion order is significant: it defines the character set reported to the UI
 * and the alphabet used when generating a random static password.
 */
internal val frScancodes: Map<Char, Int> =
    linkedMapOf(
        'a' to 0x14,
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
        'm' to 0x33,
        'n' to 0x11,
        'o' to 0x12,
        'p' to 0x13,
        'q' to 0x04,
        'r' to 0x15,
        's' to 0x16,
        't' to 0x17,
        'u' to 0x18,
        'v' to 0x19,
        'w' to 0x1D,
        'x' to 0x1B,
        'y' to 0x1C,
        'z' to 0x1A,
        'A' to 0x94,
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
        'M' to 0xB3,
        'N' to 0x91,
        'O' to 0x92,
        'P' to 0x93,
        'Q' to 0x84,
        'R' to 0x95,
        'S' to 0x96,
        'T' to 0x97,
        'U' to 0x98,
        'V' to 0x99,
        'W' to 0x9D,
        'X' to 0x9B,
        'Y' to 0x9C,
        'Z' to 0x9A,
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
        '\t' to 0x2B,
        '\n' to 0x28,
        ' ' to 0x2C,
        '!' to 0x38,
        '"' to 0x20,
        '\$' to 0x30,
        '%' to 0xB4,
        '&' to 0x1E,
        '\'' to 0x21,
        '(' to 0x22,
        ')' to 0x2D,
        '*' to 0x31,
        '+' to 0xAE,
        ',' to 0x10,
        '-' to 0x23,
        '.' to 0xB6,
        '/' to 0xB7,
        ':' to 0x37,
        ';' to 0x36,
        '<' to 0x64,
        '=' to 0x2E,
        '_' to 0x25,
        '\u007f' to 0x2A,
        '\u00a3' to 0xB0,
        '\u00a7' to 0xB8,
        '\u00b0' to 0xAD,
        '\u00b2' to 0x35,
        '\u00b5' to 0xB1,
        '\u00e0' to 0x27,
        '\u00e7' to 0x26,
        '\u00e8' to 0x24,
        '\u00e9' to 0x1F,
        '\u00f9' to 0x34
    )
