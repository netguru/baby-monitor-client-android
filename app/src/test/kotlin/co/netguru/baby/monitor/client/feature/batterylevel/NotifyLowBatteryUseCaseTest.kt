package co.netguru.baby.monitor.client.feature.batterylevel

import co.netguru.baby.monitor.RxSchedulersOverrideRule
import co.netguru.baby.monitor.client.feature.firebasenotification.FirebaseNotificationSender
import co.netguru.baby.monitor.client.feature.firebasenotification.NotificationType
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class NotifyLowBatteryUseCaseTest {

    @Rule
    @JvmField
    val schedulersRule = RxSchedulersOverrideRule()

    private val notificationSender: FirebaseNotificationSender = mock()
    private lateinit var notifyLowBatteryUseCase: NotifyLowBatteryUseCase

    @Before
    fun setUp() {
        notifyLowBatteryUseCase = NotifyLowBatteryUseCase(notificationSender)
    }

    @Test
    fun `should send low battery notification on notifyLowBattery`() {
        val title = "title"
        val text = "text"
        notifyLowBatteryUseCase.notifyLowBattery(title, text)

        verify(notificationSender).broadcastNotificationToFcm(title, text, NotificationType.LOW_BATTERY_NOTIFICATION)
    }
}
