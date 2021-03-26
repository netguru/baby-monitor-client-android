part of 'onboarding_cubit.dart';

@immutable
abstract class OnboardingState {}

class OnboardingInitial extends OnboardingState {
  final List<OnboardingItem> onboardingItems;

  OnboardingInitial(this.onboardingItems);
}
