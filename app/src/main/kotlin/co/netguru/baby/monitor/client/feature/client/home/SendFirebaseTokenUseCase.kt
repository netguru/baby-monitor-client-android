package co.netguru.baby.monitor.client.feature.client.home

import co.netguru.baby.monitor.client.feature.communication.webrtc.base.RtcCall.Companion.WEB_SOCKET_ACTION_KEY
import co.netguru.baby.monitor.client.feature.communication.websocket.RxWebSocketClient
import co.netguru.baby.monitor.client.feature.firebasenotification.FirebaseInstanceManager
import co.netguru.baby.monitor.client.feature.firebasenotification.FirebaseInstanceManager.Companion.PUSH_NOTIFICATIONS_KEY
import com.google.gson.JsonObject
import io.reactivex.Completable
import javax.inject.Inject

class SendFirebaseTokenUseCase @Inject constructor(
    private val firebaseInstanceManager: FirebaseInstanceManager
) {

    fun sendFirebaseToken(client: RxWebSocketClient): Completable =
        firebaseInstanceManager.getFirebaseToken()
            .flatMapCompletable { token ->
                JsonObject()
                    .apply {
                        addProperty(WEB_SOCKET_ACTION_KEY, PUSH_NOTIFICATIONS_KEY)
                        addProperty("value", token)
                    }
                    .toString()
                    .let(client::send)
            }
}
