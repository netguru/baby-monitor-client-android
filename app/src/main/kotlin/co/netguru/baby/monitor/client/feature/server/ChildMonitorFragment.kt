package co.netguru.baby.monitor.client.feature.server

import android.Manifest.permission.CAMERA
import android.Manifest.permission.RECORD_AUDIO
import android.app.Service
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.View
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import co.netguru.baby.monitor.client.R
import co.netguru.baby.monitor.client.common.base.BaseDaggerFragment
import co.netguru.baby.monitor.client.common.extensions.allPermissionsGranted
import co.netguru.baby.monitor.client.common.extensions.bindService
import co.netguru.baby.monitor.client.common.extensions.observeNonNull
import co.netguru.baby.monitor.client.common.extensions.showSnackbarMessage
import co.netguru.baby.monitor.client.data.communication.websocket.ClientConnectionStatus
import co.netguru.baby.monitor.client.feature.batterylevel.LowBatteryReceiver
import co.netguru.baby.monitor.client.feature.communication.webrtc.WebRtcService
import co.netguru.baby.monitor.client.feature.communication.webrtc.observers.RtcServerConnectionState
import co.netguru.baby.monitor.client.feature.communication.websocket.WebSocketServerService
import co.netguru.baby.monitor.client.feature.machinelearning.MachineLearningService
import co.netguru.baby.monitor.client.feature.machinelearning.MachineLearningService.MachineLearningBinder
import kotlinx.android.synthetic.main.fragment_child_monitor.*
import timber.log.Timber
import javax.inject.Inject

@Suppress("TooManyFunctions")
class ChildMonitorFragment : BaseDaggerFragment(), ServiceConnection {

    override val layoutResource = R.layout.fragment_child_monitor

    private val serverViewModel by lazy {
        ViewModelProviders.of(requireActivity(), factory)[ServerViewModel::class.java]
    }
    private val viewModel by lazy {
        ViewModelProviders.of(requireActivity(), factory).get(ChildMonitorViewModel::class.java)
    }

    @Inject
    internal lateinit var factory: ViewModelProvider.Factory
    private var machineLearningServiceBinder: MachineLearningBinder? = null
    private var webRtcServiceBinder: WebRtcService.Binder? = null

