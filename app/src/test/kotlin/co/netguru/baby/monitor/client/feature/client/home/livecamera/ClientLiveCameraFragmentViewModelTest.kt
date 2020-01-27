package co.netguru.baby.monitor.client.feature.client.home.livecamera

import android.content.Context
import co.netguru.baby.monitor.client.common.view.CustomSurfaceViewRenderer
import co.netguru.baby.monitor.client.feature.analytics.AnalyticsManager
import co.netguru.baby.monitor.client.feature.communication.webrtc.client.RtcClientController
import co.netguru.baby.monitor.client.feature.communication.websocket.RxWebSocketClient
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.junit.Test
import java.net.URI

class ClientLiveCameraFragmentViewModelTest {

    private val analyticsManager = mock<AnalyticsManager>()
    private val rtcClientController = mock<RtcClientController>()
    private val clientLiveCameraFragmentViewModel =
        ClientLiveCameraFragmentViewModel(analyticsManager, rtcClientController)

    @Test
    fun `should start call`() {
        val context = mock<Context>()
        val liveCameraRemoteRenderer = mock<CustomSurfaceViewRenderer>()
        val serverUri = mock<URI>()
        val client = mock<RxWebSocketClient>()

        assert(!clientLiveCameraFragmentViewModel.callInProgress.get())

        clientLiveCameraFragmentViewModel.startCall(
            context,
            liveCameraRemoteRenderer,
            serverUri,
            client,
            true
        )

        verify(rtcClientController).startCall(
            eq(context), eq(liveCameraRemoteRenderer), eq(serverUri),
            eq(client), any(), any()
        )
        assert(clientLiveCameraFragmentViewModel.callInProgress.get())
    }

    @Test
    fun `should end call`() {
        clientLiveCameraFragmentViewModel.endCall()

        verify(rtcClientController).endCall()
    }

    @Test
    fun `should call push to speak`() {
        clientLiveCameraFragmentViewModel.pushToSpeak(true)

        verify(rtcClientController).pushToSpeak(true)

        clientLiveCameraFragmentViewModel.pushToSpeak(false)

        verify(rtcClientController).pushToSpeak(false)
    }
}
