package co.netguru.baby.monitor.client.feature.server

import android.Manifest.permission.CAMERA
import android.Manifest.permission.RECORD_AUDIO
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
import co.netguru.baby.monitor.client.common.extensions.bindService
import co.netguru.baby.monitor.client.common.extensions.setVisible
import co.netguru.baby.monitor.client.common.extensions.showSnackbarMessage
import co.netguru.baby.monitor.client.data.communication.websocket.ClientConnectionStatus
import co.netguru.baby.monitor.client.feature.batterylevel.LowBatteryReceiver
import co.netguru.baby.monitor.client.feature.communication.webrtc.WebRtcService
import co.netguru.baby.monitor.client.feature.communication.webrtc.base.RtcCall
import co.netguru.baby.monitor.client.feature.communication.websocket.WebSocketServerService
import co.netguru.baby.monitor.client.feature.machinelearning.MachineLearningService
import co.netguru.baby.monitor.client.feature.machinelearning.MachineLearningService.MachineLearningBinder
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_child_monitor.*
import org.java_websocket.WebSocket
import timber.log.Timber
import javax.inject.Inject


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
    private var isNightModeEnabled = false
    private var isFacingFront = false
    private val disposables = CompositeDisposable()

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
        webRtcServiceBinder?.addSurfaceView(surfaceView)
    }

    override fun onStart() {
        super.onStart()
        startVideoPreview()
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
        disposables.clear()
        serverViewModel.unregisterNsdService()
        super.onPause()
    }

    override fun onStop() {
        stopVideoPreview()
        super.onStop()
    }

    override fun onDestroyView() {
        surfaceView.release()
        super.onDestroyView()
    }

    override fun onDestroy() {
        disposables.clear()
        requireContext().unbindService(this)
        machineLearningServiceBinder?.cleanup()
        requireContext().unregisterReceiver(lowBatteryReceiver)
        super.onDestroy()
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
        Timber.i("Service Disconnected: $name")
    }

    override fun onServiceConnected(name: ComponentName, service: IBinder) {
        when (service) {
            is WebRtcService.Binder ->
                handleWebRtcBinder(service)
            is WebSocketServerService.Binder ->
                handleWebSocketServerBinder(service)
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
            if (isNightModeEnabled) {
                nightCoverV.setVisible(false)
                nightModeActiveIv.setVisible(false)
            } else {
                nightCoverV.setVisible(true)
                nightModeActiveIv.setVisible(true)
            }
            isNightModeEnabled = !isNightModeEnabled
        }
        cameraSwapBtn.setOnClickListener {
            cameraSwapBtn.isEnabled = false
            isFacingFront = !isFacingFront
        }
        settingsIbtn.setOnClickListener {
            serverViewModel.shouldDrawerBeOpen.postValue(true)
        }
        logo.setOnClickListener { startVideoPreview() }
        message_video_disabled_energy_saver.setOnClickListener { startVideoPreview() }
        serverViewModel.previewingVideo().observe(this, Observer { previewing ->
            if (previewing == true)
                startVideoPreview()
            else
                stopVideoPreview()
        })
        serverViewModel.timer().observe(this, Observer { secondsLeft ->
            timer.text = if (secondsLeft != null && secondsLeft < 60)
                getString(
                    R.string.message_disabling_video_preview_soon,
                    "0:%02d".format(secondsLeft)
                )
            else ""
        })
    }

    private fun startVideoPreview() {
        video_preview_group.visibility = View.VISIBLE
        serverViewModel.resetTimer()
        surfaceView.disableFpsReduction()
    }

    private fun stopVideoPreview() {
        video_preview_group.visibility = View.GONE
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
            startVideoPreview()
        }
    }

    private fun handleWebRtcBinder(service: WebRtcService.Binder) {
        Timber.d("handleWebRtcBinder($service)")
        webRtcServiceBinder = service
        service.addSurfaceView(surfaceView)
    }

    private fun handleWebSocketServerBinder(binder: WebSocketServerService.Binder) {
        Timber.d("handleWebSocketServerBinder($binder)")
        binder.clientConnectionStatus().observe(this, Observer { status ->
            Timber.d("Client status: $status.")
            when (status) {
                ClientConnectionStatus.CLIENT_CONNECTED ->
                    pulsatingView.start()
                ClientConnectionStatus.EMPTY ->
                    pulsatingView.stop()
            }
        })
        binder.messages()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { (ws, msg) ->
                msg.action()?.let { (key, value) ->
                    handleWebSocketAction(ws, key, value)
                }
                msg.babyName?.let { name ->
                    baby_name.text = name
                    baby_name.visibility =
                        if (name.isBlank())
                            View.GONE
                        else
                            View.VISIBLE
                }
            }
            .addTo(disposables)
    }

    private fun handleWebSocketAction(ws: WebSocket, key: String, value: String) {
        if (key == RtcCall.ADD_FIREBASE_TOKEN) {
            viewModel.receiveFirebaseToken(ws.remoteSocketAddress.address.hostAddress, value)
        } else {
            Timber.w("Unhandled web socket action: '$key', '$value'.")
        }
    }

    companion object {
        private const val PERMISSIONS_REQUEST_CODE = 125

        private val permissions = arrayOf(
                RECORD_AUDIO, CAMERA
        )
    }
}
