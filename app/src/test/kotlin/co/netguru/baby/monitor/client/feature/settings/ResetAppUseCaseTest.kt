package co.netguru.baby.monitor.client.feature.settings

import co.netguru.baby.monitor.RxSchedulersOverrideRule
import co.netguru.baby.monitor.client.common.NotificationHandler
import co.netguru.baby.monitor.client.data.DataRepository
import co.netguru.baby.monitor.client.data.splash.AppState
import co.netguru.baby.monitor.client.feature.firebasenotification.FirebaseInstanceManager
import com.nhaarman.mockito_kotlin.*
import io.reactivex.Completable
import io.reactivex.Single
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ResetAppUseCaseTest {

    @Rule
    @JvmField
    val schedulersRule = RxSchedulersOverrideRule()

    private val notificationHandler = mock<NotificationHandler>()
    private val firebaseInstanceManager = mock<FirebaseInstanceManager>()
    private val dataRepository = mock<DataRepository>()
    private lateinit var resetAppUseCase: ResetAppUseCase

    @Before
    fun setup() {
        whenever(dataRepository.deleteAllData()).doReturn(Completable.complete())
        whenever(dataRepository.getSavedState()).doReturn(Single.just(AppState.UNDEFINED))
        resetAppUseCase =
            ResetAppUseCase(notificationHandler, firebaseInstanceManager, dataRepository)

    }


    @Test
    fun `should clear data and notifications on app reset`() {
        resetAppUseCase.resetApp().subscribe()

        verify(dataRepository).deleteAllData()
        verify(notificationHandler).clearNotifications()
    }

    @Test
    fun `should invalidate firebase token on client state`() {
        whenever(dataRepository.getSavedState()).doReturn(Single.just(AppState.CLIENT))

        resetAppUseCase.resetApp().subscribe()

        verify(firebaseInstanceManager).invalidateFirebaseToken()
    }

    @Test
    fun `should not invalidate firebase token on server state`() {
        whenever(dataRepository.getSavedState()).doReturn(Single.just(AppState.SERVER))

        resetAppUseCase.resetApp().subscribe()

        verifyZeroInteractions(firebaseInstanceManager)
    }
}
