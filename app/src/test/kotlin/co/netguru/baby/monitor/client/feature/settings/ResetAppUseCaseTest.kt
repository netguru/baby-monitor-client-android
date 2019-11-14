package co.netguru.baby.monitor.client.feature.settings

import co.netguru.baby.monitor.RxSchedulersOverrideRule
import co.netguru.baby.monitor.client.common.NotificationHandler
import co.netguru.baby.monitor.client.data.DataRepository
import co.netguru.baby.monitor.client.data.splash.AppState
import co.netguru.baby.monitor.client.feature.firebasenotification.FirebaseInstanceManager
import com.nhaarman.mockitokotlin2.*
import io.reactivex.Completable
import io.reactivex.Single
import org.junit.Rule
import org.junit.Test

class ResetAppUseCaseTest {

    @get:Rule
    val schedulersRule = RxSchedulersOverrideRule()

    private val notificationHandler = mock<NotificationHandler>()
    private val firebaseInstanceManager = mock<FirebaseInstanceManager>()
    private val dataRepository = mock<DataRepository> {
        on { deleteAllData() }.doReturn(Completable.complete())
        on { getSavedState() }.doReturn(Single.just(AppState.UNDEFINED))
    }
    private val resetAppUseCase =
        ResetAppUseCase(notificationHandler, firebaseInstanceManager, dataRepository)

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
