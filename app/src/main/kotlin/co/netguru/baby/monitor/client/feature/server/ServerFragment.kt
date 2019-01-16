package co.netguru.baby.monitor.client.feature.server

import android.Manifest.permission.CAMERA
import android.Manifest.permission.RECORD_AUDIO
import android.app.Service
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.ComponentName
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import androidx.navigation.fragment.findNavController
import co.netguru.baby.monitor.client.R
import co.netguru.baby.monitor.client.common.base.BaseDaggerFragment
import co.netguru.baby.monitor.client.common.extensions.allPermissionsGranted
import co.netguru.baby.monitor.client.common.extensions.bindService
import co.netguru.baby.monitor.client.common.extensions.showSnackbarMessage
import co.netguru.baby.monitor.client.data.communication.webrtc.CallState
import co.netguru.baby.monitor.client.feature.communication.webrtc.receiver.WebRtcReceiverService
import co.netguru.baby.monitor.client.feature.communication.webrtc.receiver.WebRtcReceiverService.WebRtcReceiverBinder
import co.netguru.baby.monitor.client.feature.machinelearning.MachineLearningService
import co.netguru.baby.monitor.client.feature.machinelearning.MachineLearningService.MachineLearningBinder
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_server.*
import timber.log.Timber
import javax.inject.Inject

//TODO Should be refactored
class ServerFragment : BaseDaggerFragment(), ServiceConnection {

    override val layoutResource = R.layout.fragment_server

    private val viewModel by lazy {
        ViewModelProviders.of(this, factory)[ServerViewModel::class.java]
    }

    @Inject
    internal lateinit var factory: ViewModelProvider.Factory
    private var rtcReceiverServiceBinder: WebRtcReceiverBinder? = null
    private var machineLearningServiceBinder: MachineLearningBinder? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.saveConfiguration()
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
        goToSettingsImgBtn.setOnClickListener {
            findNavController().navigate(R.id.serverToSettings)
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.unregisterNsdService()
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
                rtcReceiverServiceBinder = service
                if (service.currentCall == null) {
                    service.createReceiver(
                            surfaceView,
                            this@ServerFragment::handleCallStateChange
                    )
                }
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

    private fun registerNsdService() {
        viewModel.registerNsdService {
            showSnackbarMessage(R.string.nsd_service_registration_failed)
        }
    }

    private fun handleCallStateChange(state: CallState) {
        when (state) {
            CallState.CONNECTING -> {
                machineLearningServiceBinder?.stopRecording()
            }
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

    companion object {
        private const val PERMISSIONS_REQUEST_CODE = 125

        private val permissions = arrayOf(
                RECORD_AUDIO, CAMERA
        )
    }
}
