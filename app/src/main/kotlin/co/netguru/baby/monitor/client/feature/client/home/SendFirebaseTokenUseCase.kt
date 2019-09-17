package co.netguru.baby.monitor.client.feature.client.home

import co.netguru.baby.monitor.client.feature.communication.webrtc.base.RtcCall.Companion.PUSH_NOTIFICATIONS_KEY
import co.netguru.baby.monitor.client.feature.communication.webrtc.base.RtcCall.Companion.WEB_SOCKET_ACTION_KEY
import co.netguru.baby.monitor.client.feature.communication.websocket.RxWebSocketClient
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.InstanceIdResult
import io.reactivex.Completable
import io.reactivex.Single
import org.json.JSONObject
import javax.inject.Inject

class SendFirebaseTokenUseCase @Inject constructor() {
    private fun firebaseInstanceId() = Single.create<InstanceIdResult> { emitter ->
        FirebaseInstanceId.getInstance().instanceId.addOnCompleteListener { task ->
            val result = task.result
            if (result != null) {
                emitter.onSuccess(result)
            } else {
                emitter.onError(task.exception ?: Throwable())
            }
        }
    }

    fun sendFirebaseToken(client: RxWebSocketClient): Completable =
        firebaseInstanceId()
            .map(InstanceIdResult::getToken)
            .flatMapCompletable { token ->
                JSONObject()
                    .put(WEB_SOCKET_ACTION_KEY, PUSH_NOTIFICATIONS_KEY)
                    .put("value", token)
                    .toString()
                    .let(client::send)
            }
}
