import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:flutter_onboarding/feature/onboarding/cubit/onboarding_cubit.dart';
import 'package:flutter_onboarding/values/app_colors.dart';
import 'package:page_view_indicators/circle_page_indicator.dart';

import 'onboarding_view.dart';

class OnBoardingBody extends StatefulWidget {
  const OnBoardingBody();

  @override
  State<StatefulWidget> createState() => OnBoardingBodyState();
}

class OnBoardingBodyState extends State<OnBoardingBody> {
  final PageController _pageController = PageController(initialPage: 0);
  final _currentPageNotifier = ValueNotifier<int>(0);

  @override
  Widget build(BuildContext context) =>
      BlocBuilder<OnboardingCubit, OnboardingState>(
        builder: (BuildContext context, state) {
          if (state is OnboardingInitial) {
            return Container(
              padding: const EdgeInsets.all(8.0),
              alignment: AlignmentDirectional.center,
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  Expanded(
                    child: PageView(
                      scrollDirection: Axis.horizontal,
                      controller: _pageController,
                      children: state.onboardingItems
                          .map((e) => OnBoardingView(onBoardingItem: e))
                          .toList(),
                      onPageChanged: (index) =>
                          _currentPageNotifier.value = index,
                    ),
                  ),
                  _buildCircleIndicator(state.onboardingItems.length),
                  SizedBox(height: 50),
                  Row(
                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                    children: [
                      _skipButton(),
                      _nextButton(),
                    ],
                  )
                ],
              ),
            );
          }
          return const SizedBox();
        },
      );

  Widget _buildCircleIndicator(int itemCount) {
    return Padding(
      padding: const EdgeInsets.all(8.0),
      child: CirclePageIndicator(
        itemCount: itemCount,
        currentPageNotifier: _currentPageNotifier,
      ),
    );
  }

  Widget _skipButton() => TextButton(
        onPressed: () => SystemNavigator.pop(),
        child: Text(
          'Skip',
          style: Theme.of(context).textTheme.button!.copyWith(
              color: AppColors.material_grey_400, fontWeight: FontWeight.bold),
        ),
      );

  Widget _nextButton() => TextButton(
        onPressed: () => _pageController.nextPage(
            duration: Duration(milliseconds: 250), curve: Curves.easeIn),
        child: Text(
          'Next',
          style: Theme.of(context)
              .textTheme
              .button!
              .copyWith(color: Colors.white, fontWeight: FontWeight.bold),
        ),
      );
}
