package co.netguru.baby.monitor.client.feature.settings

import co.netguru.baby.monitor.client.common.NotificationHandler
import co.netguru.baby.monitor.client.data.DataRepository
import co.netguru.baby.monitor.client.data.splash.AppState
import co.netguru.baby.monitor.client.feature.firebasenotification.FirebaseInstanceManager
import io.reactivex.Completable
import io.reactivex.rxkotlin.toCompletable
import javax.inject.Inject

class ResetAppUseCase @Inject constructor(
    private val notificationHandler: NotificationHandler,
    private val firebaseInstanceManager: FirebaseInstanceManager,
    private val dataRepository: DataRepository
) {

    fun resetApp(): Completable {
        return Completable.merge(
            listOf(
                handleAppState(),
                dataRepository.deleteAllData(),
                notificationHandler::clearNotifications.toCompletable()
            )
        )
    }

    private fun handleAppState(): Completable {
        return dataRepository.getSavedState()
            .flatMapCompletable { appState ->
                when (appState) {
                    AppState.CLIENT -> this::invalidateFirebaseToken.toCompletable()
                    else -> Completable.complete()
                }
            }
    }

    private fun invalidateFirebaseToken() {
        firebaseInstanceManager.invalidateFirebaseToken()
    }
}
