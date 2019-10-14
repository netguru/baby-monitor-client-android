package co.netguru.baby.monitor.client.feature.client.configuration

import android.app.Activity
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.content.Intent
import co.netguru.baby.monitor.client.application.firebase.FirebaseRepository
import co.netguru.baby.monitor.client.common.NotificationHandler
import co.netguru.baby.monitor.client.common.RunsInBackground
import co.netguru.baby.monitor.client.data.DataRepository
import co.netguru.baby.monitor.client.data.client.ChildDataEntity
import co.netguru.baby.monitor.client.data.client.home.log.LogDataEntity
import co.netguru.baby.monitor.client.data.splash.AppState
import co.netguru.baby.monitor.client.feature.communication.nsd.NsdServiceManager
import co.netguru.baby.monitor.client.feature.onboarding.OnboardingActivity
import io.reactivex.Completable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.rxkotlin.toCompletable
import io.reactivex.schedulers.Schedulers
import org.threeten.bp.LocalDateTime
import timber.log.Timber
import javax.inject.Inject

class ConfigurationViewModel @Inject constructor(
    private val nsdServiceManager: NsdServiceManager,
    private val notificationHandler: NotificationHandler,
    private val dataRepository: DataRepository,
    private val firebaseRepository: FirebaseRepository
) : ViewModel() {

    internal val appSavedState = MutableLiveData<AppState>()
    private val compositeDisposable = CompositeDisposable()

    init {
        nsdServiceManager.serviceInfoData.observeForever { list ->
            list?.first()?.let { service ->
                handleNewService(service.host.hostAddress, service.port)
            }
        }
    }

    private fun handleNewService(
        address: String,
        port: Int
    ) {
        val address = "ws://$address:$port"
        dataRepository.doesChildDataExists(address)
            .flatMapCompletable { exists ->
                if (exists) {
                    Completable.complete()
                } else {
                    dataRepository.putChildData(ChildDataEntity(address))
                }
            }
            .andThen(addParingEventToDataBase(address))
            .andThen(dataRepository.getSavedState())
            .subscribeOn(Schedulers.io())
            .subscribeBy(
                onSuccess = { state ->
                    appSavedState.postValue(state)
                },
                onError = Timber::e
            ).addTo(compositeDisposable)
    }

    internal fun clearData(activity: Activity) {
        Completable.merge(
            listOf(
                dataRepository.deleteAllData(),
                notificationHandler::clearNotifications.toCompletable()
            )
        )
            .subscribeOn(Schedulers.io())
            .subscribeBy(
                onComplete = {
                    handleDataCleared(activity)
                },
                onError = Timber::e
            ).addTo(compositeDisposable)
    }

    internal fun discoverNsdService(onServiceConnectedListener: NsdServiceManager.OnServiceConnectedListener) {
        nsdServiceManager.discoverService(onServiceConnectedListener)
    }

    internal fun stopNsdServiceDiscovery() {
        nsdServiceManager.stopServiceDiscovery()
    }

    internal fun isUploadEnablad() = firebaseRepository.isUploadEnablad()

    internal fun setUploadEnabled(enabled: Boolean) {
        firebaseRepository.setUploadEnabled(enabled)
    }

    override fun onCleared() {
        nsdServiceManager.stopServiceDiscovery()
        compositeDisposable.dispose()
        super.onCleared()
    }

    @RunsInBackground
    private fun handleDataCleared(activity: Activity) {
        activity.startActivity(
            Intent(activity, OnboardingActivity::class.java)
        )
        activity.finish()
    }

    @RunsInBackground
    private fun addParingEventToDataBase(address: String) =
        dataRepository.insertLogToDatabase(
            LogDataEntity(
                DEVICES_PAIRED,
                LocalDateTime.now().toString(),
                address
            )
        )

    companion object {
        private const val DEVICES_PAIRED = "Devices were paired correctly"
    }
}
