package co.netguru.baby.monitor.client.feature.babycrynotification

import co.netguru.baby.monitor.RxSchedulersOverrideRule
import co.netguru.baby.monitor.client.feature.firebasenotification.FirebaseNotificationSender
import co.netguru.baby.monitor.client.feature.firebasenotification.NotificationType
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.junit.Rule
import org.junit.Test

class NotifyBabyCryingUseCaseTest {

    @get:Rule
    val schedulersRule = RxSchedulersOverrideRule()

    private val notificationSender: FirebaseNotificationSender = mock()
    private val title = "title"
    private val text = "text"
    private val notifyBabyCryingUseCase = NotifyBabyCryingUseCase(notificationSender, title, text)

    @Test
    fun `should send crying notification on notifyBabyCrying`() {
        notifyBabyCryingUseCase.notifyBabyCrying()

        verify(notificationSender).broadcastNotificationToFcm(
            title,
            text,
            NotificationType.CRY_NOTIFICATION
        )
    }
}
