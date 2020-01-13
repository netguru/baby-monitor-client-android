package co.netguru.baby.monitor.client.feature.babycrynotification

import co.netguru.baby.monitor.RxSchedulersOverrideRule
import co.netguru.baby.monitor.client.data.DataRepository
import co.netguru.baby.monitor.client.feature.analytics.AnalyticsManager
import co.netguru.baby.monitor.client.feature.analytics.Event
import co.netguru.baby.monitor.client.feature.analytics.EventType
import com.nhaarman.mockitokotlin2.*
import io.reactivex.Completable
import org.junit.Rule
import org.junit.Test

class SnoozeNotificationUseCaseTest {

    @get:Rule
    val schedulersRule = RxSchedulersOverrideRule()

    private val dataRepository: DataRepository = mock {
        on { updateChildSnoozeTimestamp(any()) }.doReturn(Completable.complete())
    }
    private val analyticsManager: AnalyticsManager = mock()
    private val snoozeNotificationUseCase =
        SnoozeNotificationUseCase(dataRepository, analyticsManager)

    @Test
    fun `should update snoozeTimestamp on snoozeNotifications`() {
        snoozeNotificationUseCase.snoozeNotifications()

        verify(dataRepository).updateChildSnoozeTimestamp(any())
    }

    @Test
    fun `should send snoozeNotification event to firebase`() {
        snoozeNotificationUseCase.snoozeNotifications()

        verify(analyticsManager).logEvent(check {
            it is Event.Simple && it.eventType == EventType.NOTIFICATION_SNOOZE
        })
    }
}
