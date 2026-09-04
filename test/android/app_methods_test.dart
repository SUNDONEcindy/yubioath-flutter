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

import 'package:flutter_test/flutter_test.dart';
import 'package:yubico_authenticator/android/app_methods.dart';
import 'package:yubico_authenticator/app/models.dart';

void main() {
  // The `OperationContext` values from `MainViewModel.kt` that Android can report as a
  // section. `OperationContextTest.kt` pins the full Kotlin enum against its own
  // `EXPECTED_OPERATION_CONTEXTS`, of which this is the subset that reaches Dart —
  // `Default`, `OpenPgp`, `HsmAuth` and `Management` are never surfaced in the navigation
  // drawer and so have no `Section`. Renumbering any value below therefore fails a test on
  // both sides until they are updated together.
  const expectedOperationContexts = {
    'Home': 0,
    'Oath': 1,
    'FidoU2f': 2,
    'FidoFingerprints': 3,
    'FidoPasskeys': 4,
    'Piv': 5,
    'YubiOtp': 6,
    'Settings': 7,
  };

  // The `Section` that each of those contexts selects. `FidoU2f` is deliberately absent:
  // `Section.securityKey` is not in `supportedSectionsProvider` on Android, so the context
  // is never reported and falls back to home.
  const expectedSections = {
    'Home': Section.home,
    'Oath': Section.accounts,
    'FidoFingerprints': Section.fingerprints,
    'FidoPasskeys': Section.passkeys,
    'Piv': Section.certificates,
    'YubiOtp': Section.slots,
    'Settings': Section.settings,
  };

  group('Section <-> OperationContext', () {
    test('Section declaration order is unchanged', () {
      // Section.index is the wire value sent to Kotlin, so inserting a section anywhere
      // but the end renumbers every later one.
      expect(Section.values.map((s) => s.name), [
        'home',
        'accounts',
        'securityKey',
        'fingerprints',
        'passkeys',
        'certificates',
        'slots',
        'settings',
      ]);
    });

    test('each Section index matches its OperationContext value', () {
      expect(Section.home.index, expectedOperationContexts['Home']);
      expect(Section.accounts.index, expectedOperationContexts['Oath']);
      expect(Section.securityKey.index, expectedOperationContexts['FidoU2f']);
      expect(
        Section.fingerprints.index,
        expectedOperationContexts['FidoFingerprints'],
      );
      expect(Section.passkeys.index, expectedOperationContexts['FidoPasskeys']);
      expect(Section.certificates.index, expectedOperationContexts['Piv']);
      expect(Section.slots.index, expectedOperationContexts['YubiOtp']);
      expect(Section.settings.index, expectedOperationContexts['Settings']);
    });

    test('sectionFromAppContext maps every reported context', () {
      for (final entry in expectedSections.entries) {
        final appContext = expectedOperationContexts[entry.key]!;
        expect(
          sectionFromAppContext(appContext),
          entry.value,
          reason: 'OperationContext.${entry.key} ($appContext)',
        );
      }
    });

    test('sectionFromAppContext round-trips every mapped Section', () {
      // The tie between the two literals above and the switch: if either the enum order
      // or the switch changes on its own, the value no longer maps back to itself.
      for (final section in expectedSections.values) {
        expect(
          sectionFromAppContext(section.index),
          section,
          reason: 'Section.${section.name} (${section.index})',
        );
      }
    });

    test('unreported and unknown contexts fall back to home', () {
      // FidoU2f is a real context that Android never surfaces as a section.
      expect(
        sectionFromAppContext(expectedOperationContexts['FidoU2f']!),
        Section.home,
      );
      // Default(-1), OpenPgp(8), HsmAuth(9), Management(10), and anything unrecognised.
      for (final appContext in [-1, 8, 9, 10, 99]) {
        expect(
          sectionFromAppContext(appContext),
          Section.home,
          reason: 'appContext $appContext',
        );
      }
    });
  });
}
