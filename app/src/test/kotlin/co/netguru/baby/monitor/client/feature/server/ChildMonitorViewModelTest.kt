package co.netguru.baby.monitor.client.feature.server

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import co.netguru.baby.monitor.RxSchedulersOverrideRule
import co.netguru.baby.monitor.client.feature.analytics.AnalyticsManager
import co.netguru.baby.monitor.client.feature.analytics.Event
import co.netguru.baby.monitor.client.feature.batterylevel.NotifyLowBatteryUseCase
import com.nhaarman.mockitokotlin2.*
import io.reactivex.Completable
import org.junit.Rule
import org.junit.Test

class ChildMonitorViewModelTest {

    @get:Rule
    val schedulersRule = RxSchedulersOverrideRule()

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val notifyLowBatteryUseCase: NotifyLowBatteryUseCase = mock()
    private val analyticsManager: AnalyticsManager = mock()
    private val childViewModel =
        ChildMonitorViewModel(notifyLowBatteryUseCase, analyticsManager)

    @Test
    fun `should use notifyLowBatteryUseCase on notifyLowBattery`() {
        val title = "title"
        val text = "text"
        whenever(
            notifyLowBatteryUseCase.notifyLowBattery(
                title,
                text
            )
        ).doReturn(Completable.complete())

        childViewModel.notifyLowBattery(title, text)

        verify(notifyLowBatteryUseCase).notifyLowBattery(title, text)
    }

    @Test
    fun `should switch nightMode status`() {
        val nightModeObserver: Observer<Boolean> = mock()
        assert(childViewModel.nightModeStatus.value != true)
        childViewModel.nightModeStatus.observeForever(nightModeObserver)

        childViewModel.switchNightMode()

        verify(nightModeObserver).onChanged(true)
        assert(childViewModel.nightModeStatus.value == true)
    }

    @Test
    fun `should send nightMode event to firebase`() {
        childViewModel.switchNightMode()

        verify(analyticsManager).logEvent(Event.ParamEvent.NightMode(true))
    }
}
