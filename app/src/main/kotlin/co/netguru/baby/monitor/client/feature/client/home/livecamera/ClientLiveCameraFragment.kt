package co.netguru.baby.monitor.client.feature.client.home.livecamera

import android.Manifest
import android.app.AlertDialog
import android.app.Service
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.ComponentName
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.View
import co.netguru.baby.monitor.client.R
import co.netguru.baby.monitor.client.common.base.BaseDaggerFragment
import co.netguru.baby.monitor.client.common.extensions.allPermissionsGranted
import co.netguru.baby.monitor.client.common.extensions.and
import co.netguru.baby.monitor.client.common.extensions.bindService
import co.netguru.baby.monitor.client.data.communication.webrtc.CallState
import co.netguru.baby.monitor.client.data.communication.websocket.ConnectionStatus
import co.netguru.baby.monitor.client.feature.client.home.ClientHomeViewModel
import co.netguru.baby.monitor.client.feature.communication.webrtc.client.WebRtcClientService
import co.netguru.baby.monitor.client.feature.communication.webrtc.client.WebRtcClientService.WebRtcClientBinder
import co.netguru.baby.monitor.client.feature.communication.websocket.ClientHandlerService
import co.netguru.baby.monitor.client.feature.communication.websocket.ClientHandlerService.ChildServiceBinder
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_client_live_camera.*
import timber.log.Timber
import javax.inject.Inject

class ClientLiveCameraFragment : BaseDaggerFragment(), ServiceConnection {
    override val layoutResource = R.layout.fragment_client_live_camera

    @Inject
    internal lateinit var factory: ViewModelProvider.Factory

    private val viewModel by lazy {
        ViewModelProviders.of(requireActivity(), factory)[ClientHomeViewModel::class.java]
    }
    private val compositeDisposable = CompositeDisposable()
    private var childServiceBinder: ChildServiceBinder? = null
    private var webRtcClientBinder: WebRtcClientBinder? = null

    private var errorOccurs = false;

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.selectedChildAvailability.observe(this, Observer(this::onAvailabilityChange))
        viewModel.showBackButton(true)
    }

    override fun onResume() {
        super.onResume()
        if (!requireContext().allPermissionsGranted(permissions)) {
            requestPermissions(permissions, PERMISSIONS_REQUEST_CODE)
        } else {
            bindServices()
        }
    }

    override fun onPause() {
        super.onPause()
        if (webRtcClientBinder != null) {
            webRtcClientBinder?.cleanup()
            childServiceBinder?.refreshChildWebSocketConnection(viewModel.selectedChild.value?.address)
            requireContext().unbindService(this)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.showBackButton(false)
        webRtcClientBinder?.cleanup()
        childServiceBinder?.refreshChildWebSocketConnection(viewModel.selectedChild.value?.address)
        compositeDisposable.dispose()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requireContext().allPermissionsGranted(Companion.permissions)) {
            bindServices()
        }
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        Timber.i("onServiceDisconnected")
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        when (service) {
            is WebRtcClientBinder -> {
                Timber.i("WebRtcReceiverService service connected")
                webRtcClientBinder = service
            }
            is ChildServiceBinder -> {
                Timber.i("ClientHandlerService service connected")
                childServiceBinder = service
            }
        }
        if (webRtcClientBinder == null || webRtcClientBinder?.callInProgress?.get() != true)
            startCall()
    }

    private fun bindServices() {
        bindService(
            WebRtcClientService::class.java,
            this,
            Service.BIND_AUTO_CREATE
        )
        bindService(
            ClientHandlerService::class.java,
            this,
            Service.BIND_AUTO_CREATE
        )
    }

    private fun handleStateChange(state: CallState) {
        Timber.i(state.toString())
        if (state == CallState.COMPLETED) {
            errorOccurs = true
            //todo temporary fix for video freeze problem
            webRtcClientBinder?.cleanup()
        }
    }

    private fun onAvailabilityChange(connectionStatus: ConnectionStatus?) {
        Timber.e("AvailabilityChange $connectionStatus")
        when (connectionStatus) {
            ConnectionStatus.CONNECTED -> if (webRtcClientBinder == null || webRtcClientBinder?.callInProgress?.get() != true || errorOccurs) {
                startCall()
            }
            ConnectionStatus.DISCONNECTED -> {
                if (errorOccurs) {
                    //todo temporary fix for video freeze problem
                    AlertDialog.Builder(requireActivity())
                        .setMessage(getString(R.string.camera_error))
                        .setPositiveButton(
                            android.R.string.ok
                        ) { dialog, which -> requireActivity().onBackPressed() }
                        .create()
                        .show()
                } else {
                    requireActivity().onBackPressed()
                }
            }
            else -> {
                requireActivity().onBackPressed()
            }
        }
    }

    private fun startCall() {
        errorOccurs = false
        with((webRtcClientBinder and childServiceBinder)) {
            this ?: return@with
            val address = viewModel.selectedChild.value?.address ?: return@with
            val client = second.getChildClient(address) ?: return@with
            first.apply {
                createClient(client)
                startCall(
                    requireActivity().applicationContext,
                    this@ClientLiveCameraFragment::handleStateChange
                )
                setRemoteRenderer(liveCameraRemoteRenderer)
            }
        }
    }

    companion object {
        private const val PERMISSIONS_REQUEST_CODE = 125

        private val permissions = arrayOf(
            Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA
        )
    }
}
