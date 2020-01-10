package co.netguru.baby.monitor.client.feature.client.home

import co.netguru.baby.monitor.client.feature.communication.websocket.Message
import co.netguru.baby.monitor.client.feature.communication.websocket.RxWebSocketClient
import co.netguru.baby.monitor.client.feature.firebasenotification.FirebaseInstanceManager
import io.reactivex.Completable
import javax.inject.Inject

class SendFirebaseTokenUseCase @Inject constructor(
    private val firebaseInstanceManager: FirebaseInstanceManager
) {

    fun sendFirebaseToken(client: RxWebSocketClient): Completable =
        firebaseInstanceManager.getFirebaseToken()
            .flatMapCompletable { token ->
                   client.send(Message(pushNotificationsToken = token))
            }
}
