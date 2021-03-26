import 'package:freezed_annotation/freezed_annotation.dart';
import 'package:flutter/foundation.dart';

part 'onboarding_item.freezed.dart';

@Freezed()
class OnboardingItem with _$OnboardingItem {
  factory OnboardingItem({required String title,required String subtitle,required String imagePath}) = _OnboardingItem;
}