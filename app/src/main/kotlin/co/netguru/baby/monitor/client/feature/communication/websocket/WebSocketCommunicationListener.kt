package co.netguru.baby.monitor.client.feature.communication.websocket

interface WebSocketCommunicationListener {

    fun onAvailabilityChanged(client: CustomWebSocketClient, connectionStatus: ConnectionStatus)
    fun onMessageReceived(client: CustomWebSocketClient, message: String?)

}
