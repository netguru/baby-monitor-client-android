// GENERATED CODE - DO NOT MODIFY BY HAND
import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import 'intl/messages_all.dart';

// **************************************************************************
// Generator: Flutter Intl IDE plugin
// Made by Localizely
// **************************************************************************

// ignore_for_file: non_constant_identifier_names, lines_longer_than_80_chars
// ignore_for_file: join_return_with_assignment, prefer_final_in_for_each
// ignore_for_file: avoid_redundant_argument_values

class S {
  S();

  static S? _current;

  static S get current {
    assert(_current != null, 'No instance of S was loaded. Try to initialize the S delegate before accessing S.current.');
    return _current!;
  }

  static const AppLocalizationDelegate delegate =
    AppLocalizationDelegate();

  static Future<S> load(Locale locale) {
    final name = (locale.countryCode?.isEmpty ?? false) ? locale.languageCode : locale.toString();
    final localeName = Intl.canonicalizedLocale(name); 
    return initializeMessages(localeName).then((_) {
      Intl.defaultLocale = localeName;
      final instance = S();
      S._current = instance;
 
      return instance;
    });
  } 

  static S of(BuildContext context) {
    final instance = S.maybeOf(context);
    assert(instance != null, 'No instance of S present in the widget tree. Did you add S.delegate in localizationsDelegates?');
    return instance!;
  }

  static S? maybeOf(BuildContext context) {
    return Localizations.of<S>(context, S);
  }

  /// `Do your daily errands and \n keep an eye on your baby`
  String get feature_a_text_1 {
    return Intl.message(
      'Do your daily errands and \n keep an eye on your baby',
      name: 'feature_a_text_1',
      desc: '',
      args: [],
    );
  }

  /// `Easily check on the baby anywhere \n and anytime you want`
  String get feature_a_text_2 {
    return Intl.message(
      'Easily check on the baby anywhere \n and anytime you want',
      name: 'feature_a_text_2',
      desc: '',
      args: [],
    );
  }

  /// `Crying baby or noise detected alert`
  String get feature_b_text_1 {
    return Intl.message(
      'Crying baby or noise detected alert',
      name: 'feature_b_text_1',
      desc: '',
      args: [],
    );
  }

  /// `You'll get a notification when your baby starts crying (beta) or some noise will be detected.\nYou can set the exact sound detection setting on parent’s device.`
  String get feature_b_text_2 {
    return Intl.message(
      'You\'ll get a notification when your baby starts crying (beta) or some noise will be detected.\nYou can set the exact sound detection setting on parent’s device.',
      name: 'feature_b_text_2',
      desc: '',
      args: [],
    );
  }

  /// `Your privacy is safe when\nusing BabyGuard`
  String get feature_c_text_1 {
    return Intl.message(
      'Your privacy is safe when\nusing BabyGuard',
      name: 'feature_c_text_1',
      desc: '',
      args: [],
    );
  }

  /// `Video and audio will not be recorded, unless\nyou want to help us develop the app and\ngive your permission to do it.`
  String get feature_c_text_2 {
    return Intl.message(
      'Video and audio will not be recorded, unless\nyou want to help us develop the app and\ngive your permission to do it.',
      name: 'feature_c_text_2',
      desc: '',
      args: [],
    );
  }

  /// `Send anonymized baby\nvoice recordings`
  String get feature_d_text_1 {
    return Intl.message(
      'Send anonymized baby\nvoice recordings',
      name: 'feature_d_text_1',
      desc: '',
      args: [],
    );
  }

  /// `Voice recordings are only used for\ndevelopment purposes and never without\nyour permission.`
  String get feature_d_text_2 {
    return Intl.message(
      'Voice recordings are only used for\ndevelopment purposes and never without\nyour permission.',
      name: 'feature_d_text_2',
      desc: '',
      args: [],
    );
  }
}

class AppLocalizationDelegate extends LocalizationsDelegate<S> {
  const AppLocalizationDelegate();

  List<Locale> get supportedLocales {
    return const <Locale>[
      Locale.fromSubtags(languageCode: 'en'),
    ];
  }

  @override
  bool isSupported(Locale locale) => _isSupported(locale);
  @override
  Future<S> load(Locale locale) => S.load(locale);
  @override
  bool shouldReload(AppLocalizationDelegate old) => false;

  bool _isSupported(Locale locale) {
    for (var supportedLocale in supportedLocales) {
      if (supportedLocale.languageCode == locale.languageCode) {
        return true;
      }
    }
    return false;
  }
}