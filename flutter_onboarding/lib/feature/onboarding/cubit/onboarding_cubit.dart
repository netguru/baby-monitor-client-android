import 'package:bloc/bloc.dart';
import 'package:flutter/cupertino.dart';
import 'package:flutter_onboarding/data/model/onboarding_item.dart';
import 'package:flutter_onboarding/generated/l10n.dart';
import 'package:flutter_onboarding/values/app_values.dart';
import 'package:meta/meta.dart';

part 'onboarding_state.dart';

class OnboardingCubit extends Cubit<OnboardingState> {
  final BuildContext context;

  late List<OnboardingItem> onBoardingItems;

  OnboardingCubit(this.context)
      : super(OnboardingInitial([
          OnboardingItem(
              title: S.of(context).feature_a_text_1,
              subtitle: S.of(context).feature_a_text_2,
              imagePath: image_Paths['feature_a_round']!),
          OnboardingItem(
              title: S.of(context).feature_b_text_1,
              subtitle: S.of(context).feature_b_text_2,
              imagePath: image_Paths['feature_b_round']!),
          OnboardingItem(
              title: S.of(context).feature_c_text_1,
              subtitle: S.of(context).feature_c_text_2,
              imagePath: image_Paths['feature_c_round']!)
        ]));
}
