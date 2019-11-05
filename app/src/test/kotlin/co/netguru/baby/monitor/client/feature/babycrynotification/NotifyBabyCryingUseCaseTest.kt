package co.netguru.baby.monitor.client.feature.babycrynotification

import co.netguru.baby.monitor.RxSchedulersOverrideRule
import co.netguru.baby.monitor.client.feature.firebasenotification.FirebaseNotificationSender
import co.netguru.baby.monitor.client.feature.firebasenotification.NotificationType
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class NotifyBabyCryingUseCaseTest {

    @Rule
    @JvmField
    val schedulersRule = RxSchedulersOverrideRule()

    private val notificationSender: FirebaseNotificationSender = mock()
    private lateinit var notifyBabyCryingUseCase: NotifyBabyCryingUseCase

    @Before
    fun setUp() {
        notifyBabyCryingUseCase = NotifyBabyCryingUseCase(notificationSender)
    }

    @Test
    fun `should send crying notification on notifyBabyCrying`() {
        val title = "title"
        val text = "text"
        notifyBabyCryingUseCase.subscribe(title, text)

        notifyBabyCryingUseCase.notifyBabyCrying()

        verify(notificationSender).broadcastNotificationToFcm(title, text, NotificationType.CRY_NOTIFICATION)
    }
}
