package co.netguru.baby.monitor.client.feature.babycrynotification

import co.netguru.baby.monitor.RxSchedulersOverrideRule
import co.netguru.baby.monitor.client.data.DataRepository
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Completable
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SnoozeNotificationUseCaseTest {

    @Rule
    @JvmField
    val schedulersRule = RxSchedulersOverrideRule()

    private val dataRepository: DataRepository = mock()
    lateinit var snoozeNotificationUseCase: SnoozeNotificationUseCase

    @Before
    fun setUp() {
        snoozeNotificationUseCase = SnoozeNotificationUseCase(dataRepository)
    }

    @Test
    fun `should update snoozeTimestamp on snoozeNotifications`() {
        whenever(dataRepository.updateChildSnoozeTimestamp(any())).thenReturn(Completable.complete())

        snoozeNotificationUseCase.snoozeNotifications()

        verify(dataRepository).updateChildSnoozeTimestamp(any())
    }
}
