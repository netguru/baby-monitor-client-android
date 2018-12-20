package co.netguru.baby.monitor.client.feature.communication.websocket

import co.netguru.baby.monitor.client.feature.common.NotificationHandler
import co.netguru.baby.monitor.client.feature.common.RunsInBackground
import co.netguru.baby.monitor.client.feature.communication.webrtc.RtcCall
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import org.java_websocket.framing.CloseFrame
import org.json.JSONObject
import timber.log.Timber
import java.util.concurrent.TimeUnit

class ClientsHandler(
        private val listener: ConnectionListener,
        private val notificationHandler: NotificationHandler
) {

    private val webSocketClients = mutableMapOf<String, CustomWebSocketClient>()
    private val compositeDisposable = CompositeDisposable()

    @RunsInBackground
    fun addClient(address: String) = Completable.fromAction {
        if (webSocketClients[address] == null) {
            connect(address)
        }
    }

    fun getClient(address: String?) = webSocketClients[address]

    fun reconnectClient(address: String) {
        webSocketClients[address]?.let { client ->
            client.closeClient()
                    .subscribeOn(Schedulers.newThread())
                    .subscribeBy(
                            onComplete = {
                                client.onDestroy()
                                connect(address)
                            },
                            onError = Timber::e
                    ).addTo(compositeDisposable)
        }
    }

    fun onDestroy() {
        Single.just(webSocketClients.map { it.value })
                .map { list ->
                    for (client in list) {
                        client.close(CloseFrame.FLASHPOLICY, "")
                        client.onDestroy()
                    }
                    true
                }.subscribeOn(Schedulers.newThread())
                .subscribeBy(
                        onSuccess = { Timber.i("clients destroyed") },
                        onError = Timber::e
                ).addTo(compositeDisposable)
        compositeDisposable.dispose()
    }

    private fun onAvailabilityChange(client: CustomWebSocketClient, status: ConnectionStatus) {
        when (status) {
            ConnectionStatus.UNKNOWN -> Unit
            ConnectionStatus.CONNECTED -> listener.onConnectionStatusChange(client)
            ConnectionStatus.DISCONNECTED -> retryConnection(client.address)
            ConnectionStatus.RETRYING -> Unit
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

    private fun retryConnection(address: String) {
        Completable.timer(RETRY_SECONDS_DELAY, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.newThread())
                .subscribeBy(
                        onComplete = {
                            webSocketClients[address]?.let(this::reconnect)
                        }
                ).addTo(compositeDisposable)
        listener.onConnectionStatusChange(webSocketClients[address] ?: return)
    }

    private fun reconnect(client: CustomWebSocketClient) {
        if (client.connectionStatus != ConnectionStatus.CONNECTED) {
            client.asyncReconnect()
                    .subscribeOn(Schedulers.newThread())
                    .subscribeBy(
                            onComplete = { Timber.i("reconnect") },
                            onError = Timber::e
                    )
        }
    }

    private fun connect(address: String): CustomWebSocketClient? {
        Timber.i("connecting $address...")
        val status = webSocketClients[address]?.connectionStatus ?: ConnectionStatus.UNKNOWN
        val retrying = webSocketClients[address]?.wasRetrying ?: false

        webSocketClients[address] = CustomWebSocketClient(
                address,
                onAvailabilityChange = this::onAvailabilityChange,
                onMessageReceived = this::onMessageReceived
        ).apply {
            connectionStatus = status
            wasRetrying = retrying
        }.also { client ->
            client.connectClient()
                    .subscribeOn(Schedulers.newThread())
                    .subscribeBy(
                            onComplete = {
                                Timber.i("Complete")
                            },
                            onError = {
                                Timber.e("connection error: $it")
                                client.notifyAvailabilityChange(ConnectionStatus.DISCONNECTED)
                            }
                    ).addTo(compositeDisposable)
        }
        return webSocketClients[address]
    }

    interface ConnectionListener {
        fun onConnectionStatusChange(client: CustomWebSocketClient)
    }

    companion object {
        private const val RETRY_SECONDS_DELAY = 10L
    }
}
