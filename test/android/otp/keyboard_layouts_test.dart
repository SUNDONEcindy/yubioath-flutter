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

import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:yubico_authenticator/android/state.dart';

void main() {
  // The canonical NDEF layout list. This is the Dart mirror of
  // `KeyboardLayouts.ndefLayoutNames()` on the Kotlin side, which `KeyboardLayoutsTest.kt` pins
  // via its own `EXPECTED_NDEF_LAYOUTS`. Both tests assert against this identical literal, so
  // adding, removing, or reordering a layout on either side of the channel fails a test until the
  // other side is updated to match — that is what keeps the two hand-written lists from drifting.
  const expectedNdefLayouts = [
    'MODHEX',
    'US',
    'UK',
    'DE',
    'FR',
    'IT',
    'BEPO',
    'NORMAN',
    'DE-CH',
  ];

  test('androidNfcSupportedKbdLayoutsProvider mirrors ndefLayoutNames()', () {
    final container = ProviderContainer();
    addTearDown(container.dispose);

    expect(
      container.read(androidNfcSupportedKbdLayoutsProvider),
      expectedNdefLayouts,
    );
  });
}
