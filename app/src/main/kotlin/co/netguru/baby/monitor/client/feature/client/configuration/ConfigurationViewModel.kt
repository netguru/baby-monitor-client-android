package co.netguru.baby.monitor.client.feature.client.configuration

import android.app.Activity
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import co.netguru.baby.monitor.client.application.firebase.FirebaseRepository
import co.netguru.baby.monitor.client.common.RunsInBackground
import co.netguru.baby.monitor.client.data.DataRepository
import co.netguru.baby.monitor.client.data.client.ChildDataEntity
import co.netguru.baby.monitor.client.data.splash.AppState
import co.netguru.baby.monitor.client.feature.communication.nsd.NsdServiceManager
import co.netguru.baby.monitor.client.feature.splash.EnterActivity
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject

class ConfigurationViewModel @Inject constructor(
        private val nsdServiceManager: NsdServiceManager,
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

        handleNewService("192.168.1.85", 100000)
    }

    private fun handleNewService(
            address: String,
            port: Int
    ) {
        val address = "ws://$address:$port"
        dataRepository.getChildDataWithAddress(address)
                .flatMapCompletable {list ->
                    if (list.isNullOrEmpty()) {
                        dataRepository.addChildData(ChildDataEntity(address))
                    } else {
                        dataRepository.updateChildData(list[0].copy(address = address))
                    }
                }.andThen(dataRepository.getSavedState())
                .subscribeOn(Schedulers.io())
                .subscribeBy(
                        onSuccess = { state ->
                            appSavedState.postValue(state)
                        },
                        onError = Timber::e
                ).addTo(compositeDisposable)
    }

    internal fun clearData(activity: Activity) {
        dataRepository.deleteAllData()
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
                Intent(activity, EnterActivity::class.java)
        )
        activity.finish()
    }

    fun openMarket(activity: Activity) {
        val uri = Uri.parse("market://details?id=" + activity.packageName)
        val goToMarket = Intent(Intent.ACTION_VIEW, uri)

        var flags = Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_MULTIPLE_TASK
        flags = if (Build.VERSION.SDK_INT >= 21) {
            flags or Intent.FLAG_ACTIVITY_NEW_DOCUMENT
        } else {
            flags or Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET
        }

        goToMarket.addFlags(flags)
        try {
            activity.startActivity(goToMarket)
        } catch (e: ActivityNotFoundException) {
            activity.startActivity(Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=" + activity.packageName)))
        }
    }
}
