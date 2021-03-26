import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:flutter_onboarding/feature/onboarding/cubit/onboarding_cubit.dart';
import 'package:flutter_onboarding/feature/onboarding/widgets/onboarding_body.dart';
import 'package:flutter_onboarding/values/app_colors.dart';

class OnBoardingPage extends StatelessWidget {
  @override
  Widget build(BuildContext context) => BlocProvider(
        create: (_) => OnboardingCubit(context),
        child: Scaffold(
          backgroundColor: AppColors.background,
          body: OnBoardingBody(),
        ),
      );
}