    @Inject
    internal lateinit var lowBatteryReceiver: LowBatteryReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        serverViewModel.saveConfiguration()
        lowBatteryReceiver.register(requireContext())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        setupObservers()
        webRtcServiceBinder?.addSurfaceView(surfaceView)
    }

    override fun onResume() {
        super.onResume()
        registerNsdService()
        if (!requireContext().allPermissionsGranted(permissions)) {
            requestPermissions(permissions, PERMISSIONS_REQUEST_CODE)
        } else {
            bindServices()
        }
    }

    override fun onPause() {
        serverViewModel.unregisterNsdService()
        serverViewModel.toggleVideoPreview(false)
        super.onPause()
    }

    override fun onDestroyView() {
        surfaceView.release()
        super.onDestroyView()
    }

    override fun onDestroy() {
        requireContext().unbindService(this)
        machineLearningServiceBinder?.cleanup()
        requireContext().unregisterReceiver(lowBatteryReceiver)
        super.onDestroy()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requireContext().allPermissionsGranted(Companion.permissions)) {
            bindServices()
        }
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        Timber.i("Service Disconnected: $name")
    }

    override fun onServiceConnected(name: ComponentName, service: IBinder) {
        when (service) {
            is WebRtcService.Binder ->
                handleWebRtcBinder(service)
            is WebSocketServerService.Binder ->
                viewModel.handleWebSocketServerBinder(service)
            is MachineLearningBinder -> {
                Timber.i("MachineLearningService service connected")
                machineLearningServiceBinder = service
            }
        }
    }

    fun handleLowBattery() {
        viewModel.notifyLowBattery(
            title = getString(R.string.notification_low_battery_title),
            text = getString(R.string.notification_low_battery_text)
        )
    }

    private fun setupView() {
        nightModeToggleBtn.setOnClickListener {
            viewModel.switchNightMode()
        }
        settingsIbtn.setOnClickListener {
            serverViewModel.toggleDrawer(true)
        }
        videoPreviewButton.setOnClickListener { serverViewModel.toggleVideoPreview(true) }
    }

    private fun setupObservers() {
        serverViewModelObservers()
        childMonitorObservables()
    }

    private fun childMonitorObservables() {
        viewModel.babyNameStatus.observeNonNull(viewLifecycleOwner, { name ->
            babyName.text = name
            babyName.visibility =
                if (name.isBlank()) {
                    View.GONE
                } else {
                    View.VISIBLE
                }
        })
        viewModel.nightModeStatus.observe(viewLifecycleOwner, Observer { isNightModeEnabled ->
            nightModeGroup.isVisible = isNightModeEnabled
        })

        viewModel.pulsatingViewStatus.observe(viewLifecycleOwner, Observer { status ->
            Timber.d("Client status: $status.")
            when (status) {
                ClientConnectionStatus.CLIENT_CONNECTED ->
                    pulsatingView.start()
                ClientConnectionStatus.EMPTY ->
                    pulsatingView.stop()
            }
        })
    }

    private fun serverViewModelObservers() {
        serverViewModel.previewingVideo.observeNonNull(viewLifecycleOwner, { previewing ->
            if (previewing) {
                showVideoPreview()
            } else {
                hideVideoPreview()
            }
        })
        serverViewModel.timer.observe(viewLifecycleOwner, Observer { secondsLeft ->
            timer.text = if (secondsLeft != null && secondsLeft < VIDEO_PREVIEW_MAX_TIME) {
                getString(
                    R.string.message_disabling_video_preview_soon,
                    "0:%02d".format(secondsLeft)
                )
            } else {
                ""
            }
        })

        serverViewModel.rtcConnectionStatus.observeNonNull(viewLifecycleOwner, { connectionState ->
            when (connectionState) {
                RtcServerConnectionState.ConnectionOffer -> machineLearningServiceBinder?.stopRecording()
                RtcServerConnectionState.Disconnected -> machineLearningServiceBinder?.startRecording()
                else -> Unit
            }
        })
        serverViewModel.cameraState.observe(viewLifecycleOwner, Observer { cameraState ->
            webRtcServiceBinder?.enableCamera(
                cameraState.previewEnabled ||
                        cameraState.streamingEnabled
            )
        })
    }

    private fun showVideoPreview() {
        Timber.i("showVideoPreview")
        videoPreviewGroup.isVisible = true
        videoPreviewTogglingGroup.isVisible = false
        serverViewModel.resetTimer()
        surfaceView.disableFpsReduction()
    }

    private fun hideVideoPreview() {
        Timber.i("hideVideoPreview")
        videoPreviewGroup.isVisible = false
        videoPreviewTogglingGroup.isVisible = true
        surfaceView.pauseVideo()
    }

    private fun registerNsdService() {
        serverViewModel.registerNsdService {
            showSnackbarMessage(R.string.nsd_service_registration_failed)
        }
    }

    private fun bindServices() {
        bindService(
            MachineLearningService::class.java,
            this,
            Service.BIND_AUTO_CREATE
        )
        requireContext().run {
            bindService(
                Intent(this, WebRtcService::class.java),
                this@ChildMonitorFragment,
                Service.BIND_AUTO_CREATE
            )
            bindService(
                Intent(this, WebSocketServerService::class.java),
                this@ChildMonitorFragment,
                Service.BIND_AUTO_CREATE
            )
        }
    }

    private fun handleWebRtcBinder(webRtcServiceBinder: WebRtcService.Binder) {
        serverViewModel.handleRtcServerConnectionState(webRtcServiceBinder)
        this.webRtcServiceBinder = webRtcServiceBinder
        serverViewModel.toggleVideoPreview(true)
        webRtcServiceBinder.addSurfaceView(surfaceView)
    }

    companion object {
        private const val PERMISSIONS_REQUEST_CODE = 125
        private const val VIDEO_PREVIEW_MAX_TIME = 60

        private val permissions = arrayOf(
            RECORD_AUDIO, CAMERA
        )
    }
}
