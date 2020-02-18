package co.netguru.baby.monitor.client.feature.babycrynotification

import co.netguru.baby.monitor.RxSchedulersOverrideRule
import co.netguru.baby.monitor.client.feature.babynotification.NotifyBabyEventUseCase
import co.netguru.baby.monitor.client.feature.firebasenotification.FirebaseNotificationSender
import co.netguru.baby.monitor.client.feature.firebasenotification.NotificationType
import com.nhaarman.mockitokotlin2.*
import org.junit.Rule
import org.junit.Test

class NotifyBabyEventUseCaseTest {

    @get:Rule
    val schedulersRule = RxSchedulersOverrideRule()

    private val notificationSender: FirebaseNotificationSender = mock()
    private val text = "text"
    private val notificationTexts = mock<Map<String, String>> {
        on { get(any<String>()) }.doReturn(text)
    }

    private val notifyBabyEventUseCase =
        NotifyBabyEventUseCase(
            notificationSender,
            notificationTexts
        )

    @Test
    fun `should send crying notification on notifyBabyCrying`() {
        notifyBabyEventUseCase
            .babyEvents()
            .subscribe()

        notifyBabyEventUseCase.notifyBabyCrying()

        verify(notificationSender).broadcastNotificationToFcm(
            argThat {
                type == NotificationType.CRY_NOTIFICATION
            }
        )
    }

    @Test
    fun `should send crying notification on notifyNoiseDetected`() {
        notifyBabyEventUseCase
            .babyEvents()
            .subscribe()

        notifyBabyEventUseCase.notifyNoiseDetected()

        verify(notificationSender).broadcastNotificationToFcm(
            argThat {
                type == NotificationType.NOISE_NOTIFICATION
            }

        )
    }
}
