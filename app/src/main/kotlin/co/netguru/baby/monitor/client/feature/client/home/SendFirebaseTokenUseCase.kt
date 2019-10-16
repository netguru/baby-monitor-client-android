package co.netguru.baby.monitor.client.feature.client.home

import co.netguru.baby.monitor.client.feature.communication.webrtc.base.RtcCall.Companion.PUSH_NOTIFICATIONS_KEY
import co.netguru.baby.monitor.client.feature.communication.webrtc.base.RtcCall.Companion.WEB_SOCKET_ACTION_KEY
import co.netguru.baby.monitor.client.feature.communication.websocket.RxWebSocketClient
import co.netguru.baby.monitor.client.feature.firebasenotification.FirebaseInstanceManager
import io.reactivex.Completable
import org.json.JSONObject
import javax.inject.Inject

class SendFirebaseTokenUseCase @Inject constructor(
    private val firebaseInstanceManager: FirebaseInstanceManager
) {

    fun sendFirebaseToken(client: RxWebSocketClient): Completable =
        firebaseInstanceManager.getFirebaseToken()
            .flatMapCompletable { token ->
                JSONObject()
                    .put(WEB_SOCKET_ACTION_KEY, PUSH_NOTIFICATIONS_KEY)
                    .put("value", token)
                    .toString()
                    .let(client::send)
            }
}
