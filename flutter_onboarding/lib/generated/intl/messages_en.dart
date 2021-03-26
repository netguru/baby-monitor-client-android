// DO NOT EDIT. This is code generated via package:intl/generate_localized.dart
// This is a library that provides messages for a en locale. All the
// messages from the main program should be duplicated here with the same
// function name.

// Ignore issues from commonly used lints in this file.
// ignore_for_file:unnecessary_brace_in_string_interps, unnecessary_new
// ignore_for_file:prefer_single_quotes,comment_references, directives_ordering
// ignore_for_file:annotate_overrides,prefer_generic_function_type_aliases
// ignore_for_file:unused_import, file_names

import 'package:intl/intl.dart';
import 'package:intl/message_lookup_by_library.dart';

final messages = new MessageLookup();

typedef String MessageIfAbsent(String messageStr, List<dynamic> args);

class MessageLookup extends MessageLookupByLibrary {
  String get localeName => 'en';

  final messages = _notInlinedMessages(_notInlinedMessages);
  static _notInlinedMessages(_) => <String, Function> {
    "feature_a_text_1" : MessageLookupByLibrary.simpleMessage("Do your daily errands and \n keep an eye on your baby"),
    "feature_a_text_2" : MessageLookupByLibrary.simpleMessage("Easily check on the baby anywhere \n and anytime you want"),
    "feature_b_text_1" : MessageLookupByLibrary.simpleMessage("Crying baby or noise detected alert"),
    "feature_b_text_2" : MessageLookupByLibrary.simpleMessage("You\'ll get a notification when your baby starts crying (beta) or some noise will be detected.\nYou can set the exact sound detection setting on parent’s device."),
    "feature_c_text_1" : MessageLookupByLibrary.simpleMessage("Your privacy is safe when\nusing BabyGuard"),
    "feature_c_text_2" : MessageLookupByLibrary.simpleMessage("Video and audio will not be recorded, unless\nyou want to help us develop the app and\ngive your permission to do it."),
    "feature_d_text_1" : MessageLookupByLibrary.simpleMessage("Send anonymized baby\nvoice recordings"),
    "feature_d_text_2" : MessageLookupByLibrary.simpleMessage("Voice recordings are only used for\ndevelopment purposes and never without\nyour permission.")
  };
}
