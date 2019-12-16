package co.netguru.baby.monitor.client.feature.communication.webrtc.client

import co.netguru.baby.monitor.client.feature.communication.webrtc.IceCandidateState
import co.netguru.baby.monitor.client.feature.communication.webrtc.OnIceCandidateAdded
import co.netguru.baby.monitor.client.feature.communication.webrtc.base.RtcMessageHandler
import co.netguru.baby.monitor.client.feature.communication.websocket.Message
import co.netguru.baby.monitor.client.feature.communication.websocket.RxWebSocketClient
import com.google.gson.Gson
import com.google.gson.JsonParseException
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import org.webrtc.IceCandidate
import org.webrtc.SessionDescription
import timber.log.Timber
import java.net.URI

class RtcClientMessageController(
    private val gson: Gson,
    private val serverUri: URI,
    private val rxWebSocketClient: RxWebSocketClient
) {

    lateinit var rtcMessageHandler: RtcMessageHandler
    private var compositeDisposable = CompositeDisposable()

    fun startRtcSession(
        sessionDescription: SessionDescription
    ) {

        if (rxWebSocketClient.isOpen()) sendOffer(sessionDescription)
        compositeDisposable += rxWebSocketClient.events(serverUri = serverUri)
            .doOnNext {
                if (it is RxWebSocketClient.Event.Open) sendOffer(sessionDescription)
            }
            .ofType(RxWebSocketClient.Event.Message::class.java)
            .subscribe { event: RxWebSocketClient.Event.Message ->
                handleIncomingMessage(event)
            }
    }

    fun dispose() {
        compositeDisposable.dispose()
    }

    fun handleIceCandidateChange(iceCandidateState: IceCandidateState) {
        if (iceCandidateState is OnIceCandidateAdded) sendIceCandidate(iceCandidateState.iceCandidate)
    }

    private fun sendIceCandidate(iceCandidate: IceCandidate) {
        sendMessage(
            Message(
                iceCandidate = Message.IceCandidateData(
                    sdp = iceCandidate.sdp,
                    sdpMid = iceCandidate.sdpMid,
                    sdpMLineIndex = iceCandidate.sdpMLineIndex
                )
            )
        )
    }

    private fun sendOffer(sessionDescription: SessionDescription) {
        sendMessage(
            Message(
                sdpOffer = Message.SdpData(
                    sdp = sessionDescription.description,
                    type = sessionDescription.type.canonicalForm()
                )
            )
        )
    }

    private fun handleIncomingMessage(event: RxWebSocketClient.Event.Message) {
        val message = parseWebSocketMessage(event)
        message?.sdpAnswer?.let {
            rtcMessageHandler.handleSdpAnswerMessage(it)
        }
        message?.iceCandidate?.let {
            rtcMessageHandler.handleIceCandidateMessage(it)
        }
        message?.sdpError?.let {
            rtcMessageHandler.handleBabyDeviceSdpError(it)
        }
    }

    private fun parseWebSocketMessage(event: RxWebSocketClient.Event.Message): Message? {
        return try {
            gson.fromJson(event.message, Message::class.java)
        } catch (e: JsonParseException) {
            Timber.w(e)
            null
        }
    }

    private fun sendMessage(message: Message) {
        compositeDisposable += rxWebSocketClient.send(message.let(gson::toJson))
            .subscribeBy(
                onComplete = { Timber.i("message sent: $message") },
                onError = { Timber.e(it) })
    }
}
