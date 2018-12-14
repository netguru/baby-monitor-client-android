package co.netguru.baby.monitor.client.feature.communication.websocket

import co.netguru.baby.monitor.client.feature.common.NotificationHandler
import co.netguru.baby.monitor.client.feature.common.RunsInBackground
import co.netguru.baby.monitor.client.feature.communication.webrtc.RtcCall
import io.reactivex.Completable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import org.json.JSONObject
import timber.log.Timber
import java.util.concurrent.TimeUnit

class ClientsHandler(
        private val listener: ConnectionListener,
        private val notificationHandler: NotificationHandler
) {

    var webSocketClients = mutableMapOf<String, CustomWebSocketClient>()
    private val compositeDisposable = CompositeDisposable()
    @RunsInBackground
    fun addClient(address: String) = Completable.fromAction {
        if (webSocketClients[address] == null) {
            connect(address)
        }
    }

    private fun onAvailabilityChange(client: CustomWebSocketClient, status: ConnectionStatus) {
        when (status) {
            ConnectionStatus.UNKNOWN -> Unit
            ConnectionStatus.CONNECTED -> listener.onClientConnected(client)
            ConnectionStatus.DISCONNECTED -> retryConnection(client)
        }
    }

    private fun onMessageReceived(client: CustomWebSocketClient, message: String?) {
        val json = JSONObject(message)
        if (json.has(RtcCall.WEB_SOCKET_ACTION_KEY)) {
            when (json.getString(RtcCall.WEB_SOCKET_ACTION_KEY)) {
                RtcCall.BABY_IS_CRYING -> notificationHandler.showBabyIsCryingNotification()
                else -> Timber.e("Unrecognized action from message: $message")
            }
        }
    }

    private fun retryConnection(client: CustomWebSocketClient) =
            Completable.timer(RETRY_SECONDS_DELAY, TimeUnit.SECONDS)
                    .subscribeOn(Schedulers.io())
                    .subscribeBy(
                            onComplete = {
                                client.availability = ConnectionStatus.UNKNOWN
                                client.reconnect()
                            }
                    ).addTo(compositeDisposable)

    fun onDestroy() {
        for (client in webSocketClients) {
            client.value.onDestroy()
        }
        compositeDisposable.dispose()
    }

    private fun connect(address: String) {
        Timber.i("connecting $address...")
        webSocketClients[address] = CustomWebSocketClient(
                address,
                onAvailabilityChange = this::onAvailabilityChange,
                onMessageReceived = this::onMessageReceived
        )
    }

    fun getClient(address: String?) = webSocketClients[address]

    interface ConnectionListener {
        fun onClientConnected(client: CustomWebSocketClient)
    }

    companion object {
        private const val RETRY_SECONDS_DELAY = 10L
    }
}
