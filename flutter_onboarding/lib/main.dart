import 'package:flutter/material.dart';
import 'package:flutter_localizations/flutter_localizations.dart';
import 'package:flutter_onboarding/feature/onboarding/onboarding_page.dart';
import 'package:flutter_onboarding/values/app_theme.dart';

import 'generated/l10n.dart';

void main() => runApp(MyApp());

class MyApp extends StatelessWidget {
  // This widget is the root of your application.
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      theme: lightTheme(),
      darkTheme: darkTheme(),
      home: OnBoardingPage(),
      localizationsDelegates: _getLocalizationsDelegates(),
      supportedLocales: S.delegate.supportedLocales,
    );
  }

  List<LocalizationsDelegate> _getLocalizationsDelegates() => [
    S.delegate,
    GlobalMaterialLocalizations.delegate,
    GlobalWidgetsLocalizations.delegate,
    GlobalCupertinoLocalizations.delegate,
  ];
}

