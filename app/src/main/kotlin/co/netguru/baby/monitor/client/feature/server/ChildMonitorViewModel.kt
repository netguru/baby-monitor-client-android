package co.netguru.baby.monitor.client.feature.server

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import co.netguru.baby.monitor.client.data.communication.websocket.ClientConnectionStatus
import co.netguru.baby.monitor.client.feature.batterylevel.NotifyLowBatteryUseCase
import co.netguru.baby.monitor.client.feature.communication.webrtc.base.RtcCall
import co.netguru.baby.monitor.client.feature.communication.websocket.WebSocketServerService
import dagger.Lazy
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import org.java_websocket.WebSocket
import timber.log.Timber
import javax.inject.Inject

class ChildMonitorViewModel @Inject constructor(
    private val receiveFirebaseTokenUseCase: Lazy<ReceiveFirebaseTokenUseCase>,
    private val notifyLowBatteryUseCase: NotifyLowBatteryUseCase
) : ViewModel() {

    private val disposables = CompositeDisposable()
    private val mutableBabyNameStatus = MutableLiveData<String>()
    val babyNameStatus: LiveData<String> = mutableBabyNameStatus
    private val mutablePulsatingViewStatus = MutableLiveData<ClientConnectionStatus>()
    val pulsatingViewStatus: LiveData<ClientConnectionStatus> = mutablePulsatingViewStatus

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
            .subscribe { (ws, msg) ->
                msg.action()?.let { (key, value) ->
                    handleWebSocketAction(ws, key, value)
                }

                msg.babyName?.let { name ->
                    mutableBabyNameStatus.postValue(name)
                }
            }
    }

    private fun handleWebSocketAction(ws: WebSocket, key: String, value: String) {
        if (key == RtcCall.PUSH_NOTIFICATIONS_KEY) {
            receiveFirebaseToken(ws.remoteSocketAddress.address.hostAddress, value)
        } else {
            Timber.w("Unhandled web socket action: '$key', '$value'.")
        }
    }

    override fun onCleared() {
        disposables.clear()
    }
}
