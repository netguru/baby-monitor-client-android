package co.netguru.baby.monitor.client.feature.server

import androidx.core.os.bundleOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import co.netguru.baby.monitor.client.data.communication.websocket.ClientConnectionStatus
import co.netguru.baby.monitor.client.feature.analytics.AnalyticsManager
import co.netguru.baby.monitor.client.feature.analytics.AnalyticsManager.Companion.ENABLED_PARAM
import co.netguru.baby.monitor.client.feature.analytics.AnalyticsManager.Companion.NIGHT_MODE_EVENT
import co.netguru.baby.monitor.client.feature.batterylevel.NotifyLowBatteryUseCase
import co.netguru.baby.monitor.client.feature.communication.websocket.Message
import co.netguru.baby.monitor.client.feature.communication.websocket.WebSocketServerService
import dagger.Lazy
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject

class ChildMonitorViewModel @Inject constructor(
    private val receiveFirebaseTokenUseCase: Lazy<ReceiveFirebaseTokenUseCase>,
    private val notifyLowBatteryUseCase: NotifyLowBatteryUseCase,
    private val analyticsManager: AnalyticsManager
) : ViewModel() {

    private val disposables = CompositeDisposable()
    private val mutableBabyNameStatus = MutableLiveData<String>()
    val babyNameStatus: LiveData<String> = mutableBabyNameStatus
    private val mutablePulsatingViewStatus = MutableLiveData<ClientConnectionStatus>()
    val pulsatingViewStatus: LiveData<ClientConnectionStatus> = mutablePulsatingViewStatus
    private val mutableNightModeStatus = MutableLiveData<Boolean>()
    internal val nightModeStatus: LiveData<Boolean> = mutableNightModeStatus

    private val mutablePairingCodeLiveData = MutableLiveData<String>()
    internal val pairingCodeLiveData: LiveData<String> = mutablePairingCodeLiveData

    private fun receiveFirebaseToken(ipAddress: String, token: String) {
        receiveFirebaseTokenUseCase.get().receiveToken(ipAddress = ipAddress, token = token)
            .subscribeOn(Schedulers.io())
            .subscribeBy(
                onComplete = {
                    Timber.d("Firebase token saved for address $ipAddress.")
                },
                onError = { error ->
                    Timber.w(error, "Couldn't save Firebase token.")
                }
            )
            .addTo(disposables)
    }

    fun notifyLowBattery(title: String, text: String) {
        notifyLowBatteryUseCase.notifyLowBattery(title = title, text = text)
            .subscribeBy(
                onComplete = {
                    Timber.d("Low battery notification posted.")
                },
                onError = { error ->
                    Timber.w(error, "Error posting low battery notification.")
                }
            )
            .addTo(disposables)
    }

    fun handleWebSocketServerBinder(binder: WebSocketServerService.Binder) {
        Timber.d("handleWebSocketServerBinder($binder)")

        disposables += binder.clientConnectionStatus().subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onNext = { connectionStatus ->
                    mutablePulsatingViewStatus.postValue(connectionStatus)
                },
                onError = { Timber.e(it) }
            )

        disposables += binder.messages()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { (ws, message) ->
                message.pushNotificationsToken?.let {
                    receiveFirebaseToken(ws.remoteSocketAddress.address.hostAddress, it)
                }
                message.babyName?.let { name ->
                    mutableBabyNameStatus.postValue(name)
                }
                message.pairingCode?.let {
                    mutablePairingCodeLiveData.postValue(it)
                }
            }
    }

    fun switchNightMode() {
        val currentStatus = mutableNightModeStatus.value == true
        analyticsManager.logEvent(NIGHT_MODE_EVENT, bundleOf(ENABLED_PARAM to !currentStatus))
        mutableNightModeStatus.postValue(!currentStatus)
    }

    fun approvePairingCode(binder: WebSocketServerService.Binder) {
        binder.sendMessage(
            Message(
                pairingApproved = true
            )
        )
    }

    fun disapprovePairingCode(binder: WebSocketServerService.Binder) {
        binder.sendMessage(
            Message(
                pairingApproved = false
            )
        )
        mutablePairingCodeLiveData.postValue("")
    }

    override fun onCleared() {
        disposables.clear()
    }
}
