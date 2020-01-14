package co.netguru.baby.monitor.client.feature.onboarding

import co.netguru.baby.monitor.client.data.DataRepository
import co.netguru.baby.monitor.client.data.splash.AppState
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.junit.Test

class FinishOnboardingUseCaseTest {

    private val dataRepository = mock<DataRepository>()
    private val finishOnboardingUseCase = FinishOnboardingUseCase(dataRepository)

    @Test
    fun `should save undefined state`() {
        finishOnboardingUseCase.finishOnboarding()

        verify(dataRepository).saveConfiguration(AppState.UNDEFINED)
    }
}
