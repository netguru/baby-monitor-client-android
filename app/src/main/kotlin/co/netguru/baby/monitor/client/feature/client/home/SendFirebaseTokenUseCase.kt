package co.netguru.baby.monitor.client.feature.client.home

import co.netguru.baby.monitor.client.feature.communication.websocket.Message
import co.netguru.baby.monitor.client.feature.communication.websocket.RxWebSocketClient
import co.netguru.baby.monitor.client.feature.firebasenotification.FirebaseInstanceManager
import com.google.gson.Gson
import io.reactivex.Completable
import javax.inject.Inject

class SendFirebaseTokenUseCase @Inject constructor(
    private val firebaseInstanceManager: FirebaseInstanceManager,
    private val gson: Gson
) {

    fun sendFirebaseToken(client: RxWebSocketClient): Completable =
        firebaseInstanceManager.getFirebaseToken()
            .flatMapCompletable { token ->
                gson.toJson(Message(pushNotificationsToken = token))
                    .let(client::send)
            }
}
