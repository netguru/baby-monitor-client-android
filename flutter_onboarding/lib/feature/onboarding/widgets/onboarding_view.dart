import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:flutter_onboarding/data/model/onboarding_item.dart';
import 'package:flutter_onboarding/values/app_colors.dart';

class OnBoardingView extends StatelessWidget {
  final OnboardingItem onBoardingItem;

  const OnBoardingView({Key? key, required this.onBoardingItem})
      : super(key: key);

  @override
  Widget build(BuildContext context) => Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          _logoImage(),
          _spacer(),
          _title(context),
          _subtitle(context),
        ],
      );

  Widget _logoImage() => Center(
        child: Image.asset(onBoardingItem.imagePath),
      );

  Widget _title(BuildContext context) => Padding(
        padding: const EdgeInsets.all(8.0),
        child: Text(
          onBoardingItem.title,
          textAlign: TextAlign.center,
          style: Theme.of(context)
              .textTheme
              .headline5!
              .copyWith(color: Colors.white, fontWeight: FontWeight.bold),
        ),
      );

  Widget _subtitle(BuildContext context) => Padding(
        padding: const EdgeInsets.all(8.0),
        child: Text(
          onBoardingItem.subtitle,
          textAlign: TextAlign.center,
          style: Theme.of(context)
              .textTheme
              .subtitle2!
              .copyWith(color: AppColors.accent),
        ),
      );

  Widget _spacer() => SizedBox(height: 50);
}
