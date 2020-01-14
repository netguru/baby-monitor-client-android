package co.netguru.baby.monitor.client.feature.onboarding

import co.netguru.baby.monitor.client.data.DataRepository
import co.netguru.baby.monitor.client.data.splash.AppState
import javax.inject.Inject

class FinishOnboardingUseCase @Inject constructor(
    private val dataRepository: DataRepository
) {

    fun finishOnboarding() {
        dataRepository.saveConfiguration(AppState.UNDEFINED)
    }
}
