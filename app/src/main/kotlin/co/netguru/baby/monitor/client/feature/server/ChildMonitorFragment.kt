package co.netguru.baby.monitor.client.feature.server

import android.Manifest.permission.CAMERA
import android.Manifest.permission.RECORD_AUDIO
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
import co.netguru.baby.monitor.client.common.extensions.bindService
import co.netguru.baby.monitor.client.common.extensions.setVisible
import co.netguru.baby.monitor.client.common.extensions.showSnackbarMessage
import co.netguru.baby.monitor.client.data.communication.webrtc.CallState
import co.netguru.baby.monitor.client.data.communication.websocket.ClientConnectionStatus
import co.netguru.baby.monitor.client.feature.communication.webrtc.receiver.WebRtcReceiverService
import co.netguru.baby.monitor.client.feature.communication.webrtc.receiver.WebRtcReceiverService.WebRtcReceiverBinder
import co.netguru.baby.monitor.client.feature.machinelearning.MachineLearningService
import co.netguru.baby.monitor.client.feature.machinelearning.MachineLearningService.MachineLearningBinder
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_child_monitor.*
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ChildMonitorFragment : BaseDaggerFragment(), ServiceConnection {

    override val layoutResource = R.layout.fragment_child_monitor

    private val viewModel by lazy {
        ViewModelProviders.of(requireActivity(), factory)[ServerViewModel::class.java]
    }

    @Inject
    internal lateinit var factory: ViewModelProvider.Factory
    private var rtcReceiverServiceBinder: WebRtcReceiverBinder? = null
    private var machineLearningServiceBinder: MachineLearningBinder? = null
    private var isNightModeEnabled = false
    private var isFacingFront = false

    private val disposables = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.saveConfiguration()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
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
            rtcReceiverServiceBinder?.startCapturer()
        }
    }

    override fun onPause() {
        disposables.clear()
        viewModel.unregisterNsdService()
        super.onPause()
    }

    override fun onStop() {
        stopVideoPreview()
        super.onStop()
    }

    override fun onDestroy() {
        requireContext().unbindService(this)
        rtcReceiverServiceBinder?.cleanup()
        machineLearningServiceBinder?.cleanup()
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

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        when (service) {
            is WebRtcReceiverBinder -> {
                Timber.i("WebRtcReceiverService service connected")
                handleWebRtcReceiverBinder(service)
            }
            is MachineLearningBinder -> {
                Timber.i("MachineLearningService service connected")
                machineLearningServiceBinder = service
                service.setOnCryingBabyDetectedListener {
                    rtcReceiverServiceBinder?.handleBabyCrying()
                }
            }
        }
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
            rtcReceiverServiceBinder?.recreateCapturer(isFacingFront) { cameraSwapBtn.post { it.isEnabled = true } }
        }
        settingsIbtn.setOnClickListener {
            viewModel.shouldDrawerBeOpen.postValue(true)
        }
        logo.setOnClickListener { startVideoPreview() }
        message_video_disabled_energy_saver.setOnClickListener { startVideoPreview() }
    }

    private fun startVideoPreview() {
        rtcReceiverServiceBinder?.startRendering() ?: return
        video_preview_group.visibility = View.VISIBLE

        val totalTime = 65L
        Observable.intervalRange(0, totalTime + 1, 0, 1, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                        onNext = { elapsedSeconds ->
                            val secondsLeft = totalTime - elapsedSeconds
                            timer.text = if (secondsLeft < 60)
                                getString(R.string.message_disabling_video_preview_soon, "0:%02d".format(secondsLeft))
                            else ""
                        },
                        onComplete = ::stopVideoPreview
                )
                .addTo(disposables)
    }

    private fun stopVideoPreview() {
        rtcReceiverServiceBinder?.stopRendering()
        video_preview_group.visibility = View.GONE
    }

    private fun registerNsdService() {
        viewModel.registerNsdService {
            showSnackbarMessage(R.string.nsd_service_registration_failed)
        }
    }

    private fun handleCallStateChange(state: CallState) {
        when (state) {
            CallState.ENDED -> {
                rtcReceiverServiceBinder?.let(this::endCall)
            }
        }
    }

    private fun endCall(binder: WebRtcReceiverBinder) {
        binder.hangUpReceiver()
                .subscribeOn(Schedulers.io())
                .subscribeBy(
                        onComplete = {
                            machineLearningServiceBinder?.startRecording()
                        },
                        onError = Timber::e
                )
    }

    private fun bindServices() {
        bindService(
                WebRtcReceiverService::class.java,
                this,
                Service.BIND_AUTO_CREATE
        )
        bindService(
                MachineLearningService::class.java,
                this,
                Service.BIND_AUTO_CREATE
        )
    }

    private fun handleWebRtcReceiverBinder(service: WebRtcReceiverBinder) {
        rtcReceiverServiceBinder = service
        if (service.currentCall == null) {
            service.createReceiver(
                    surfaceView,
                    this@ChildMonitorFragment::handleCallStateChange
            )
            startVideoPreview()
        }
        service.clientConnectionStatus().observe(this, Observer { status ->
            Timber.d("Client status: $status.")
            when (status) {
                ClientConnectionStatus.CLIENT_CONNECTED ->
                    pulsatingView.start()
                ClientConnectionStatus.EMPTY ->
                    pulsatingView.stop()
            }
        })
    }

    companion object {
        private const val PERMISSIONS_REQUEST_CODE = 125

        private val permissions = arrayOf(
                RECORD_AUDIO, CAMERA
        )
    }
}
