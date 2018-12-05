package co.netguru.baby.monitor.client.feature.communication.websocket

class WebSocketClientFactory {

    fun create(
            address: String,
            listener: WebSocketCommunicationListener
    ): CustomWebSocketClient {
        return CustomWebSocketClient(address, listener)
    }

}
