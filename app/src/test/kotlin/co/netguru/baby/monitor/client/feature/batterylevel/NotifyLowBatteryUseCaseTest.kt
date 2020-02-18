package co.netguru.baby.monitor.client.feature.batterylevel

import co.netguru.baby.monitor.RxSchedulersOverrideRule
import co.netguru.baby.monitor.client.feature.firebasenotification.FirebaseNotificationSender
import co.netguru.baby.monitor.client.feature.firebasenotification.NotificationType
import com.nhaarman.mockitokotlin2.argThat
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.junit.Rule
import org.junit.Test

class NotifyLowBatteryUseCaseTest {

    @get:Rule
    val schedulersRule = RxSchedulersOverrideRule()

    private val notificationSender: FirebaseNotificationSender = mock()
    private val notifyLowBatteryUseCase = NotifyLowBatteryUseCase(notificationSender)

    @Test
    fun `should send low battery notification on notifyLowBattery`() {
        val title = "title"
        val text = "text"
        notifyLowBatteryUseCase.notifyLowBattery(title, text)

        verify(notificationSender).broadcastNotificationToFcm(
           argThat {
               type == NotificationType.LOW_BATTERY_NOTIFICATION
           }
        )
    }
}
