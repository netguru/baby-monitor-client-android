package co.netguru.baby.monitor.client.feature.client.home.livecamera

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
import co.netguru.baby.monitor.client.data.communication.webrtc.CallState
import co.netguru.baby.monitor.client.feature.babycrynotification.CryingActionIntentService
import co.netguru.baby.monitor.client.feature.client.home.BackButtonState
import co.netguru.baby.monitor.client.feature.client.home.ClientHomeViewModel
import co.netguru.baby.monitor.client.feature.communication.webrtc.ConnectionState
import co.netguru.baby.monitor.client.feature.communication.webrtc.StreamState
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.selectedChildAvailability.observe(
            this,
            Observer { it?.let(::onAvailabilityChange) })
        viewModel.setBackButtonState(
            BackButtonState(
                true,
                shouldShowSnoozeDialogOnBack()
            )
        )
        requireContext().bindService(
            Intent(requireContext(), WebSocketClientService::class.java),
            this,
            Service.BIND_AUTO_CREATE
        )
    }

    override fun onStart() {
        super.onStart()
        maybeStartCall()
    }

    override fun onStop() {
        super.onStop()
        fragmentViewModel.endCall()
    }

    private fun shouldShowSnoozeDialogOnBack() =
        arguments?.getBoolean(CryingActionIntentService.SHOULD_SHOW_SNOOZE_DIALOG) == true

    override fun onDestroy() {
        super.onDestroy()
        viewModel.setBackButtonState(
            BackButtonState(
                shouldBeVisible = false,
                shouldShowSnoozeDialog = false
            )
        )
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
        }
        maybeStartCall()
    }

    private fun handleStateChange(state: CallState) {
        Timber.i(state.toString())
    }

    private fun onAvailabilityChange(connectionAvailable: Boolean) {
        Timber.d("onAvailabilityChange($connectionAvailable)")
        if (connectionAvailable) {
            maybeStartCall()
        }
    }

    private fun maybeStartCall() {
        val socketBinder = socketBinder ?: return Timber.e("No socket binder.")
        if (!fragmentViewModel.callInProgress.get()) startCall(socketBinder)
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
        when ((streamState as? ConnectionState)?.connectionState) {
            PeerConnection.IceConnectionState.COMPLETED -> streamProgressBar.visibility = View.GONE
            PeerConnection.IceConnectionState.CHECKING -> streamProgressBar.visibility =
                View.VISIBLE
            else -> Unit
        }
    }
}
