package co.netguru.baby.monitor.client.feature.communication.pairing

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import co.netguru.baby.monitor.client.common.LocalDateTimeProvider
import co.netguru.baby.monitor.client.data.DataRepository
import co.netguru.baby.monitor.client.data.client.ChildDataEntity
import co.netguru.baby.monitor.client.data.client.home.log.LogDataEntity
import co.netguru.baby.monitor.client.feature.communication.websocket.Message
import co.netguru.baby.monitor.client.feature.communication.websocket.MessageParser
import co.netguru.baby.monitor.client.feature.communication.websocket.RxWebSocketClient
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.net.URI
import javax.inject.Inject

class PairingUseCase @Inject constructor(
    private val messageParser: MessageParser,
    private val rxWebSocketClient: RxWebSocketClient,
    private val dataRepository: DataRepository,
    private val localDateTimeProvider: LocalDateTimeProvider
) {
    private val compositeDisposable = CompositeDisposable()
    private val mutablePairingCompletedState = MutableLiveData<Boolean>()
    internal val pairingCompletedState: LiveData<Boolean> = mutablePairingCompletedState

    fun pair(address: URI, pairingCode: String) {
        compositeDisposable += rxWebSocketClient.events(address)
            .subscribeOn(Schedulers.io())
            .subscribeBy(onNext = {
                when (it) {
                    is RxWebSocketClient.Event.Open -> sendPairingCode(pairingCode)
                    is RxWebSocketClient.Event.Message -> handleMessage(it, address)
                }
            }, onError = { mutablePairingCompletedState.postValue(false) })
    }

    fun cancelPairing() {
        sendMessage(
            Message(
                pairingCode = ""
            )
        )
    }

    fun dispose() {
        disconnectFromService()
    }

    private fun sendMessage(message: Message) {
        compositeDisposable += rxWebSocketClient.send(message)
            .subscribeBy(
                onComplete = { Timber.i("message sent: $message") },
                onError = { Timber.e(it) })
    }

    private fun sendPairingCode(pairingCode: String) {
        sendMessage(
            Message(
                pairingCode = pairingCode
            )
        )
    }

    private fun disconnectFromService() {
        rxWebSocketClient.dispose()
        compositeDisposable.dispose()
        mutablePairingCompletedState.postValue(false)
    }

    private fun handleMessage(webSocketMessage: RxWebSocketClient.Event.Message, address: URI) {
        val message = messageParser.parseWebSocketMessage(webSocketMessage)
        message?.pairingApproved?.let { pairingApproved ->
            handlePairingResponse(pairingApproved, address)
        }
    }

    private fun handlePairingResponse(pairingApproved: Boolean, address: URI) {
        if (pairingApproved) {
            handleNewService(address)
        } else {
            disconnectFromService()
        }
    }

    private fun handleNewService(
        address: URI
    ) {
        compositeDisposable += dataRepository.putChildData(ChildDataEntity(address.toString()))
            .andThen(addPairingEventToDataBase(address.toString()))
            .subscribeOn(Schedulers.io())
            .subscribeBy(
                onComplete = { mutablePairingCompletedState.postValue(true) },
                onError = { mutablePairingCompletedState.postValue(false) }
            )
    }

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
    }
}
