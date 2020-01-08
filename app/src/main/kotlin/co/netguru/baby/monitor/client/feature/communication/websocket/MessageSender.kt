package co.netguru.baby.monitor.client.feature.communication.websocket

interface MessageSender {
    fun sendMessage(message: Message)
}
