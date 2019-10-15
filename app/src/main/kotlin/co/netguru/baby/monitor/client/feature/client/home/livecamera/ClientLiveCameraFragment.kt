package co.netguru.baby.monitor.client.feature.client.home.livecamera

import android.Manifest
import android.app.Service
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.View
import co.netguru.baby.monitor.client.R
import co.netguru.baby.monitor.client.common.base.BaseDaggerFragment
import co.netguru.baby.monitor.client.common.extensions.allPermissionsGranted
import co.netguru.baby.monitor.client.data.communication.webrtc.CallState
import co.netguru.baby.monitor.client.feature.client.home.ClientHomeViewModel
import co.netguru.baby.monitor.client.feature.communication.webrtc.ConnectionState
import co.netguru.baby.monitor.client.feature.communication.webrtc.GatheringState
import co.netguru.baby.monitor.client.feature.communication.webrtc.StreamState
import co.netguru.baby.monitor.client.feature.communication.webrtc.WebRtcService
import co.netguru.baby.monitor.client.feature.communication.websocket.WebSocketClientService
import kotlinx.android.synthetic.main.fragment_client_live_camera.*
import org.webrtc.PeerConnection
import timber.log.Timber
import java.net.URI
import javax.inject.Inject

class ClientLiveCameraFragment : BaseDaggerFragment(), ServiceConnection {
    override val layoutResource = R.layout.fragment_client_live_camera

    @Inject
    internal lateinit var factory: ViewModelProvider.Factory

    private val viewModel by lazy {
        ViewModelProviders.of(requireActivity(), factory)[ClientHomeViewModel::class.java]
    }

    private val fragmentViewModel by lazy {
        ViewModelProviders.of(this, factory)[ClientLiveCameraFragmentViewModel::class.java]
    }

    private var socketBinder: WebSocketClientService.Binder? = null
    private var webRtcBinder: WebRtcService.Binder? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.selectedChildAvailability.observe(
            this,
            Observer { it?.let(::onAvailabilityChange) })
        viewModel.showBackButton(true)

        requireContext().bindService(
            Intent(requireContext(), WebSocketClientService::class.java),
            this,
            Service.BIND_AUTO_CREATE
        )
    }

    override fun onResume() {
        super.onResume()
        if (!requireContext().allPermissionsGranted(permissions)) {
            requestPermissions(permissions, PERMISSIONS_REQUEST_CODE)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.showBackButton(false)
        requireContext().unbindService(this)
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        Timber.i("onServiceDisconnected")
    }

    override fun onServiceConnected(name: ComponentName, service: IBinder) {
        when (service) {
            is WebSocketClientService.Binder -> {
                socketBinder = service
            }
            is WebRtcService.Binder -> {
                webRtcBinder = service
            }
        }

        maybeStartCall()
    }

    private fun handleStateChange(state: CallState) {
        Timber.i(state.toString())
    }

    private fun onAvailabilityChange(connectionAvailable: Boolean) {
        Timber.d("onAvailabilityChange($connectionAvailable)")
        if (connectionAvailable && !fragmentViewModel.callInProgress.get()) {
            maybeStartCall()
        }
    }

    private fun maybeStartCall() {
        val socketBinder = socketBinder ?: return Timber.e("No socket binder.")
        startCall(socketBinder)
    }

    private fun startCall(
        socketBinder: WebSocketClientService.Binder
    ) {
        val serverUri = URI.create(viewModel.selectedChild.value?.address ?: return)
        fragmentViewModel.startCall(
            requireActivity().applicationContext,
            liveCameraRemoteRenderer,
            serverUri,
            socketBinder.client(),
            this@ClientLiveCameraFragment::handleStateChange,
            this@ClientLiveCameraFragment::handleStreamStateChange
        )
    }

    private fun handleStreamStateChange(streamState: StreamState) {
        when (streamState) {
            is ConnectionState -> {
                if (streamState.connectionState == PeerConnection.IceConnectionState.COMPLETED) {
                    streamProgressBar.visibility = View.GONE
                }
            }
            is GatheringState -> Unit
        }
    }

    companion object {
        private const val PERMISSIONS_REQUEST_CODE = 125

        private val permissions = arrayOf(
            Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA
        )
    }
}
