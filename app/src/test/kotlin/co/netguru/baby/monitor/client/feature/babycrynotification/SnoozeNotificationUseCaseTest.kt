package co.netguru.baby.monitor.client.feature.babycrynotification

import co.netguru.baby.monitor.RxSchedulersOverrideRule
import co.netguru.baby.monitor.client.data.DataRepository
import com.nhaarman.mockitokotlin2.*
import io.reactivex.Completable
import org.junit.Rule
import org.junit.Test

class SnoozeNotificationUseCaseTest {

    @get:Rule
    val schedulersRule = RxSchedulersOverrideRule()

    private val dataRepository: DataRepository = mock()
    private val snoozeNotificationUseCase = SnoozeNotificationUseCase(dataRepository)

    @Test
    fun `should update snoozeTimestamp on snoozeNotifications`() {
        whenever(dataRepository.updateChildSnoozeTimestamp(any())).doReturn(Completable.complete())

        snoozeNotificationUseCase.snoozeNotifications()

        verify(dataRepository).updateChildSnoozeTimestamp(any())
    }
}
