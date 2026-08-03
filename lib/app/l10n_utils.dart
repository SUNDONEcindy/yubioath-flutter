/*
 * Copyright (C) 2025-2026 Yubico.
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

import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:url_launcher/url_launcher.dart';

/// Creates [RichText] from [text] where the keys of [urls] are replaced
/// with clickable links.
///
/// Each link owns a [TapGestureRecognizer], which Flutter does not dispose on
/// its own. A caller that rebuilds the returned [Text] more than once (e.g. from
/// [State.didChangeDependencies]) should pass a [recognizers] list to collect
/// them and dispose each one when it is done, otherwise they leak.
Text injectLinksInText(
  String text,
  Map<String, Uri> urls, {
  TextStyle? textStyle,
  TextStyle? linkStyle,
  List<TapGestureRecognizer>? recognizers,
}) {
  final keys = urls.keys.toList();
  // Split text by keys and keep the keys. Escape each key so a URL containing regex
  // metacharacters (the dots in a host, say) is matched literally, not as a pattern.
  final alternation = keys.map(RegExp.escape).join('|');
  final pattern = RegExp('(?=($alternation))|(?<=($alternation))');
  final parts = text.split(pattern);

  List<TextSpan> spans = [];
  int index = 0;
  for (var part in parts) {
    if (keys.contains(part)) {
      final recognizer = TapGestureRecognizer()
        ..onTap = () async {
          await launchUrl(urls[part]!, mode: LaunchMode.externalApplication);
        };
      recognizers?.add(recognizer);
      spans.add(
        TextSpan(
          text: part,
          style: linkStyle,
          recognizer: recognizer,
          children: [
            if (index == parts.length - 1)
              // without this the recognizer takes over whole row
              TextSpan(text: ' '),
          ],
        ),
      );
    } else {
      spans.add(TextSpan(text: part));
    }
    index += 1;
  }
  return Text.rich(TextSpan(style: textStyle, children: spans));
}
