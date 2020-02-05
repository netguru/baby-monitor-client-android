package co.netguru.baby.monitor.client.feature.communication.websocket

import io.reactivex.Observable

interface MessageController {
    fun sendMessage(message: Message)
    fun receivedMessages(): Observable<Message>
}
