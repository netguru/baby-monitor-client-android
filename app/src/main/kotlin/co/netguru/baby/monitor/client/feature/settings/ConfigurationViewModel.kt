package co.netguru.baby.monitor.client.feature.settings

import android.app.Activity
import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import android.content.Intent
import android.net.wifi.WifiManager
import androidx.lifecycle.LiveData
import co.netguru.baby.monitor.client.application.firebase.FirebaseRepository
import co.netguru.baby.monitor.client.common.LocalDateTimeProvider
import co.netguru.baby.monitor.client.common.RunsInBackground
import co.netguru.baby.monitor.client.data.DataRepository
import co.netguru.baby.monitor.client.data.client.ChildDataEntity
import co.netguru.baby.monitor.client.data.client.home.log.LogDataEntity
import co.netguru.baby.monitor.client.feature.communication.nsd.NsdServiceManager
import co.netguru.baby.monitor.client.feature.onboarding.OnboardingActivity
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject

class ConfigurationViewModel @Inject constructor(
    private val nsdServiceManager: NsdServiceManager,
    private val resetAppUseCase: ResetAppUseCase,
    private val dataRepository: DataRepository,
    private val firebaseRepository: FirebaseRepository,
    private val localDateTimeProvider: LocalDateTimeProvider
) : ViewModel() {

    internal val connectionCompletedState = MutableLiveData<Boolean>()
    private val compositeDisposable = CompositeDisposable()
    private val mutableResetInProgress = MutableLiveData<Boolean>()
    private var multicastLock: WifiManager.MulticastLock? = null
    val resetInProgress: LiveData<Boolean> = mutableResetInProgress

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
        dataRepository.putChildData(ChildDataEntity(address))
            .andThen(addPairingEventToDataBase(address))
            .subscribeOn(Schedulers.io())
            .subscribeBy(
                onComplete = { connectionCompletedState.postValue(true) },
                onError = { connectionCompletedState.postValue(false) }
            ).addTo(compositeDisposable)
    }

    fun resetApp(activity: Activity) {
        resetAppUseCase.resetApp()
            .doOnSubscribe { mutableResetInProgress.postValue(true) }
            .subscribeOn(Schedulers.io())
            .subscribeBy(
                onComplete = {
                    handleAppReset(activity)
                },
                onError = {
                    mutableResetInProgress.postValue(false)
                    Timber.w(it)
                }
            ).addTo(compositeDisposable)
    }

    internal fun discoverNsdService(
        onServiceConnectedListener: NsdServiceManager.OnServiceConnectedListener,
        context: Context
    ) {
        acquireMulticastLock(context)
        nsdServiceManager.discoverService(onServiceConnectedListener)
    }

    private fun acquireMulticastLock(context: Context) {
        // Pixel workaround for discovering services
        val wifi = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        multicastLock = wifi.createMulticastLock(MUTLICAST_LOCK_TAG)
        multicastLock?.setReferenceCounted(true)
        multicastLock?.acquire()
    }

    internal fun stopNsdServiceDiscovery() {
        releaseMulticastLock()
        nsdServiceManager.stopServiceDiscovery()
    }

    private fun releaseMulticastLock() {
        multicastLock?.release()
        multicastLock = null
    }

    internal fun isUploadEnabled() = firebaseRepository.isUploadEnablad()

    internal fun setUploadEnabled(enabled: Boolean) {
        firebaseRepository.setUploadEnabled(enabled)
    }

    override fun onCleared() {
        nsdServiceManager.stopServiceDiscovery()
        compositeDisposable.dispose()
        super.onCleared()
    }

    @RunsInBackground
    private fun handleAppReset(activity: Activity) {
        activity.startActivity(
            Intent(activity, OnboardingActivity::class.java)
        )
        activity.finish()
    }

    @RunsInBackground
    private fun addPairingEventToDataBase(address: String) =
        dataRepository.insertLogToDatabase(
            LogDataEntity(
                DEVICES_PAIRED,
                localDateTimeProvider.now().toString(),
                address
            )
        )

    companion object {
        private const val DEVICES_PAIRED = "Devices were paired correctly"
        internal const val MUTLICAST_LOCK_TAG = "multicastLock"
    }
}
