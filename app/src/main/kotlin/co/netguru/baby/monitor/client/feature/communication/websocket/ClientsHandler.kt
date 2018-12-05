package co.netguru.baby.monitor.client.feature.communication.websocket

import co.netguru.baby.monitor.client.feature.common.NotificationHandler
import co.netguru.baby.monitor.client.feature.common.RunsInBackground
import co.netguru.baby.monitor.client.feature.communication.webrtc.RtcCall
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import org.json.JSONObject
import timber.log.Timber
import java.util.concurrent.TimeUnit

class ClientsHandler(
        private val notificationHandler: NotificationHandler,
        private val webSocketClientFactory: WebSocketClientFactory
) : WebSocketCommunicationListener {

    var webSocketClients = mutableMapOf<String, CustomWebSocketClient>()
    private var connectionListeners: MutableList<(CustomWebSocketClient) -> Unit> = mutableListOf()

    @RunsInBackground
    fun addClient(address: String) = Single.fromCallable {
        connect(address)
        address
    }

    fun addConnectionListener(listener: (CustomWebSocketClient) -> Unit) {
        connectionListeners.add(listener)
    }

    override fun onMessageReceived(client: CustomWebSocketClient, message: String?) {
        val json = JSONObject(message)
        if (json.has(RtcCall.WEB_SOCKET_ACTION_KEY)) {
            when (json.getString(RtcCall.WEB_SOCKET_ACTION_KEY)) {
                RtcCall.BABY_IS_CRYING -> notificationHandler.showBabyIsCryingNotification()
                else -> Timber.e("Unrecognized action from message: $message")
            }
        }
    }

    override fun onAvailabilityChanged(client: CustomWebSocketClient, connectionStatus: ConnectionStatus) {
        when (connectionStatus) {
            ConnectionStatus.UNKNOWN -> Unit
            ConnectionStatus.CONNECTED -> notifyAboutConnection(client)
            ConnectionStatus.DISCONNECTED -> retryConnection(client)
        }
    }

    private fun retryConnection(client: CustomWebSocketClient) =
            Completable.timer(RETRY_SECONDS_DELAY, TimeUnit.SECONDS)
                    .andThen {
                        connect(client.address)
                    }
                    .subscribeOn(Schedulers.io())
                    .subscribeBy(
                            onComplete = { Timber.i("connected to ${client.address}") }
                    )

    private fun notifyAboutConnection(client: CustomWebSocketClient) {
        for (listener in connectionListeners) {
            listener.invoke(client)
        }
    }

    fun onDestroy() {
        for (client in webSocketClients) {
            client.value.onDestroy()
        }
    }

    private fun connect(address: String) {
        Timber.i("connecting $address...")
        webSocketClients[address] = webSocketClientFactory.create(
                address,
                this
        ).apply { connect() }
    }

    fun getClient(address: String?) = webSocketClients[address]

    companion object {
        private const val RETRY_SECONDS_DELAY = 3L
    }
}
